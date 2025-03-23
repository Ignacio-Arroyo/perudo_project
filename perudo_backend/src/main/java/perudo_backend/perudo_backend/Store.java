package perudo_project.perudo;

import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    int id;
    String name;
    Collection<Dice> dices;

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

    public Collection<Dice> getDices(){
        return dices;
    }

    public void setDices(Collection<Dice> dices){
        this.dices = dices;
    }

    

}
