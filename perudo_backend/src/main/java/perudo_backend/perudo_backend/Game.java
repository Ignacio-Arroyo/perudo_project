package perudo_project.perudo;

import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GenerationType;

@Entity
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    int id;
    String name;
    int nbPlayers;
    //String log;
    Collection<Person> players;

    @OneToMany(mappedBy="game")

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getNbPlayers(){
        return nbPlayers;
    }

    public void setNbPlayers(int nbPlayers){
        this.nbPlayers = nbPlayers;
    }

    public Collection<Person> getPlayers(){
        return players;
    }

    public void setPlayers(Collection<Person> players){
        this.players = players;
    }

    // public String getLog(){
    //     return log;
    // }

    // public void setLog(String log){
    //     this.log = log;
    // }

    public void addPlayer(Person player){
        this.players.add(player);
    }

    public void removePlayer(Person player){
        this.players.remove(player);
    }

    public void startGame(){
        //TODO
    }

    public void endGame(){
        //TODO
    }

}