package perudo_backend.perudo_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.Product;
import perudo_backend.perudo_backend.repositories.PlayerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import perudo_backend.perudo_backend.repositories.ProductRepository;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ProductRepository productRepository;

    public Player getPlayerById(int playerId) {
        return playerRepository.findById(playerId).orElse(null);
    }

    public ResponseEntity<?> buyProduct(int playerId, int productId) {
        Player player = getPlayerById(playerId);
        Product product = productRepository.findById((long)productId).orElse(null);
        if (player == null) return ResponseEntity.notFound().build();
        else if (player.getPieces() < product.getPrice()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous n'avez pas assez de pièces");
        else {
            player.setPieces(player.getPieces() - (int)product.getPrice());
            playerRepository.save(player);
            player.getInventory().add(product);
            playerRepository.save(player); 
            return ResponseEntity.ok(player.getInventory());
        }
    }

    public ResponseEntity<?> equipDice(int playerId, int diceId) {
        // TODO: Logique pour équiper un dé
        return ResponseEntity.ok("Dé équipé (squelette)");
    }
}
