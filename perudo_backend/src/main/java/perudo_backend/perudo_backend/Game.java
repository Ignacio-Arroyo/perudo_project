package perudo_backend.perudo_backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.OrderColumn;


@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String gameId;
    
    @Column(nullable = false)
    private String name;
    
    private int nbPlayers;
    private int round;
    
    @Enumerated(EnumType.STRING)
    private GameStatus status;

    // Add the player field to match the mapping in Player entity
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Player> players = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_player_id")
    private Player currentPlayer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_id")
    private Player winner;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_bid_id")
    private Bid currentBid;

    @OneToMany(fetch = FetchType.EAGER)
    @OrderColumn
    private List<Player> turnSequence = new ArrayList<>();
    
    private int currentTurnIndex = 0;

    public Game() {
        this.status = GameStatus.WAITING;
        this.round = 0;
    }

    // Fix the ID getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId; 
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNbPlayers() {
        return nbPlayers;
    }

    public void setNbPlayers(int nbPlayers) {
        this.nbPlayers = nbPlayers;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
        player.setGame(this);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }    public Player getCurrentPlayer() { 
        return currentPlayer; 
    }
    
    public void setCurrentPlayer(Player currentPlayer) { 
        this.currentPlayer = currentPlayer; 
    }

    public Bid getCurrentBid() { 
        return currentBid; 
    }
    
    public void setCurrentBid(Bid currentBid) { 
        this.currentBid = currentBid; 
    }

    public int getRound() { 
        return round; 
    }
    
    public void setRound(int round) { 
        this.round = round; 
    }

    public void moveToNextPlayer() {
        if (turnSequence.isEmpty()) return;
        currentTurnIndex = (currentTurnIndex + 1) % turnSequence.size();
        currentPlayer = turnSequence.get(currentTurnIndex);
    }

    public void setWinner(Player winner) {
        this.winner = winner;
        this.status = GameStatus.FINISHED;
    }

    public Player getWinner() {
        return winner;
    }
    
    public void initializeTurnSequence() {
        if (players.isEmpty()) return;
        
        // Create a copy of players list
        turnSequence = new ArrayList<>(players);
        // Shuffle the list randomly
        Collections.shuffle(turnSequence);
        currentTurnIndex = 0;
        
        // Set the first player as current player
        if (!turnSequence.isEmpty()) {
            currentPlayer = turnSequence.get(0);
        }
    }

    public List<Player> getTurnSequence() {
        return turnSequence;
    }

    public void setTurnSequence(List<Player> sequence) {
        this.turnSequence = sequence;
    }
}
