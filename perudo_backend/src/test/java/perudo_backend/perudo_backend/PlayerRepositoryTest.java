package perudo_backend.perudo_backend;

import perudo_backend.perudo_backend.repositories.PlayerRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    public void testAddPlayer() {
        // Create a new Player entity
        Player player = new Player();
        player.setNom("Doe");
        player.setPrenom("John");
        player.setUsername("johndoe");
        player.setPassword("password");

        // Save the Player entity
        Player savedPlayer = playerRepository.save(player);

        // Retrieve the saved Player entity
        Player foundPlayer = entityManager.find(Player.class, savedPlayer.getId());

        // Assert that the saved Player entity is equal to the retrieved Player entity
        assertThat(foundPlayer.getNom()).isEqualTo(player.getNom());
        assertThat(foundPlayer.getPrenom()).isEqualTo(player.getPrenom());
        assertThat(foundPlayer.getUsername()).isEqualTo(player.getUsername());
        assertThat(foundPlayer.getPassword()).isEqualTo(player.getPassword());
    }
}
