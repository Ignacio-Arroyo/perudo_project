package perudo_backend.perudo_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import perudo_backend.perudo_backend.Player;

public interface PlayerRepository extends JpaRepository<Player, Integer> {

    Optional<Player> findByUsername(String username);
    //Player findByUsername(String username);  Custom query method to find a player by username
}