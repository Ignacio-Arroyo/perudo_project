package perudo_backend.perudo_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import perudo_backend.perudo_backend.FriendRequest;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.repositories.FriendRequestRepository;
import perudo_backend.perudo_backend.repositories.PlayerRepository;

import java.util.List;

import java.util.Optional;

@Service
public class FriendService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Transactional
    public void sendFriendRequest(String fromPlayerUsername, String toPlayerFriendCode) {
        Optional<Player> fromPlayerOptional = playerRepository.findByUsername(fromPlayerUsername);
        if (fromPlayerOptional.isEmpty()) {
            throw new RuntimeException("From player not found");
        }

        Optional<Player> toPlayerOptional = playerRepository.findByFriendCode(toPlayerFriendCode);
        if (toPlayerOptional.isEmpty()) {
            throw new RuntimeException("To player not found");
        }

        Player fromPlayer = fromPlayerOptional.get();
        Player toPlayer = toPlayerOptional.get();

        FriendRequest friendRequest = new FriendRequest(fromPlayer, toPlayer);
        friendRequestRepository.save(friendRequest);
    }

    public List<FriendRequest> getFriendRequests(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        return friendRequestRepository.findByToPlayerAndStatus(player, "pending");
    }


    @Transactional
    public void acceptFriendRequest(Long requestId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        friendRequest.setStatus("accepted");

        Player fromPlayer = friendRequest.getFromPlayer();
        Player toPlayer = friendRequest.getToPlayer();

        fromPlayer.getFriends().add(toPlayer);
        toPlayer.getFriends().add(fromPlayer);

        playerRepository.save(fromPlayer);
        playerRepository.save(toPlayer);
        friendRequestRepository.save(friendRequest);
    }
}
