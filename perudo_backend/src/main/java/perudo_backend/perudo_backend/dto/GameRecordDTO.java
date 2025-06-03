package perudo_backend.perudo_backend.dto;

import java.time.LocalDateTime;

public class GameRecordDTO {
    private Long id;
    private LocalDateTime playedAt;
    private boolean won;
    private int scoreChange;
    private String gameId; // On pourrait ajouter l'ID du jeu si disponible
    private int finalPosition; // Position finale dans le jeu (1er, 2ème, etc.)

    public GameRecordDTO() {
    }

    public GameRecordDTO(Long id, LocalDateTime playedAt, boolean won, int scoreChange) {
        this.id = id;
        this.playedAt = playedAt;
        this.won = won;
        this.scoreChange = scoreChange;
        this.gameId = null; // Pour les anciens records sans gameId
    }

    public GameRecordDTO(Long id, LocalDateTime playedAt, boolean won, int scoreChange, String gameId) {
        this.id = id;
        this.playedAt = playedAt;
        this.won = won;
        this.scoreChange = scoreChange;
        this.gameId = gameId;
        this.finalPosition = 0; // Valeur par défaut
    }

    public GameRecordDTO(Long id, LocalDateTime playedAt, boolean won, int scoreChange, String gameId, int finalPosition) {
        this.id = id;
        this.playedAt = playedAt;
        this.won = won;
        this.scoreChange = scoreChange;
        this.gameId = gameId;
        this.finalPosition = finalPosition;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public int getScoreChange() {
        return scoreChange;
    }

    public void setScoreChange(int scoreChange) {
        this.scoreChange = scoreChange;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getFinalPosition() {
        return finalPosition;
    }

    public void setFinalPosition(int finalPosition) {
        this.finalPosition = finalPosition;
    }
} 