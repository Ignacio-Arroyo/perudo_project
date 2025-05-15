package perudo_backend.perudo_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.repositories.PlayerRepository;
import perudo_backend.perudo_backend.LoginRequest;
import perudo_backend.perudo_backend.services.PlayerService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerService playerService;

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
        logger.info("Player created successfully: {} - {}", createdPlayer.getUsername(), createdPlayer.getFriendCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginPlayer(@RequestBody LoginRequest loginRequest) {
        Optional<Player> player = playerRepository.findByUsername(loginRequest.getUsername());
        if (player.isPresent() && player.get().getPassword().equals(loginRequest.getPassword())) {
            // Générer un token ou une session pour l'utilisateur si nécessaire
            return ResponseEntity.ok(player.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
        

    @GetMapping("/{playerId}")
    public Player getPlayerProfile(@PathVariable int playerId) {
        Player player = playerService.getPlayerById(playerId);
        if (player != null) {
            player.setPassword(null); // Ensure password is not sent
        }
        return player;
    }

    // Get all Players
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    // Get a Player by FriendCode
    @GetMapping("/friendcode/{friendCode}")
    public ResponseEntity<Player> getPlayerByFriendCode(@PathVariable String friendCode) {
        Optional<Player> player = playerRepository.findByFriendCode(friendCode);
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get a Player by ID
    @PostMapping("/{id}")
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
}
