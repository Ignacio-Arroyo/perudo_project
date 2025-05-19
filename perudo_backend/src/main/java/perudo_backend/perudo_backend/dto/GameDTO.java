package perudo_backend.perudo_backend.dto;

import java.util.List;
import java.util.stream.Collectors;

import perudo_backend.perudo_backend.Game;

public class GameDTO {
    private String id; // Make sure this matches Game entity
    private List<GamePlayerDTO> players;
    private String status;

    // Update constructor to use GamePlayerDTO
    public GameDTO(Game game) {
        this.id = String.valueOf(game.getId());
        this.players = game.getPlayers().stream()
            .map(player -> new GamePlayerDTO(player, false))
            .collect(Collectors.toList());
        this.status = game.getStatus().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<GamePlayerDTO> getPlayers() { return players; }
    public void setPlayers(List<GamePlayerDTO> players) { this.players = players; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
