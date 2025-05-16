package perudo_backend.perudo_backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ProductRepository productRepository;

    public Player getPlayerById(int playerId) {
        return playerRepository.findById(playerId).orElse(null);
    }

    public ResponseEntity<?> buyProduct(int playerId, int productId) {
        logger.info("Tentative d'achat du produit {} par le joueur {}", productId, playerId);
        
        Player player = getPlayerById(playerId);
        if (player == null) {
            logger.warn("Joueur {} non trouvé", playerId);
            return ResponseEntity.notFound().build();
        }
        
        Product product = productRepository.findById((long)productId).orElse(null);
        if (product == null) {
            logger.warn("Produit {} non trouvé", productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Le produit n'existe pas");
        }
        
        logger.info("Joueur: {}, Pièces: {}, Prix du produit: {}", player.getUsername(), player.getPieces(), product.getPrice());
        
        if (player.getPieces() < product.getPrice()) {
            logger.warn("Joueur {} n'a pas assez de pièces pour acheter le produit {}", playerId, productId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous n'avez pas assez de pièces");
        }
        
        try {
            // Mettre à jour les pièces du joueur
            player.setPieces(player.getPieces() - product.getPrice());
            playerRepository.save(player);
            
            // Ajouter le produit à l'inventaire
            player.getInventory().add(product);
            playerRepository.save(player);
            
            logger.info("Achat réussi: Joueur {} a acheté le produit {}", playerId, productId);
            return ResponseEntity.ok(player.getInventory());
        } catch (Exception e) {
            logger.error("Erreur lors de l'achat du produit {} par le joueur {}: {}", productId, playerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'achat: " + e.getMessage());
        }
    }

    public ResponseEntity<?> equipDice(int playerId, int diceId) {
        // TODO: Logique pour équiper un dé
        return ResponseEntity.ok("Dé équipé (squelette)");
    }
}
