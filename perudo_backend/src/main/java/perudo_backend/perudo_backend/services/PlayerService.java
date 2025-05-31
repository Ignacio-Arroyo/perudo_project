package perudo_backend.perudo_backend.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.dto.FriendDTO;
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

    public Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId).orElse(null);
    }

    public List<FriendDTO> getFriendsByPlayerId(Long playerId) {
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player != null) {
            return player.getFriends().stream()
                    .map(friend -> new FriendDTO(friend.getId(), friend.getUsername(), friend.getFriendCode()))
                    .distinct()
                    .collect(Collectors.toList());
        }
        return List.of(); // Return an empty list if the player is not found
    }

    public ResponseEntity<?> buyProduct(Long playerId, int productId) {
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
            // Vérifier si le joueur possède déjà ce produit
            boolean alreadyOwns = player.getInventory().stream()
                .anyMatch(p -> p.getId().equals(product.getId()));
                
            if (alreadyOwns) {
                logger.info("Le joueur {} possède déjà le produit {}", playerId, productId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous possédez déjà ce produit");
            }
            
            // Mettre à jour les pièces du joueur
            player.setPieces(player.getPieces() - product.getPrice());
            
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

    public ResponseEntity<?> equipDice(Long playerId, int productId) {
        logger.info("Tentative d'équipement du produit {} par le joueur {}", productId, playerId);
        
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
        
        // Vérifier si le joueur possède le produit
        boolean ownsProduct = player.getInventory().stream()
                .anyMatch(p -> p.getId().equals(product.getId()));
                
        if (!ownsProduct) {
            logger.warn("Le joueur {} ne possède pas le produit {}", playerId, productId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous ne possédez pas ce produit");
        }
        
        try {
            // Équiper le produit
            player.setEquippedProduct(productId);
            playerRepository.save(player);
            
            logger.info("Équipement réussi: Joueur {} a équipé le produit {}", playerId, productId);
            return ResponseEntity.ok().body(Map.of(
                "message", "Produit équipé avec succès",
                "equippedProduct", productId
            ));
        } catch (Exception e) {
            logger.error("Erreur lors de l'équipement du produit {} par le joueur {}: {}", productId, playerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'équipement: " + e.getMessage());
        }
    }
}
