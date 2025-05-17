package perudo_backend.perudo_backend.services;

import org.springframework.stereotype.Service;
import perudo_backend.perudo_backend.Player;

import java.util.LinkedList;
import java.util.Queue;

@Service
public class MatchmakingService {

    private Queue<Player> waitingPlayers = new LinkedList<>();

    public void addPlayerToQueue(Player player) {
        waitingPlayers.add(player);
        if (waitingPlayers.size() >= 2) {
            Player player1 = waitingPlayers.poll();
            Player player2 = waitingPlayers.poll();
            startGameSession(player1, player2);
        }
    }

    private void startGameSession(Player player1, Player player2) {
        // Logic to start a game session between player1 and player2
        System.out.println("Starting game session between " + player1.getUsername() + " and " + player2.getUsername());
    }
}
