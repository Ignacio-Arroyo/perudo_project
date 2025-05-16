package perudo_backend.perudo_backend.dto;

import perudo_backend.perudo_backend.Player;

public class FriendDTO {
    private int player_id;
    private String username;
    private String friendCode;

    // Constructor
    public FriendDTO(Player player) {
        this.player_id = player.getId();
        this.username = player.getUsername();
        this.friendCode = player.getFriendCode();
    }

    // Getters and setters
    public int getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(int player_id) {
        this.player_id = player_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFriendCode() {
        return friendCode;
    }

    public void setFriendCode(String friendCode) {
        this.friendCode = friendCode;
    }
}
