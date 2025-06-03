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
import jakarta.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


@Entity
@Table(name = "games")
@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class,
  property = "id")
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

    // To store all players who started the game, for end-game stats
    // This list will hold references to the Player entities involved at the start.
    // The Player entities themselves are managed by JPA.
    @Transient // Marking as transient as we are not mapping it as a direct persistent collection here
    private List<Player> originalPlayers = new ArrayList<>();

    public Game() {
        this.status = GameStatus.WAITING;
        this.round = 0;
        this.players = new ArrayList<>();
        this.turnSequence = new ArrayList<>();
        this.originalPlayers = new ArrayList<>(); // Initialize in constructor
    }

    // Getters et Setters de base
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

    public GameStatus getStatus() { 
        return status; 
    }
    
    public void setStatus(GameStatus status) { 
        this.status = status; 
    }    
    
    public Player getCurrentPlayer() { 
        return currentPlayer; 
    }
    
    public void setCurrentPlayer(Player currentPlayer) { 
        // Mettre à jour l'attribut currentTurn pour chaque joueur
        for (Player p : players) {
            p.setCurrentTurn(p.equals(currentPlayer));
        }
        this.currentPlayer = currentPlayer; 
        if (this.currentPlayer != null && !this.turnSequence.isEmpty()) {
            this.currentTurnIndex = this.turnSequence.indexOf(this.currentPlayer);
            System.out.println("[Game.setCurrentPlayer] Set currentTurnIndex to: " + this.currentTurnIndex + " for player " + (this.currentPlayer != null ? this.currentPlayer.getId() : "null"));
        } else if (this.turnSequence.isEmpty()) {
            this.currentTurnIndex = 0;
            System.out.println("[Game.setCurrentPlayer] turnSequence is empty or currentPlayer is null, currentTurnIndex reset/kept as is.");
        } else {
             this.currentTurnIndex = 0;
             System.out.println("[Game.setCurrentPlayer] currentPlayer is null, turnSequence not empty. currentTurnIndex set to 0.");
        }
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

    public Player getWinner() {
        return winner;
    }

    public List<Player> getTurnSequence() {
        return turnSequence;
    }

    public void setTurnSequence(List<Player> sequence) {
        this.turnSequence = sequence;
    }

    public List<Player> getOriginalPlayers() {
        return originalPlayers;
    }

    public void setOriginalPlayers(List<Player> originalPlayers) {
        this.originalPlayers = originalPlayers;
    }

    // Méthodes de gestion des joueurs
    public void addPlayer(Player player) {
        this.players.add(player);
        player.setGame(this);
    }

    public void removePlayer(Player player) {
        boolean wasCurrentPlayer = player.equals(currentPlayer);
        this.players.remove(player);
        this.turnSequence.remove(player);
        player.setCurrentTurn(false);
        
        // Si c'était le joueur actuel, on passe au joueur suivant
        if (wasCurrentPlayer && !turnSequence.isEmpty()) {
            moveToNextPlayer();
        }
    }

    // Méthodes de gestion des tours
    public void initializeTurnSequence() {
        // Réinitialiser l'état de tous les joueurs
        for (Player p : players) {
            p.setCurrentTurn(false);
        }
        
        // Créer une séquence de tour avec tous les joueurs et la mélanger
        turnSequence = new ArrayList<>(players);
        Collections.shuffle(turnSequence);
        
        // Réinitialiser l'index de tour et définir le joueur actuel
        currentTurnIndex = 0;
        if (!turnSequence.isEmpty()) {
            currentPlayer = turnSequence.get(0);
            currentPlayer.setCurrentTurn(true);
        }
    }

    public void moveToNextPlayer() {
        System.out.println("[Game.moveToNextPlayer] Called. Current turnSequence size: " + turnSequence.size());
        if (turnSequence.isEmpty()) {
            System.out.println("[Game.moveToNextPlayer] turnSequence is empty, returning.");
            return;
        }
        
        System.out.println("[Game.moveToNextPlayer] Current player before move: " + (currentPlayer != null ? currentPlayer.getId() + " (" + currentPlayer.getUsername() + ")" : "null") + " at index: " + currentTurnIndex);
        System.out.print("[Game.moveToNextPlayer] Turn sequence IDs: ");
        for (Player p : turnSequence) {
            System.out.print(p.getId() + " ");
        }
        System.out.println();

        // Désactiver le tour du joueur actuel
        if (currentPlayer != null) {
            currentPlayer.setCurrentTurn(false);
        }
        
        // Passer au joueur suivant
        currentTurnIndex = (currentTurnIndex + 1) % turnSequence.size();
        currentPlayer = turnSequence.get(currentTurnIndex);
        
        System.out.println("[Game.moveToNextPlayer] New current player after move: " + (currentPlayer != null ? currentPlayer.getId() + " (" + currentPlayer.getUsername() + ")" : "null") + " at new index: " + currentTurnIndex);

        // Activer le tour du nouveau joueur actuel
        if (currentPlayer != null) {
            currentPlayer.setCurrentTurn(true);
        }
    }

    public void setWinner(Player winner) {
        // Désactiver le tour pour tous les joueurs
        for (Player p : players) {
            p.setCurrentTurn(false);
        }
        
        this.winner = winner;
        this.status = GameStatus.FINISHED;
        
        // Optionnel: activer le tour pour le gagnant
        if (winner != null) {
            winner.setCurrentTurn(true);
        }
    }
}
