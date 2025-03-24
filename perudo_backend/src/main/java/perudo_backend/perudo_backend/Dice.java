package perudo_backend.perudo_backend;

import java.util.Collection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Dice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    int dice_id;
    String color;
    String face1;
    String face2;
    String face3;
    String face4;
    String face5;
    String face6;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToMany(mappedBy = "ownedDice")
    private Collection<Player> owners;

    public int getId(){
        return dice_id;
    }

    public void setId(int id){
        this.dice_id = id;
    }

    public String getColor(){
        return color;
    }

    public void setColor(String color){
        this.color = color;
    }

    public String getFace1(){
        return face1;
    }

    public void setFace1(String face1){
        this.face1 = face1;
    }

    public String getFace2(){
        return face2;
    }   

    public void setFace2(String face2){
        this.face2 = face2;
    }

    public String getFace3(){
        return face3;
    }

    public void setFace3(String face3){
        this.face3 = face3;
    }

    public String getFace4(){
        return face4;
    }

    public void setFace4(String face4){
        this.face4 = face4;
    }

    public String getFace5(){
        return face5;
    }

    public void setFace5(String face5){
        this.face5 = face5;
    }

    public String getFace6(){
        return face6;
    }

    public void setFace6(String face6){
        this.face6 = face6;
    }

    public Dice(String color, String face1, String face2, String face3, String face4, String face5, String face6){
        this.color = color;
        this.face1 = face1;
        this.face2 = face2;
        this.face3 = face3;
        this.face4 = face4;
        this.face5 = face5;
        this.face6 = face6;
    }

}
