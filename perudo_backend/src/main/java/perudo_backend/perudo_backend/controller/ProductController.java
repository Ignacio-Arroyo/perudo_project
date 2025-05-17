package perudo_backend.perudo_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import perudo_backend.perudo_backend.Product;
import perudo_backend.perudo_backend.repositories.ProductRepository;

import java.util.List;
import java.util.ArrayList;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    // Méthode d'initialisation des produits par défaut
    @PostConstruct
    public void initializeProducts() {
        if (productRepository.count() == 0) {
            logger.info("Initialisation des produits par défaut...");
            
            List<Product> defaultProducts = new ArrayList<>();
            
            // Ajouter les produits par défaut
            Product p1 = new Product();
            p1.setName("Dé Bois");
            p1.setDescription("Un dé en bois classique");
            p1.setPrice(200);
            p1.setImgUrl("/assets/woodensetdice.png");
            defaultProducts.add(p1);
            
            Product p2 = new Product();
            p2.setName("Dé Rouge");
            p2.setDescription("Un dé rouge brillant");
            p2.setPrice(250);
            p2.setImgUrl("/assets/redsetdice.png");
            defaultProducts.add(p2);
            
            Product p3 = new Product();
            p3.setName("Dé Orange");
            p3.setDescription("Un dé orange vif");
            p3.setPrice(250);
            p3.setImgUrl("/assets/orangesetdice.png");
            defaultProducts.add(p3);
            
            Product p4 = new Product();
            p4.setName("Dé Multicolore");
            p4.setDescription("Un dé avec plusieurs couleurs");
            p4.setPrice(350);
            p4.setImgUrl("/assets/multicolorsetdice.png");
            defaultProducts.add(p4);
            
            Product p5 = new Product();
            p5.setName("Dé Gris/Noir");
            p5.setDescription("Un dé gris avec des points noirs");
            p5.setPrice(220);
            p5.setImgUrl("/assets/grey-blacksetdice.jpg");
            defaultProducts.add(p5);
            
            Product p6 = new Product();
            p6.setName("Dé Vert");
            p6.setDescription("Un dé vert éclatant");
            p6.setPrice(230);
            p6.setImgUrl("/assets/greensetdice.png");
            defaultProducts.add(p6);
            
            Product p7 = new Product();
            p7.setName("Dé Bleu Clair");
            p7.setDescription("Un dé bleu clair élégant");
            p7.setPrice(240);
            p7.setImgUrl("/assets/clearbluesetdice.png");
            defaultProducts.add(p7);
            
            Product p8 = new Product();
            p8.setName("Dé Bleu");
            p8.setDescription("Un dé bleu profond");
            p8.setPrice(240);
            p8.setImgUrl("/assets/bluesetdice.png");
            defaultProducts.add(p8);
            
            Product p9 = new Product();
            p9.setName("Dé Noir");
            p9.setDescription("Un dé noir élégant");
            p9.setPrice(260);
            p9.setImgUrl("/assets/blacksetdice.png");
            defaultProducts.add(p9);
            
            // Sauvegarder tous les produits
            productRepository.saveAll(defaultProducts);
            logger.info("{} produits par défaut ont été créés", defaultProducts.size());
        } else {
            logger.info("Les produits existent déjà dans la base de données. Aucune initialisation nécessaire.");
        }
    }

    @GetMapping
    public List<Product> getAllProducts() {
        logger.info("Récupération de tous les produits");
        List<Product> products = productRepository.findAll();
        logger.info("{} produits trouvés", products.size());
        return products;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.info("Récupération du produit {}", id);
        return productRepository.findById(id)
                .map(product -> {
                    logger.info("Produit trouvé: {}", product.getName());
                    return ResponseEntity.ok(product);
                })
                .orElseGet(() -> {
                    logger.warn("Produit {} non trouvé", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        logger.info("Ajout d'un nouveau produit: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        logger.info("Produit sauvegardé avec l'ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        logger.info("Suppression du produit {}", id);
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    logger.info("Produit {} supprimé", id);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> {
                    logger.warn("Produit {} non trouvé, impossible de le supprimer", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
