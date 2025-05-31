package perudo_backend.perudo_backend;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import perudo_backend.perudo_backend.Product;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "players")
@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class, 
  property = "id")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String username;
    private String password;
    private boolean currentTurn;
    private double winRate;
    private String friendCode;

    private boolean hasRolled;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dice> dice = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToMany(mappedBy = "winner", fetch = FetchType.LAZY)
    private List<Game> wonGames = new ArrayList<>();

    @OneToMany(mappedBy = "currentPlayer", fetch = FetchType.LAZY)
    private List<Game> currentGames = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "player_friends",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<Player> friends = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameRecord> gameRecords = new ArrayList<>();

    // --- AJOUTS POUR COMPATIBILITÉ ---
    private int pieces;
    private int trophies;
    @OneToMany
    private List<Product> inventory = new ArrayList<>();
    private Integer equippedProduct;
    
    // --- GAME ACTIVITY TRACKING ---
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private int totalChallenges = 0;
    private int successfulChallenges = 0;
    private int playersEliminated = 0;
    private int points = 0;
    
    // --- TRANSIENT FIELDS FOR CURRENT GAME ---
    @Transient
    private int currentGameChallenges = 0;
    @Transient 
    private int currentGameSuccessfulChallenges = 0;
    @Transient
    private int currentGameEliminatedPlayers = 0;
    @Transient
    private int finalPosition = 0; // 1st, 2nd, 3rd place etc.

    // Constructors
    public Player() {
        this.pieces = 2000;
        this.trophies = 0;
        this.winRate = 0;
        this.dice = new ArrayList<>();
        this.friends = new HashSet<>();
        this.gameRecords = new ArrayList<>();
        this.inventory = new ArrayList<>();
        this.equippedProduct = null;
    }

    public Player(String nom, String prenom, String username, String password) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.password = password;
        this.friendCode = generateFriendCode();
    }

    public Player(Long id) {
        this();
        this.id = id;
        this.username = "Player " + id;
        this.currentTurn = false;
        this.friendCode = generateFriendCode();
    }

    // Player.java
    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", username='" + username + '\'' +
                ", friendCode='" + friendCode + '\'' +
                ", winRate=" + winRate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        if (id == null) {
            return false; 
        }
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFriendCode() {
        return friendCode;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public Set<Player> getFriends() {
        return friends;
    }

    public void setFriends(Set<Player> friends) {
        this.friends = friends;
    }

    public List<GameRecord> getGameRecords() {
        return gameRecords;
    }

    public void setGameRecords(List<GameRecord> gameRecords) {
        this.gameRecords = gameRecords;
    }

    public List<Dice> getDice() {
        return dice;
    }

    public void setDice(List<Dice> dice) {
        this.dice = dice;
    }

    public int getDiceCount() {
        return dice != null ? dice.size() : 0;
    }

    public boolean isCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(boolean currentTurn) {
        this.currentTurn = currentTurn;
    }

    public boolean getHasRolled() {
        return hasRolled;
    }

    public void setHasRolled(boolean hasRolled) {
        this.hasRolled = hasRolled;
    }

    // Helper methods for managing relationships
    public void addFriend(Player friend) {
        this.friends.add(friend);
        friend.getFriends().add(this);
    }

    public void removeFriend(Player friend) {
        this.friends.remove(friend);
        friend.getFriends().remove(this);
    }

    public void addGameRecord(GameRecord gameRecord) {
        if (!this.gameRecords.contains(gameRecord)) {
            this.gameRecords.add(gameRecord);
            gameRecord.setPlayer(this);
        }
    }

    public void removeGameRecord(GameRecord gameRecord) {
        if (this.gameRecords.remove(gameRecord)) {
            gameRecord.setPlayer(null);
        }
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public List<Game> getCurrentGames() {
        return currentGames;
    }

    public void setCurrentGames(List<Game> currentGames) {
        this.currentGames = currentGames;
    }

    public List<Game> getWonGames() {
        return wonGames;
    }

    public void setWonGames(List<Game> wonGames) {
        this.wonGames = wonGames;
    }

    // Add helper methods for managing dice
    public void addDice(Dice dice) {
        this.dice.add(dice);
        dice.setPlayer(this);
    }

    public void removeDice(Dice dice) {
        this.dice.remove(dice);
        dice.setPlayer(null);
    }

    // Getter/setter pour pieces
    public int getPieces() { return pieces; }
    public void setPieces(int pieces) { this.pieces = pieces; }

    // Getter/setter pour trophies
    public int getTrophies() { return trophies; }
    public void setTrophies(int trophies) { this.trophies = trophies; }
    public void addTrophies(int amount) { this.trophies += amount; }

    // Getter/setter pour inventory
    public List<Product> getInventory() { return inventory; }
    public void setInventory(List<Product> inventory) { this.inventory = inventory; }

    // Getter/setter pour equippedProduct
    public Integer getEquippedProduct() { return equippedProduct; }
    public void setEquippedProduct(Integer equippedProduct) { this.equippedProduct = equippedProduct; }

    // --- GAME ACTIVITY TRACKING ---
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public void setGamesWon(int gamesWon) { this.gamesWon = gamesWon; }
    public int getTotalChallenges() { return totalChallenges; }
    public void setTotalChallenges(int totalChallenges) { this.totalChallenges = totalChallenges; }
    public int getSuccessfulChallenges() { return successfulChallenges; }
    public void setSuccessfulChallenges(int successfulChallenges) { this.successfulChallenges = successfulChallenges; }
    public int getPlayersEliminated() { return playersEliminated; }
    public void setPlayersEliminated(int playersEliminated) { this.playersEliminated = playersEliminated; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    // --- TRANSIENT FIELDS FOR CURRENT GAME ---
    public int getCurrentGameChallenges() { return currentGameChallenges; }
    public void setCurrentGameChallenges(int currentGameChallenges) { this.currentGameChallenges = currentGameChallenges; }
    public int getCurrentGameSuccessfulChallenges() { return currentGameSuccessfulChallenges; }
    public void setCurrentGameSuccessfulChallenges(int currentGameSuccessfulChallenges) { this.currentGameSuccessfulChallenges = currentGameSuccessfulChallenges; }
    public int getCurrentGameEliminatedPlayers() { return currentGameEliminatedPlayers; }
    public void setCurrentGameEliminatedPlayers(int currentGameEliminatedPlayers) { this.currentGameEliminatedPlayers = currentGameEliminatedPlayers; }
    public int getFinalPosition() { return finalPosition; }
    public void setFinalPosition(int finalPosition) { this.finalPosition = finalPosition; }
}
