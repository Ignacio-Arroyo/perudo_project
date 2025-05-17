package perudo_backend.perudo_backend.dto;

import perudo_backend.perudo_backend.Player;

public class FriendDTO {
    private Long id;  // Changed from int to Long
    private String username;
    private String friendCode;

    // Constructor
    public FriendDTO(Player player) {
        this.id = player.getId();
        this.username = player.getUsername();
        this.friendCode = player.getFriendCode();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
