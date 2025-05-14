package perudo_backend.perudo_backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class GameRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int record_id;
    private boolean victory; // Indique si le joueur a gagné
    private int score; // Score obtenu lors de la partie

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player; // Référence au joueur associé à cet enregistrement

    // Constructeur par défaut
    public GameRecord() {
    }

    // Constructeur avec paramètres
    public GameRecord(boolean victory, int score, Player player) {
        this.victory = victory;
        this.score = score;
        this.player = player;
    }

    // Getters et Setters

    public int getRecordId() {
        return record_id;
    }

    public void setRecordId(int record_id) {
        this.record_id = record_id;
    }

    public boolean isVictory() {
        return victory;
    }

    public void setVictory(boolean victory) {
        this.victory = victory;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}

