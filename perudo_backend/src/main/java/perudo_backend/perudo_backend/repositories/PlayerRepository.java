package perudo_backend.perudo_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import perudo_backend.perudo_backend.Player;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    Player findByUsername(String username);
}