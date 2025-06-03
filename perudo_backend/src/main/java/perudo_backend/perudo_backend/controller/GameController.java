package perudo_backend.perudo_backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import perudo_backend.perudo_backend.Game;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.dto.*;
import perudo_backend.perudo_backend.dto.RollDiceRequest;
import perudo_backend.perudo_backend.dto.GameEndResultDTO;
import perudo_backend.perudo_backend.services.*;
import perudo_backend.perudo_backend.repositories.PlayerRepository;
import perudo_backend.exception.PlayerNotFoundException;
import perudo_backend.exception.NotYourTurnException;
import perudo_backend.perudo_backend.Dice;
import perudo_backend.perudo_backend.GameStatus;
import perudo_backend.exception.NotEnoughPlayersException;
import perudo_backend.exception.GameNotFoundException;
import perudo_backend.exception.GameFullException;

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
    
    @Autowired
    private PlayerRepository playerRepository;
    
    // Store challenge results temporarily
    private Map<String, ChallengeResultDTO> challengeResults = new ConcurrentHashMap<>();

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
        log.info("Attempting to join game: gameId='{}', playerId='{}'", gameId, playerId);
        try {
            GameStateDTO gameState = gameService.joinGame(gameId, playerId);
            log.info("Successfully joined game: gameId='{}', playerId='{}'. Response: {}", gameId, playerId, gameState);
            return ResponseEntity.ok(gameState);
        } catch (PlayerNotFoundException pnfe) {
            log.warn("Player not found while trying to join game: gameId='{}', playerId='{}'. Error: {}", gameId, playerId, pnfe.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or a DTO with error
        } catch (GameNotFoundException gnfe) {
            log.warn("Game not found while trying to join game: gameId='{}', playerId='{}'. Error: {}", gameId, playerId, gnfe.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or a DTO with error
        } catch (GameFullException gfe) {
            log.warn("Game full while trying to join game: gameId='{}', playerId='{}'. Error: {}", gameId, playerId, gfe.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Or a DTO with error
        } catch (Exception e) {
            log.error("Unexpected error while joining game: gameId='{}', playerId='{}'", gameId, playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Or a DTO with error
        }
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
        } catch (IllegalArgumentException | NotYourTurnException e) { // Catch specific, expected exceptions
            log.warn("Invalid bid or turn error for game {}: {}", gameId, e.getMessage());
            GameStateDTO currentActualState = gameService.getGameState(gameId); // Get the true current state
            currentActualState.setErrorMessage(e.getMessage());
            // Optionally, set a specific status if needed, but GameService might not have changed it yet
            // currentActualState.setStatus(GameStatus.ERROR); // Or keep the game's actual current status
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", currentActualState);
        } catch (Exception e) { // Catch other unexpected exceptions
            log.error("Unexpected error processing bid for game {}: ", gameId, e);
            // For unexpected errors, sending a minimal error DTO might still be appropriate
            // or try to get current state if possible, but it might be corrupted.
            GameStateDTO errorState = new GameStateDTO(
                gameId,
                GameStatus.ERROR,
                "An unexpected server error occurred while processing your bid."
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

    @PostMapping("/{gameId}/next-round")
    public ResponseEntity<GameStateDTO> startNextRound(
            @PathVariable String gameId,
            @RequestBody Map<String, String> request) {
        String losingPlayerId = request.get("losingPlayerId");
        GameStateDTO gameState = gameService.startNextRoundAfterChallenge(gameId, losingPlayerId);
        
        // Send updated dice to each player after the new round starts
        Game game = gameService.getGame(gameId);
        for (Player player : game.getPlayers()) {
            if (player.getDice() != null && !player.getDice().isEmpty()) {
                DiceRollDTO diceRoll = new DiceRollDTO(
                    String.valueOf(player.getId()),
                    player.getDice().stream()
                        .map(Dice::getValue)
                        .collect(Collectors.toList())
                );
                
                messagingTemplate.convertAndSendToUser(
                    String.valueOf(player.getId()),
                    "/queue/game/" + gameId + "/dice",
                    diceRoll
                );
                
                log.info("Sent updated dice to player {} after challenge: {}", 
                    player.getId(), diceRoll.getValues());
            }
        }
        
        // Also broadcast the updated game state
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", gameState);
        
        return ResponseEntity.ok(gameState);
    }

    @MessageMapping("/game/{gameId}/challenge")
    public void challenge(@DestinationVariable String gameId, ChallengeRequest request) {
        try {
            log.info("Processing challenge via WebSocket: gameId={}, challengerId={}",
                gameId, request.getPlayerId());
                
            GameStateDTO gameState = gameService.processChallenge(gameId, String.valueOf(request.getPlayerId()));
            
            // Get the detailed challenge result
            ChallengeResultDTO challengeResult = gameService.getChallengeResult(gameId);
            
            log.info("Challenge processed successfully for game {}", gameId);
            
            // First, broadcast the challenge result with all revealed dice
            if (challengeResult != null) {
                messagingTemplate.convertAndSend("/topic/game/" + gameId + "/challenge", challengeResult);
                log.info("Challenge result broadcasted: {} vs {}, actual count: {}, challenge {}",
                    challengeResult.getChallengerName(), challengeResult.getBidPlayerName(),
                    challengeResult.getActualCount(), 
                    challengeResult.isChallengeSuccessful() ? "SUCCESSFUL" : "FAILED");
            }
            
            // Then broadcast updated game state to all players
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", gameState);
            
            // Clear the challenge result after broadcasting
            gameService.clearChallengeResult(gameId);
            
            // If the game is finished, broadcast the final result
            if (gameState.getStatus() == GameStatus.FINISHED) {
                log.info("Game {} finished after challenge. Triggering end results calculation.", gameId);
                // Game is finished, calculate and broadcast final results
                // No need to pass players in request body anymore as GameService uses game.getOriginalPlayers()
                GameEndResultDTO endResults = gameService.calculateGameEndResults(gameId);
                if (endResults != null) {
                    messagingTemplate.convertAndSend("/topic/game/" + gameId + "/results", endResults);
                    log.info("Game end results broadcasted for game {}", gameId);
                } else {
                    log.warn("Failed to calculate or broadcast end results for game {}", gameId);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing challenge: ", e);
            GameStateDTO errorState = new GameStateDTO(
                gameId,
                GameStatus.ERROR,
                e.getMessage()
            );
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", errorState);
        }
    }

    @MessageMapping("/game/{gameId}/state")
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
    
    @PostMapping("/{gameId}/end-results")
    public ResponseEntity<GameEndResultDTO> triggerGameEndResults(@PathVariable String gameId) {
        // This endpoint can now be simplified if the frontend doesn't need to send player stats,
        // as GameService will use game.getOriginalPlayers()
        // However, the current GameService.calculateGameEndResults still uses playerRepository.findById
        // and copies stats from originalPlayerInstance to managedPlayer. 
        // The key is that originalPlayerInstance comes from game.getOriginalPlayers() which has updated stats.

        // For now, this endpoint can be a manual trigger if needed, or called by the frontend
        // when it observes GameStatus.FINISHED, without a complex body.
        try {
            log.info("Received request to calculate end results for game: {}", gameId);
            Game game = gameService.findById(gameId);
            if (game.getStatus() != GameStatus.FINISHED) {
                log.warn("Attempt to calculate results for game {} that is not FINISHED. Status: {}", gameId, game.getStatus());
                // Optionally return a specific response if game is not finished
                // For now, proceed and let GameService handle it (it also checks status)
            }

            GameEndResultDTO results = gameService.calculateGameEndResults(gameId);
            
            if (results == null) {
                log.warn("Could not calculate end results for game {} (service returned null)", gameId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Or NOT_FOUND if game finished but no results
            }
            
            // Broadcasting should ideally happen once, perhaps after processChallenge if that's what finishes the game.
            // If this endpoint is just for explicit calculation, maybe broadcasting here is fine too or redundant.
            // The processChallenge block above now handles broadcasting /results if game is finished.
            log.info("Game end results calculated for game {}: {} players ranked. Results will be/were broadcasted.", gameId, results.getPlayerResults().size());
            return ResponseEntity.ok(results);
            
        } catch (GameNotFoundException e) {
            log.error("Game not found when trying to calculate end results for game {}: {}", gameId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error calculating game end results for game {}: {}", gameId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}