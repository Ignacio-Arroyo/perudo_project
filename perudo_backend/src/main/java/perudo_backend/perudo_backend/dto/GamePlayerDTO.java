package perudo_backend.perudo_backend.dto;

import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.Dice;

import java.util.List;
import java.util.stream.Collectors;

public class GamePlayerDTO {
    private String id;
    private String username;
    private List<Integer> dice;
    private boolean currentTurn;
    private boolean isCurrentPlayer;

    public GamePlayerDTO(Player player, boolean isCurrentPlayer) {
        this.id = String.valueOf(player.getId());
        this.username = player.getUsername();
        this.dice = player.getDice().stream()
            .map(Dice::getValue)
            .collect(Collectors.toList());
        this.currentTurn = player.isCurrentTurn();
        this.isCurrentPlayer = isCurrentPlayer;
    }

    public GamePlayerDTO(Player player) {
        this.id = String.valueOf(player.getId());
        this.username = player.getUsername();
        this.dice = player.getDice().stream()
            .map(Dice::getValue)
            .collect(Collectors.toList());
        this.currentTurn = player.isCurrentTurn();
        this.isCurrentPlayer = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Integer> getDice() {
        return dice;
    }

    public void setDice(List<Integer> dice) {
        this.dice = dice;
    }

    public boolean isCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(boolean currentTurn) {
        this.currentTurn = currentTurn;
    }

    public boolean isCurrentPlayer() {
        return isCurrentPlayer;
    }

    public void setCurrentPlayer(boolean currentPlayer) {
        isCurrentPlayer = currentPlayer;
    }
}