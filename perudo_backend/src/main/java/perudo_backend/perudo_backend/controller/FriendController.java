package perudo_backend.perudo_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import perudo_backend.perudo_backend.FriendRequest;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.dto.FriendDTO;
import perudo_backend.perudo_backend.repositories.FriendRequestRepository;
import perudo_backend.perudo_backend.repositories.PlayerRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    // Send friend request using friendCode
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(
            @RequestParam String fromPlayerUsername,
            @RequestParam String toPlayerFriendCode) {
        
        logger.info("Friend request from '{}' to friend code '{}'", fromPlayerUsername, toPlayerFriendCode);
        
        // Validate sender
        Optional<Player> fromPlayerOpt = playerRepository.findByUsername(fromPlayerUsername);
        if (!fromPlayerOpt.isPresent()) {
            logger.warn("Sender with username '{}' not found", fromPlayerUsername);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender not found");
        }
        
        // Validate receiver
        Optional<Player> toPlayerOpt = playerRepository.findByFriendCode(toPlayerFriendCode);
        if (!toPlayerOpt.isPresent()) {
            logger.warn("Recipient with friend code '{}' not found", toPlayerFriendCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with this friend code not found");
        }
        
        Player fromPlayer = fromPlayerOpt.get();
        Player toPlayer = toPlayerOpt.get();
        
        logger.info("Sender ID: {}, username: {}", fromPlayer.getId(), fromPlayer.getUsername());
        logger.info("Recipient ID: {}, username: {}", toPlayer.getId(), toPlayer.getUsername());
        
        // Cannot send request to self
        if (fromPlayer.getId() == toPlayer.getId()) {
            logger.warn("Player attempted to send friend request to self");
            return ResponseEntity.badRequest().body("Cannot send friend request to yourself");
        }
        
        // Check if any friend request exists (regardless of status)
        List<FriendRequest> allRequests = friendRequestRepository.findByFromPlayerAndToPlayer(fromPlayer, toPlayer);
        
        if (!allRequests.isEmpty()) {
            FriendRequest existingRequest = allRequests.get(0);
            logger.info("Found existing request with status: {}", existingRequest.getStatus());
            
            if (existingRequest.getStatus().equals("PENDING")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Friend request already sent and pending");
            } else if (existingRequest.getStatus().equals("ACCEPTED")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("You are already friends with this player");
            } else {
                // If request was rejected, allow to send again by deleting old request
                logger.info("Deleting old rejected request and creating new one");
                friendRequestRepository.delete(existingRequest);
            }
        }
        
        // Check if they're already friends
        if (fromPlayer.getFriends().contains(toPlayer)) {
            logger.info("Players are already friends");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already friends");
        }
        
        // Check for request in opposite direction (from recipient to sender)
        List<FriendRequest> oppositeRequests = friendRequestRepository.findByFromPlayerAndToPlayerAndStatus(
                toPlayer, fromPlayer, "PENDING");
        
        if (!oppositeRequests.isEmpty()) {
            logger.info("Found opposite direction request - automatically accepting");
            FriendRequest oppositeRequest = oppositeRequests.get(0);
            
            // Update friend lists
            updateFriendLists(fromPlayer, toPlayer);
            
            // Update request status
            oppositeRequest.setStatus("ACCEPTED");
            friendRequestRepository.save(oppositeRequest);
            
            return ResponseEntity.ok().body("Friend request from other player accepted automatically");
        }
        
        // Create and save new friend request
        FriendRequest request = new FriendRequest(fromPlayer, toPlayer);
        FriendRequest savedRequest = friendRequestRepository.save(request);
        logger.info("Created new friend request with ID: {}", savedRequest.getId());
        
        return ResponseEntity.ok().body("Friend request sent successfully");
    }

    // Get all friend requests for a player
    @GetMapping("/requests/{playerId}")
    public ResponseEntity<List<FriendRequest>> getFriendRequests(@PathVariable Long playerId) {
        logger.info("Getting friend requests for player ID: {}", playerId);
        
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            logger.warn("Player ID {} not found", playerId);
            return ResponseEntity.notFound().build();
        }
        
        Player player = playerOpt.get();
        logger.info("Found player: {} (id={})", player.getUsername(), player.getId());
        
        List<FriendRequest> requests = friendRequestRepository.findByToPlayerAndStatus(player, "PENDING");
        logger.info("Found {} pending friend requests for player {}", requests.size(), player.getUsername());
        
        // Debug: Log all requests
        for (FriendRequest request : requests) {
            logger.info("Request ID: {}, From: {}, To: {}, Status: {}", 
                request.getId(), 
                request.getFromPlayer().getUsername(),
                request.getToPlayer().getUsername(),
                request.getStatus());
        }
        
        return ResponseEntity.ok(requests);
    }

    // Get all friend requests for a player - Alternative endpoint for player_id field
    @GetMapping("/requests/player/{playerId}")
    public ResponseEntity<List<FriendRequest>> getFriendRequestsByPlayerId(@PathVariable Long playerId) {
        logger.info("Getting friend requests by player_id: {}", playerId);
        
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            logger.warn("Player with player_id {} not found", playerId);
            return ResponseEntity.notFound().build();
        }
        
        Player player = playerOpt.get();
        logger.info("Found player: {} (id={})", player.getUsername(), player.getId());
        
        List<FriendRequest> requests = friendRequestRepository.findByToPlayerAndStatus(player, "PENDING");
        logger.info("Found {} pending friend requests for player {}", requests.size(), player.getUsername());
        
        return ResponseEntity.ok(requests);
    }

    // Accept friend request
    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestParam Long requestId) {
        logger.info("Accepting friend request {}", requestId);
        
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        FriendRequest request = requestOpt.get();
        if (!request.getStatus().equals("PENDING")) {
            return ResponseEntity.badRequest().body("Request is not pending");
        }
        
        Player fromPlayer = request.getFromPlayer();
        Player toPlayer = request.getToPlayer();
        
        // Update friend lists
        updateFriendLists(fromPlayer, toPlayer);
        
        // Update request status
        request.setStatus("ACCEPTED");
        friendRequestRepository.save(request);
        
        return ResponseEntity.ok().body("Friend request accepted");
    }

    // Reject friend request
    @PostMapping("/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestParam Long requestId) {
        logger.info("Rejecting friend request {}", requestId);
        
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        if (!requestOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        FriendRequest request = requestOpt.get();
        if (!request.getStatus().equals("PENDING")) {
            return ResponseEntity.badRequest().body("Request is not pending");
        }
        
        // Update request status
        request.setStatus("REJECTED");
        friendRequestRepository.save(request);
        
        return ResponseEntity.ok().body("Friend request rejected");
    }
    
    // Get friends of a player
    @GetMapping("/{playerId}/friends")
    public ResponseEntity<List<FriendDTO>> getFriends(@PathVariable Long playerId) {
        logger.info("Getting friends for player {}", playerId);
        
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            logger.warn("Player ID {} not found", playerId);
            return ResponseEntity.notFound().build();
        }
        
        Player player = playerOpt.get();
        List<FriendDTO> friendDTOs;
        
        if (player.getFriends() != null) {
            friendDTOs = player.getFriends().stream()
                .map(friend -> new FriendDTO(friend.getId(), friend.getUsername(), friend.getFriendCode()))
                .collect(Collectors.toList());
        } else {
            friendDTOs = java.util.Collections.emptyList();
        }
        
        logger.info("Found {} friends for player {}", friendDTOs.size(), player.getUsername());
        
        // Debug: Log all friends (maintenant DTOs)
        for (FriendDTO friendDto : friendDTOs) {
            logger.info("Friend DTO: {} (id={})", friendDto.getUsername(), friendDto.getId());
        }
        
        return ResponseEntity.ok(friendDTOs);
    }
    
    // Helper method to update friends lists for both players
    private void updateFriendLists(Player player1, Player player2) {
        // Add player2 to player1's friends
        player1.getFriends().add(player2);
        playerRepository.save(player1);
        
        // Add player1 to player2's friends
        player2.getFriends().add(player1);
        playerRepository.save(player2);
    }

    // Debug endpoint to show all friend requests
    @GetMapping("/requests/all")
    public ResponseEntity<List<FriendRequest>> getAllFriendRequests() {
        logger.info("Debug: Getting all friend requests");
        List<FriendRequest> allRequests = friendRequestRepository.findAll();
        
        logger.info("Found total {} friend requests in the system", allRequests.size());
        for (FriendRequest req : allRequests) {
            logger.info("Request ID: {}, From: {}, To: {}, Status: {}", 
                req.getId(), 
                req.getFromPlayer().getUsername(),
                req.getToPlayer().getUsername(), 
                req.getStatus());
        }
        
        return ResponseEntity.ok(allRequests);
    }
}
