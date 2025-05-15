package perudo_backend.perudo_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import perudo_backend.perudo_backend.FriendRequest;
import perudo_backend.perudo_backend.services.FriendService;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @PostMapping("/request")
    public void sendFriendRequest(@RequestParam String fromPlayerUsername, @RequestParam String toPlayerFriendCode) {
        friendService.sendFriendRequest(fromPlayerUsername, toPlayerFriendCode);
    }

    @GetMapping("/requests/{playerId}")
    public List<FriendRequest> getFriendRequests(@PathVariable Long playerId) {
        return friendService.getFriendRequests(playerId);
    }


    @PostMapping("/accept")
    public void acceptFriendRequest(@RequestParam Long requestId) {
        friendService.acceptFriendRequest(requestId);
    }
}
