package perudo_project.perudo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GenerationType;

import java.util.Collection;

@Entity
public class Person{
	@Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
	String nom;
	String prenom;
    String username;
    String password;
    Collection<Dice> dices;
    


    @OneToMany(mappedBy="personne")
	
	
	public int getId(){
        return id;
	}
    
	public void setId(int id){
        this.id = id;
    } 

    public String getNom(){
        return nom;
    }

    public void setNom(String nom){
        this.nom = nom;
    }

    public String getPrenom(){
        return prenom;
    }

    public void setPrenom(String prenom){
        this.prenom = prenom;
    }

    public String getUsername(){
        return username;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    //is this logic? To be able to get the password of a person?
    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }
    

}