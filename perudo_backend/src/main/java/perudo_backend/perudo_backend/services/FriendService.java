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
        // Retrieve the friend request
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        System.out.println("Friend request retrieved: " + friendRequest);

        // Update the status of the friend request
        friendRequest.setStatus("accepted");
        System.out.println("Friend request status updated to: " + friendRequest.getStatus());

        // Retrieve the players involved in the friend request
        Player fromPlayer = friendRequest.getFromPlayer();
        Player toPlayer = friendRequest.getToPlayer();
        System.out.println("From player: " + fromPlayer.getUsername());
        System.out.println("To player: " + toPlayer.getUsername());

        // Add each player to the other's friends list
        fromPlayer.getFriends().add(toPlayer);
        toPlayer.getFriends().add(fromPlayer);
        System.out.println("Friends added to each other's lists");

        // Print the updated friends lists
        System.out.println("From player's friends: " + fromPlayer.getFriends());
        System.out.println("To player's friends: " + toPlayer.getFriends());

        // Save the changes to the database
        playerRepository.save(fromPlayer);
        playerRepository.save(toPlayer);
        friendRequestRepository.save(friendRequest);
        System.out.println("Changes saved to the database");
    }

   @Transactional
    public void rejectFriendRequest(Long requestId) {
        // Retrieve the friend request
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        System.out.println("Friend request retrieved: " + friendRequest);

        // Update the status of the friend request to "rejected"
        friendRequest.setStatus("rejected");
        System.out.println("Friend request status updated to: " + friendRequest.getStatus());

        // Save the changes to the database
        friendRequestRepository.save(friendRequest);
        System.out.println("Changes saved to the database");
    }

}
