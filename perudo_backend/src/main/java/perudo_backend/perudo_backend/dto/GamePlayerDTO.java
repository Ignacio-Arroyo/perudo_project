package perudo_backend.perudo_backend.dto;

import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.Dice;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamePlayerDTO {
    private static final Logger log = LoggerFactory.getLogger(GamePlayerDTO.class);
    
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
            
        // Récupérer et définir clairement si c'est le tour de ce joueur
        this.currentTurn = player.isCurrentTurn();
        this.isCurrentPlayer = isCurrentPlayer;
        
        // Ajouter un log pour le debug
        log.debug("Created GamePlayerDTO - Player ID: {}, Username: {}, currentTurn: {}, isCurrentPlayer: {}", 
            this.id, this.username, this.currentTurn, this.isCurrentPlayer);
    }

    public GamePlayerDTO(Player player) {
        this(player, false);
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
    
    @Override
    public String toString() {
        return "GamePlayerDTO{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", dice=" + dice +
                ", currentTurn=" + currentTurn +
                ", isCurrentPlayer=" + isCurrentPlayer +
                '}';
    }
}