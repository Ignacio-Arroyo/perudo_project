package perudo_backend.perudo_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import perudo_backend.perudo_backend.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
