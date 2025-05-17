package perudo_backend.perudo_backend.services;

import org.springframework.stereotype.Service;
import perudo_backend.perudo_backend.GameSession;
import perudo_backend.perudo_backend.Player;

@Service
public class GameService {

    public GameSession processGameMove(GameSession gameSession, Player player, String moveDetails) {
        // Logic to process a game move and update the game state
        System.out.println("Processing move: " + moveDetails + " by player: " + player.getUsername());

        // Return the updated game session
        return gameSession;
    }

    public void endGameSession(GameSession gameSession, String winner) {
        // Logic to end a game session
        gameSession.setWinner(winner);
        System.out.println("Game session ended. Winner: " + winner);
    }
}
