package perudo_backend.perudo_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import perudo_backend.perudo_backend.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUsername(String username);
    Optional<Player> findByFriendCode(String friendCode);
    Optional<Player> findById(Long playerId);
    //Player findByUsername(String username);  Custom query method to find a player by username
    
    // Récupérer les joueurs triés par nombre de trophées (ordre décroissant)
    @Query("SELECT p FROM Player p ORDER BY p.trophies DESC")
    List<Player> findAllOrderByTrophiesDesc();
}