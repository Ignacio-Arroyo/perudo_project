package perudo_backend.perudo_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.dto.FriendDTO;
import perudo_backend.perudo_backend.repositories.PlayerRepository;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public Player getPlayerById(int playerId) {
        return playerRepository.findById(playerId).orElse(null);
    }

    public List<FriendDTO> getFriendsByPlayerId(int playerId) {
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player != null) {
            return player.getFriends().stream()
                    .map(FriendDTO::new)
                    .collect(Collectors.toList());
        }
        return List.of(); // Return an empty list if the player is not found
    }
}
