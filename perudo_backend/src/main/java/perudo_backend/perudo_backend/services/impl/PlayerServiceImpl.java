package perudo_backend.perudo_backend.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.repositories.PlayerRepository;
import perudo_backend.perudo_backend.services.PlayerService;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public boolean addPlayer(Player player) {
        // Check if the username already exists
        if (playerRepository.findByUsername(player.getUsername()) != null) {
            return false; // Username already exists
        }

        // Save the player to the database
        playerRepository.save(player);
        return true;
    }
}