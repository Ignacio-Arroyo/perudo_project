package perudo_backend.perudo_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.repositories.PlayerRepository;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public Player getPlayerById(int playerId) {
        return playerRepository.findById(playerId).orElse(null);
    }
}
