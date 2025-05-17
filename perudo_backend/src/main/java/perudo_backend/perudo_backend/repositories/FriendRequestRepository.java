package perudo_backend.perudo_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import perudo_backend.perudo_backend.FriendRequest;
import perudo_backend.perudo_backend.Player;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByToPlayerAndStatus(Player toPlayer, String status);
    List<FriendRequest> findByFromPlayerAndToPlayerAndStatus(Player fromPlayer, Player toPlayer, String status);
    List<FriendRequest> findByToPlayer_Id(int playerId);
    List<FriendRequest> findByFromPlayerAndToPlayer(Player fromPlayer, Player toPlayer);
}
