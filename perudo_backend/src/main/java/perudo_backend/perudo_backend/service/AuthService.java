package perudo_backend.perudo_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import perudo_backend.perudo_backend.LoginRequest;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.repositories.PlayerRepository;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private PlayerRepository playerRepository;

    public Player registerPlayer(Player player) {
        // Valider les données du joueur
        if (player.getUsername() == null || player.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        // Sauvegarder le joueur et récupérer l'instance avec l'ID
        Player savedPlayer = playerRepository.save(player);
        
        // Log pour debug
        log.info("Player registered with ID: {}", savedPlayer.getId());
        
        return savedPlayer;
    }

    public Player authenticatePlayer(LoginRequest loginRequest) {
        // Example authentication logic
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            throw new IllegalArgumentException("Username and password must not be null");
        }
        Optional<Player> optionalPlayer = playerRepository.findByUsername(loginRequest.getUsername());
        if (optionalPlayer.isPresent() && optionalPlayer.get().getPassword().equals(loginRequest.getPassword())) {
            Player player = optionalPlayer.get();
            log.info("Player authenticated: {}", player.getUsername());
            return player;
        } else {
            log.warn("Authentication failed for username: {}", loginRequest.getUsername());
            return null;
        }
    }
}