package perudo_backend.perudo_backend;

import jakarta.persistence.*;
import java.util.Collection;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int player_id;
    String nom;
    String prenom;
    String username;
    String password;

    @ManyToOne
    @JoinColumn(name = "game_id") // Foreign key column in the Player table
    private Game game;

    @ManyToMany
    @JoinTable(
        name = "player_dice",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "dice_id")
    )
    private Collection<Dice> ownedDice;

    public Player( String nom, String prenom, String username, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.username = username;
        this.password = password;
    }

    public Player() {
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
}