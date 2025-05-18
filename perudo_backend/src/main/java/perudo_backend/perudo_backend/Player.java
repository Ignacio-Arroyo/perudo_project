package perudo_backend.perudo_backend;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "players")
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
    private List<Player> friends = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameRecord> gameRecords = new ArrayList<>();

    // Constructors
    public Player() {
        this.winRate = 0;
        this.dice = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.gameRecords = new ArrayList<>();
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

    public List<Player> getFriends() {
        return friends;
    }

    public void setFriends(List<Player> friends) {
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
        if (!this.friends.contains(friend)) {
            this.friends.add(friend);
            if (!friend.getFriends().contains(this)) {
                friend.getFriends().add(this);
            }
        }
    }

    public void removeFriend(Player friend) {
        if (this.friends.remove(friend)) {
            friend.getFriends().remove(this);
        }
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
}
