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
import perudo_backend.perudo_backend.services.*;
import perudo_backend.exception.PlayerNotFoundException;
import perudo_backend.perudo_backend.Dice;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/games")
public class GameController {

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
        GameStateDTO gameState = gameService.startGame(request.getGameId());
        Game game = gameService.getGame(request.getGameId());
        
        // Send initial game state to all players
        messagingTemplate.convertAndSend("/topic/game/" + request.getGameId() + "/state", 
            gameState);

        // Send private dice rolls to each player
        game.getPlayers().forEach(player -> {
            messagingTemplate.convertAndSendToUser(
                String.valueOf(player.getId()),
                "/topic/game/" + game.getId() + "/dice",
                new DiceRollDTO(String.valueOf(player.getId()), player.getDice().stream().map(Dice::getValue).collect(Collectors.toList()))
            );
        });
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
        GameStateDTO gameState = gameService.processBid(gameId, String.valueOf(request.getPlayerId()), 
            request.getQuantity(), request.getValue());
            
        // Broadcast new game state
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", 
            gameState);
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
    }

    @MessageMapping("/game/{gameId}/state")
    public void getGameState(@DestinationVariable String gameId, Principal principal) {
        Game game = gameService.getGame(gameId);
        int playerId = getUserId(principal);
        
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

    private int getUserId(Principal principal) {
        // Implementation depends on your authentication system
        // This is a placeholder - implement according to your needs
        return Integer.parseInt(principal.getName());
    }
}