package perudo_backend.perudo_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import perudo_backend.perudo_backend.FriendRequest;
import perudo_backend.perudo_backend.Player;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByToPlayerAndStatus(Player toPlayer, String status);
}
