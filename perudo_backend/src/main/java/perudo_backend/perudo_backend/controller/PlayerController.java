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
import java.util.Map;

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
        logger.info("Attempting to create player: {}", player.toString());
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

    // Récupérer l'inventaire du joueur
    @GetMapping("/{playerId}/inventory")
    public ResponseEntity<?> getInventory(@PathVariable int playerId) {
        Player player = playerService.getPlayerById(playerId);
        if (player == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(player.getDices());
    }

    // Acheter un dé
    @PostMapping("/{playerId}/buy")
    public ResponseEntity<?> buyProduct(@PathVariable int playerId, @RequestBody Map<String, Integer> body) {
        int productId = body.get("productId");
        return playerService.buyProduct(playerId, productId);
    }

    // Équiper un dé
    @PostMapping("/{playerId}/equip")
    public ResponseEntity<?> equipDice(@PathVariable int playerId, @RequestBody Map<String, Integer> body) {
        int diceId = body.get("diceId");
        return playerService.equipDice(playerId, diceId);
    }
    
    // Ajouter des pièces à un joueur spécifique (pour les tests)
    @PutMapping("/update-coins/{username}")
    public ResponseEntity<?> updatePlayerCoins(@PathVariable String username, @RequestBody Map<String, Integer> body) {
        logger.info("Updating coins for player with username: {}", username);
        int coins = body.get("coins");
        
        Optional<Player> playerOpt = playerRepository.findByUsername(username);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setPieces(coins);
            playerRepository.save(player);
            logger.info("Updated coins for player {}: new amount {}", username, coins);
            return ResponseEntity.ok().body(Map.of("message", "Coins updated successfully", "player", player));
        } else {
            logger.warn("Player not found with username: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Player not found");
        }
    }
}
