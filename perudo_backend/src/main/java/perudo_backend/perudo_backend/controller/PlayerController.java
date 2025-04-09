package perudo_backend.perudo_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.repositories.PlayerRepository;

import java.util.List;
import java.util.Optional;



@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);


    @PostMapping
    public ResponseEntity<?> createPlayer(@RequestBody Player player) {
        // Check if the username already exists
        Optional<Player> existingPlayer = playerRepository.findByUsername(player.getUsername());
        if (existingPlayer.isPresent()) {
            logger.warn("Attempt to create a player with an existing username: {}", player.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        Player createdPlayer = playerRepository.save(player);
        logger.info("Player created successfully: {}", createdPlayer.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
    }


    // Get all Players
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    // Get a Player by ID
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable int id) {
        Optional<Player> player = playerRepository.findById(id);
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a Player
    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable int id, @RequestBody Player playerDetails) {
        return playerRepository.findById(id).map(player -> {
            player.setNom(playerDetails.getNom());
            player.setPrenom(playerDetails.getPrenom());
            player.setUsername(playerDetails.getUsername());
            player.setPassword(playerDetails.getPassword());
            Player updatedPlayer = playerRepository.save(player);
            return ResponseEntity.ok(updatedPlayer);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a Player
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable int id) {
        return playerRepository.findById(id).map(player -> {
            playerRepository.delete(player);
            return ResponseEntity.ok().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPlayer(@RequestParam String username) {
        logger.info("Searching for player with username: {}", username);
        Optional<Player> player = playerRepository.findByUsername(username);
        if (player.isPresent()) {
            logger.info("Player found: {}", player.get().getUsername());
            return ResponseEntity.ok(player.get());
        } else {
            logger.warn("Player not found with username: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

/*     @GetMapping("/api/players/stats")
    public ResponseEntity<?> getPlayerStats(@RequestParam String username) {
        Optional<Player> player = playerRepository.findByUsername(username);
        if (player.isPresent()) {
            // Assuming you have a method to calculate stats
            PlayerStats stats = calculatePlayerStats(player.get());
            return ResponseEntity.ok(stats);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    private PlayerStats calculatePlayerStats(Player player) {
        // Implement your logic to calculate statistics
        PlayerStats stats = new PlayerStats();
        stats.setUsername(player.getUsername());
        stats.setTotalGamesPlayed(10); // Example value
        stats.setTotalWins(5); // Example value
        stats.setWinRate(50.0); // Example value
        return stats;
    } */

}
