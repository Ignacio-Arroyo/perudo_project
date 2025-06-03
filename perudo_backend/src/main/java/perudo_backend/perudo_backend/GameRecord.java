package perudo_backend.perudo_backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_records", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "game_id"}))
public class GameRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(nullable = false)
    private LocalDateTime playedAt;

    @Column(nullable = false)
    private boolean won;

    @Column(nullable = false)
    private int scoreChange;

    // Constructeurs
    public GameRecord() {
    }

    public GameRecord(Player player, String gameId, LocalDateTime playedAt, boolean won, int scoreChange) {
        this.player = player;
        this.gameId = gameId;
        this.playedAt = playedAt;
        this.won = won;
        this.scoreChange = scoreChange;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
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
}

