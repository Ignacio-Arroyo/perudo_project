package perudo_backend.perudo_backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import perudo_backend.perudo_backend.Game;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.dto.*;
import perudo_backend.perudo_backend.dto.RollDiceRequest;
import perudo_backend.perudo_backend.services.*;
import perudo_backend.exception.PlayerNotFoundException;
import perudo_backend.perudo_backend.Dice;
import perudo_backend.perudo_backend.GameStatus;
import perudo_backend.exception.NotEnoughPlayersException;

import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private GameService gameService;

    @MessageMapping("/game/create")
    @SendTo("/topic/lobby")
    public GameStateDTO createGame() {
        System.out.println("Received game creation request");
        GameStateDTO gameState = gameService.createGame();
        System.out.println("Created game state: " + gameState);
        
        if (gameState.getId() == null || gameState.getId().equals("null")) {
            throw new IllegalStateException("Game creation failed - invalid ID");
        }
        
        return gameState;
    }

    @PostMapping("/{gameId}/join/{playerId}")
    public ResponseEntity<GameStateDTO> joinGame(
            @PathVariable String gameId,  // Changed from int to String
            @PathVariable String playerId) {  // Changed from int to String
        return ResponseEntity.ok(gameService.joinGame(gameId, playerId));
    }

    @GetMapping("/{gameId}/players/{playerId}")
    public ResponseEntity<GameStateDTO> getGameState(@PathVariable String gameId, @PathVariable String playerId) {
        return ResponseEntity.ok(gameService.getGameState(gameId));
    }

    @MessageMapping("/game/start")
    public void startGame(StartGameRequest request) {
        try {
            GameStateDTO gameState = gameService.startGame(request.getGameId());
            // Manually send to the specific game topic
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId() + "/state", gameState);
        } catch (NotEnoughPlayersException e) {
            log.error("Cannot start game: {}", e.getMessage());
            GameStateDTO errorState = new GameStateDTO(
                request.getGameId(),
                GameStatus.ERROR,
                e.getMessage()
            );
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId() + "/state", errorState);
        }
    }

    @PostMapping("/{gameId}/players/{playerId}/bid")
    public ResponseEntity<GameStateDTO> placeBid(
            @PathVariable String gameId,  // Changed from int to String
            @PathVariable String playerId,  // Changed from int to String
            @RequestBody BidRequest bid) {
        return ResponseEntity.ok(gameService.processBid(gameId, playerId, bid.getQuantity(), bid.getValue()));
    }

    @MessageMapping("/game/{gameId}/bid")
    public void makeBid(@DestinationVariable String gameId, BidRequest request) {
        log.info("Processing bid via WebSocket: gameId={}, playerId={}, quantity={}, value={}",
            gameId, request.getPlayerId(), request.getQuantity(), request.getValue());
            
        try {
            GameStateDTO gameState = gameService.processBid(gameId, String.valueOf(request.getPlayerId()), 
                request.getQuantity(), request.getValue());
                
            // Log détaillé de l'état du jeu après la mise
            log.info("After bid: gameId={}, currentPlayerId={}, players={}, turnSequence={}",
                gameId, gameState.getCurrentPlayerId(),
                gameState.getPlayers() != null ? gameState.getPlayers().size() : 0,
                gameState.getTurnSequence() != null ? gameState.getTurnSequence().size() : 0);
                
            // Broadcast new game state
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", gameState);
            
            log.info("Game state broadcasted after bid");
        } catch (Exception e) {
            log.error("Error processing bid: ", e);
            GameStateDTO errorState = new GameStateDTO(
                gameId,
                GameStatus.ERROR,
                e.getMessage()
            );
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", errorState);
        }
    }

    @PostMapping("/{gameId}/players/{playerId}/challenge")
    public ResponseEntity<GameStateDTO> challenge(
            @PathVariable String gameId,  // Changed from int to String
            @PathVariable String playerId) {  // Changed from int to String
        return ResponseEntity.ok(gameService.processChallenge(gameId, playerId));
    }

    @MessageMapping("/game/{gameId}/challenge")
    public void challenge(@DestinationVariable String gameId, ChallengeRequest request) {
        GameStateDTO gameState = gameService.processChallenge(gameId, String.valueOf(request.getPlayerId()));
        
        // Broadcast challenge result and new game state
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", 
            gameState);
    }    @MessageMapping("/game/{gameId}/state")
    public void getGameState(@DestinationVariable String gameId, Principal principal) {
        Game game = gameService.getGame(gameId);
        String playerId = String.valueOf(getUserId(principal));
        
        messagingTemplate.convertAndSendToUser(
            principal.getName(),
            "/topic/game/" + gameId + "/state",
            new GameStateDTO(game, playerId)
        );
    }

    @GetMapping("/{gameId}/players/{playerId}/dice")
    public ResponseEntity<DiceRollDTO> getPlayerDice(@PathVariable String gameId, @PathVariable String playerId) {
        Game game = gameService.findById(gameId);
        Player player = game.getPlayers().stream()
            .filter(p -> String.valueOf(p.getId()).equals(playerId))
            .findFirst()
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
        
        List<Integer> diceValues = player.getDice().stream()
            .map(Dice::getValue)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new DiceRollDTO(String.valueOf(player.getId()), diceValues));
    }

    @MessageMapping("/game/roll")
    public void handleRoll(RollDiceRequest request) {
        try {
            log.info("Received roll request for player {} in game {}", request.getPlayerId(), request.getGameId());
            
            // Log the players in the game
            Game game = gameService.getGame(request.getGameId());
            log.info("Players in game: {}", game.getPlayers().stream()
                .map(p -> "ID: " + p.getId() + ", Username: " + p.getUsername())
                .collect(Collectors.joining(", ")));
                
            GameStateDTO gameState = gameService.handleRoll(request.getGameId(), request.getPlayerId());
            
            // Send updated game state to all players
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId() + "/state", gameState);
            
            // Send private dice info to the player who rolled
            Player player = gameService.getPlayer(request.getGameId(), request.getPlayerId());
            DiceRollDTO diceRoll = new DiceRollDTO(
                request.getPlayerId(),
                player.getDice().stream()
                    .map(Dice::getValue)
                    .collect(Collectors.toList())
            );
            
            messagingTemplate.convertAndSendToUser(
                request.getPlayerId(),
                "/queue/game/" + request.getGameId() + "/dice",
                diceRoll
            );
        } catch (Exception e) {
            log.error("Error handling roll: ", e);
            GameStateDTO errorState = new GameStateDTO(
                request.getGameId(),
                GameStatus.ERROR,
                e.getMessage()
            );
            messagingTemplate.convertAndSend("/topic/game/" + request.getGameId() + "/state", errorState);
        }
    }

    private int getUserId(Principal principal) {
        // Implementation depends on your authentication system
        // This is a placeholder - implement according to your needs
        return Integer.parseInt(principal.getName());
    }

    // Ajouter un endpoint de diagnostic
    @GetMapping("/{gameId}/debug")
    public ResponseEntity<String> debugGame(@PathVariable String gameId) {
        try {
            String debugInfo = gameService.debugGameState(gameId);
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            log.error("Error getting debug info for game {}: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}