package perudo_backend.perudo_backend;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Random;
import java.util.ArrayList;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int player_id;
    String nom;
    String prenom;
    String username;
    String password;
    private String friendCode; // Code ami
    private int winRate; // Taux de victoire
    private int pieces;
    private int equippedProduct;
    private int trophies; // Nombre de trophées

    @ManyToOne
    @JoinColumn(name = "game_id") // Foreign key column in the Player table
    private Game game;

    @ManyToMany
    @JoinTable(
        name = "player_dice",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "dice_id")
    )
    private Collection<Dice> ownedDice = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "player_friends",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Collection<Player> friends = new ArrayList<>(); // Liste d'amis

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<GameRecord> gameRecords = new ArrayList<>(); // Enregistrement des parties jouées

    @ManyToMany
    @JoinTable(
        name = "player_inventory",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Collection<Product> inventory = new ArrayList<>();

    public Player() {
        this.pieces = 2000;
        this.winRate = 0;
        this.equippedProduct = 0;
        this.friendCode = "";
        this.trophies = 0;
    }

    public Player(String nom, String prenom, String username, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.password = password;
        this.friendCode = generateFriendCode();
        this.pieces = 2000;
        this.equippedProduct = 0;
        this.winRate = 0;
        this.trophies = 0;
    }

    public Player(String nom, String prenom, String username, String password, String friendCode, int pieces, int equippedProduct) {
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.password = password;
        this.friendCode = friendCode;
        this.pieces = pieces;
        this.equippedProduct = equippedProduct;
        this.winRate = 0;
        this.trophies = 0;
    }

    @PrePersist
    private void ensureFriendCode() {
        if (this.friendCode == null || this.friendCode.isEmpty()) {
            this.friendCode = generateFriendCode();
        }
    }

    private String generateFriendCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder friendCode = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            friendCode.append(characters.charAt(index));
        }
        return friendCode.toString();
    }
 
    public Collection<Product> getInventory() {
        return inventory;
    }

    public void setInventory(Collection<Product> inventory) {
        this.inventory = inventory;
    }

    public int getEquippedProduct() {
        return equippedProduct;
    }

    public void setEquippedProduct(int equippedProduct) {
        this.equippedProduct = equippedProduct;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public int getId() {
        return player_id;
    }

    public void setId(int id) {
        this.player_id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Collection<Dice> getDices() {
        return ownedDice;
    }

    public void setDices(Collection<Dice> dices) {
        this.ownedDice = dices;
    }

    public String getFriendCode() {
        return friendCode;
    }

    public void setFriendCode(String friendCode) {
        this.friendCode = friendCode;
    }

    public int getWinRate() {
        return winRate;
    }

    public void setWinRate(int winRate) {
        this.winRate = winRate;
    }

    public Collection<Player> getFriends() {
        return friends;
    }

    public void setFriends(Collection<Player> friends) {
        this.friends = friends;
    }

    public Collection<GameRecord> getGameRecords() {
        return gameRecords;
    }

    public void setGameRecords(Collection<GameRecord> gameRecords) {
        this.gameRecords = gameRecords;
    }

    public int getTrophies() {
        return trophies;
    }

    public void setTrophies(int trophies) {
        this.trophies = trophies;
    }

    public void addTrophies(int amount) {
        this.trophies += amount;
        if (this.trophies < 0) {
            this.trophies = 0;
        }
    }

    @Override
    public String toString() {
        return "Player{" +
                "player_id=" + player_id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", username='" + username + '\'' +
                ", password='" + "[PROTECTED]" + '\'' + // Avoid logging password
                ", friendCode='" + friendCode + '\'' +
                ", winRate=" + winRate +
                ", pieces=" + pieces +
                ", trophies=" + trophies +
                ", equippedProduct=" + equippedProduct +
                ", game=" + (game != null ? game.getId() : null) +
                ", ownedDice_count=" + (ownedDice != null ? ownedDice.size() : 0) +
                ", friends_count=" + (friends != null ? friends.size() : 0) +
                ", gameRecords_count=" + (gameRecords != null ? gameRecords.size() : 0) +
                ", inventory_count=" + (inventory != null ? inventory.size() : 0) +
                '}';
    }
}
