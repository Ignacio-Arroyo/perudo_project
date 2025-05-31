package perudo_backend.perudo_backend.dto;

import java.util.List;

public class GameEndResultDTO {
    private String gameId;
    private boolean gameFinished;
    private List<PlayerGameResult> playerResults;
    
    public static class PlayerGameResult {
        private Long playerId;
        private String username;
        private int finalPosition;
        private int pointsEarned;
        private int coinsEarned;
        private int totalChallenges;
        private int successfulChallenges;
        private int playersEliminated;
        private int diceRemaining;
        private String performanceMessage;
        
        // Constructors
        public PlayerGameResult() {}
        
        public PlayerGameResult(Long playerId, String username, int finalPosition, 
                              int pointsEarned, int coinsEarned, int totalChallenges, 
                              int successfulChallenges, int playersEliminated, 
                              int diceRemaining) {
            this.playerId = playerId;
            this.username = username;
            this.finalPosition = finalPosition;
            this.pointsEarned = pointsEarned;
            this.coinsEarned = coinsEarned;
            this.totalChallenges = totalChallenges;
            this.successfulChallenges = successfulChallenges;
            this.playersEliminated = playersEliminated;
            this.diceRemaining = diceRemaining;
        }
        
        // Getters and setters
        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public int getFinalPosition() { return finalPosition; }
        public void setFinalPosition(int finalPosition) { this.finalPosition = finalPosition; }
        
        public int getPointsEarned() { return pointsEarned; }
        public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }
        
        public int getCoinsEarned() { return coinsEarned; }
        public void setCoinsEarned(int coinsEarned) { this.coinsEarned = coinsEarned; }
        
        public int getTotalChallenges() { return totalChallenges; }
        public void setTotalChallenges(int totalChallenges) { this.totalChallenges = totalChallenges; }
        
        public int getSuccessfulChallenges() { return successfulChallenges; }
        public void setSuccessfulChallenges(int successfulChallenges) { this.successfulChallenges = successfulChallenges; }
        
        public int getPlayersEliminated() { return playersEliminated; }
        public void setPlayersEliminated(int playersEliminated) { this.playersEliminated = playersEliminated; }
        
        public int getDiceRemaining() { return diceRemaining; }
        public void setDiceRemaining(int diceRemaining) { this.diceRemaining = diceRemaining; }
        
        public String getPerformanceMessage() { return performanceMessage; }
        public void setPerformanceMessage(String performanceMessage) { this.performanceMessage = performanceMessage; }
    }
    
    // Constructors
    public GameEndResultDTO() {}
    
    public GameEndResultDTO(String gameId, boolean gameFinished, List<PlayerGameResult> playerResults) {
        this.gameId = gameId;
        this.gameFinished = gameFinished;
        this.playerResults = playerResults;
    }
    
    // Getters and setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    
    public boolean isGameFinished() { return gameFinished; }
    public void setGameFinished(boolean gameFinished) { this.gameFinished = gameFinished; }
    
    public List<PlayerGameResult> getPlayerResults() { return playerResults; }
    public void setPlayerResults(List<PlayerGameResult> playerResults) { this.playerResults = playerResults; }
} 