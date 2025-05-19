package perudo_backend.perudo_backend.dto;
import perudo_backend.perudo_backend.Player;


public class PlayerDTO {
    private Long id;  // Changed from int to Long
    private String nom;
    private String prenom;
    private String username;
    private String friendCode;
    private double winRate;
    private int trophies;
    private int pieces;

    // Constructor
    public PlayerDTO(Player player) {
        this.id = player.getId();
        this.nom = player.getNom();
        this.prenom = player.getPrenom();
        this.username = player.getUsername();
        this.friendCode = player.getFriendCode();
        this.winRate = player.getWinRate();
        this.trophies = player.getTrophies();
        this.pieces = player.getPieces();
    }

        // Override toString() method
    @Override
    public String toString() {
        return "PlayerDTO{" +
                "player_id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", username='" + username + '\'' +
                ", friendCode='" + friendCode + '\'' +
                ", winRate=" + winRate +
                ", trophies=" + trophies +
                ", pieces=" + pieces +
                '}';
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public int getTrophies() {
        return trophies;
    }

    public void setTrophies(int trophies) {
        this.trophies = trophies;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }
}
