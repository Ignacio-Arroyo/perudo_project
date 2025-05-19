package perudo_backend.perudo_backend.dto;

import perudo_backend.perudo_backend.Game;
import perudo_backend.perudo_backend.GameStatus;
import java.util.List;
import java.util.stream.Collectors;

public class GameStateDTO {
    private String id; // This will store the gameId
    private GameStatus status;
    private List<GamePlayerDTO> players;
    private BidDTO currentBid;
    private String currentPlayerId;
    private int round;
    private String errorMessage;
    private List<GamePlayerDTO> turnSequence;

    public GameStateDTO(Game game, String requestingPlayerId) {
        if (game.getGameId() == null) {
            throw new IllegalStateException("Game ID cannot be null");
        }
        this.id = game.getGameId();
        this.players = game.getPlayers().stream()
            .map(player -> new GamePlayerDTO(player, 
                String.valueOf(player.getId()).equals(requestingPlayerId)))
            .collect(Collectors.toList());
        this.currentBid = game.getCurrentBid() != null ? new BidDTO(game.getCurrentBid()) : null;
        this.currentPlayerId = game.getCurrentPlayer() != null ? 
            String.valueOf(game.getCurrentPlayer().getId()) : null;
        this.round = game.getRound();
        this.status = game.getStatus();
        
        if (game.getTurnSequence() != null) {
            this.turnSequence = game.getTurnSequence().stream()
                .map(player -> new GamePlayerDTO(player, 
                    String.valueOf(player.getId()).equals(requestingPlayerId)))
                .collect(Collectors.toList());
        }
    }

    public GameStateDTO(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        if (game.getGameId() == null || game.getGameId().equals("null")) {
            throw new IllegalArgumentException("Game must have a valid ID");
        }
        
        this.id = game.getGameId();
        this.players = game.getPlayers().stream()
            .map(player -> new GamePlayerDTO(player, false))
            .collect(Collectors.toList());
        this.currentBid = game.getCurrentBid() != null ? new BidDTO(game.getCurrentBid()) : null;
        this.currentPlayerId = game.getCurrentPlayer() != null ? 
            String.valueOf(game.getCurrentPlayer().getId()) : null;
        this.round = game.getRound();
        this.status = game.getStatus();
        
        if (game.getTurnSequence() != null) {
            this.turnSequence = game.getTurnSequence().stream()
                .map(player -> new GamePlayerDTO(player, false))
                .collect(Collectors.toList());
        }
    }

    // Add new constructor for error states
    public GameStateDTO(String gameId, GameStatus status, String errorMessage) {
        this.id = gameId;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    // Default constructor
    public GameStateDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.equals("null")) {
            throw new IllegalArgumentException("Invalid game ID");
        }
        this.id = id;
    }

    public List<GamePlayerDTO> getPlayers() { return players; }
    public void setPlayers(List<GamePlayerDTO> players) { this.players = players; }

    public BidDTO getCurrentBid() { return currentBid; }
    public void setCurrentBid(BidDTO currentBid) { this.currentBid = currentBid; }

    public String getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(String currentPlayerId) { this.currentPlayerId = currentPlayerId; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<GamePlayerDTO> getTurnSequence() { return turnSequence; }
    public void setTurnSequence(List<GamePlayerDTO> turnSequence) { this.turnSequence = turnSequence; }

    // Add toString for debugging
    @Override
    public String toString() {
        return "GameStateDTO{" +
            "id='" + id + '\'' +
            ", status=" + status +
            ", players=" + players +
            ", currentPlayerId='" + currentPlayerId + '\'' +
            ", round=" + round +
            '}';
    }
}

