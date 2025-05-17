package perudo_backend.perudo_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import perudo_backend.perudo_backend.GameMove;
import perudo_backend.perudo_backend.GameSession;
import perudo_backend.perudo_backend.services.GameService;

@Controller
public class GameController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameService gameService;

    @MessageMapping("/game/move")
    public void handleGameMove(GameMove move) {
        // Process the game move and update the game state
        GameSession updatedGameState = gameService.processGameMove(move.getGameSession(), move.getPlayer(), move.getMoveDetails());

        // Broadcast the updated game state to all players
        messagingTemplate.convertAndSend("/topic/game/updates", updatedGameState);
    }
}
