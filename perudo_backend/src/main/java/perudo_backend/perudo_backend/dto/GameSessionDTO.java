package perudo_backend.perudo_backend.dto;

import perudo_backend.perudo_backend.GameSession;

public class GameSessionDTO {
    private Long id;
    private String player1Username;
    private String player2Username;
    private String winner;

    public GameSessionDTO(GameSession gameSession) {
        this.id = gameSession.getId();
        this.player1Username = gameSession.getPlayer1().getUsername();
        this.player2Username = gameSession.getPlayer2().getUsername();
        this.winner = gameSession.getWinner();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}
