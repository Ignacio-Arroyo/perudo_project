package perudo_backend.perudo_backend;

import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;


@Entity
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int store_id;
    String name;

    @OneToMany(mappedBy = "store") // Define the relationship
    private Collection<Dice> dices;

    public int getId() {
        return store_id;
    }

    public void setId(int id) {
        this.store_id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Dice> getDices() {
        return dices;
    }

    public void setDices(Collection<Dice> dices) {
        this.dices = dices;
    }
}
