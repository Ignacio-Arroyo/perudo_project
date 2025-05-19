package perudo_backend.perudo_backend.dto;

public class RollDiceRequest {
    private String gameId;
    private String playerId;

    // Default constructor
    public RollDiceRequest() {
    }

    // Constructor with fields
    public RollDiceRequest(String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
    }

    // Getters and setters
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public String toString() {
        return "RollDiceRequest{" +
                "gameId='" + gameId + '\'' +
                ", playerId='" + playerId + '\'' +
                '}';
    }
}