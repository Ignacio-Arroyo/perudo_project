package perudo_backend.perudo_backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_player_id")
    private Player fromPlayer;

    @ManyToOne
    @JoinColumn(name = "to_player_id")
    private Player toPlayer;

    private String status; // e.g., "pending", "accepted", "rejected"

    private LocalDateTime createdAt;

    // Constructors, getters, and setters
    public FriendRequest() {
        this.createdAt = LocalDateTime.now();
    }

    public FriendRequest(Player fromPlayer, Player toPlayer) {
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getFromPlayer() {
        return fromPlayer;
    }

    public void setFromPlayer(Player fromPlayer) {
        this.fromPlayer = fromPlayer;
    }

    public Player getToPlayer() {
        return toPlayer;
    }

    public void setToPlayer(Player toPlayer) {
        this.toPlayer = toPlayer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
