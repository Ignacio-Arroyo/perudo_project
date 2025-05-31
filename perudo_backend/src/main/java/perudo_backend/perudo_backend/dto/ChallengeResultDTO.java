package perudo_backend.perudo_backend.dto;

import java.util.List;
import java.util.Map;

public class ChallengeResultDTO {
    private String challengerId;
    private String challengerName;
    private String bidPlayerId;
    private String bidPlayerName;
    private BidDTO challengedBid;
    private int actualCount;
    private boolean challengeSuccessful;
    private String losingPlayerId;
    private String losingPlayerName;
    private Map<String, List<Integer>> allPlayerDice; // playerId -> dice values
    private boolean gameFinished;
    private String winnerId;
    private String winnerName;

    public ChallengeResultDTO() {}

    public ChallengeResultDTO(String challengerId, String challengerName, String bidPlayerId, String bidPlayerName,
                             BidDTO challengedBid, int actualCount, boolean challengeSuccessful,
                             String losingPlayerId, String losingPlayerName, Map<String, List<Integer>> allPlayerDice,
                             boolean gameFinished, String winnerId, String winnerName) {
        this.challengerId = challengerId;
        this.challengerName = challengerName;
        this.bidPlayerId = bidPlayerId;
        this.bidPlayerName = bidPlayerName;
        this.challengedBid = challengedBid;
        this.actualCount = actualCount;
        this.challengeSuccessful = challengeSuccessful;
        this.losingPlayerId = losingPlayerId;
        this.losingPlayerName = losingPlayerName;
        this.allPlayerDice = allPlayerDice;
        this.gameFinished = gameFinished;
        this.winnerId = winnerId;
        this.winnerName = winnerName;
    }

    // Getters and setters
    public String getChallengerId() { return challengerId; }
    public void setChallengerId(String challengerId) { this.challengerId = challengerId; }

    public String getChallengerName() { return challengerName; }
    public void setChallengerName(String challengerName) { this.challengerName = challengerName; }

    public String getBidPlayerId() { return bidPlayerId; }
    public void setBidPlayerId(String bidPlayerId) { this.bidPlayerId = bidPlayerId; }

    public String getBidPlayerName() { return bidPlayerName; }
    public void setBidPlayerName(String bidPlayerName) { this.bidPlayerName = bidPlayerName; }

    public BidDTO getChallengedBid() { return challengedBid; }
    public void setChallengedBid(BidDTO challengedBid) { this.challengedBid = challengedBid; }

    public int getActualCount() { return actualCount; }
    public void setActualCount(int actualCount) { this.actualCount = actualCount; }

    public boolean isChallengeSuccessful() { return challengeSuccessful; }
    public void setChallengeSuccessful(boolean challengeSuccessful) { this.challengeSuccessful = challengeSuccessful; }

    public String getLosingPlayerId() { return losingPlayerId; }
    public void setLosingPlayerId(String losingPlayerId) { this.losingPlayerId = losingPlayerId; }

    public String getLosingPlayerName() { return losingPlayerName; }
    public void setLosingPlayerName(String losingPlayerName) { this.losingPlayerName = losingPlayerName; }

    public Map<String, List<Integer>> getAllPlayerDice() { return allPlayerDice; }
    public void setAllPlayerDice(Map<String, List<Integer>> allPlayerDice) { this.allPlayerDice = allPlayerDice; }

    public boolean isGameFinished() { return gameFinished; }
    public void setGameFinished(boolean gameFinished) { this.gameFinished = gameFinished; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }

    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
} 