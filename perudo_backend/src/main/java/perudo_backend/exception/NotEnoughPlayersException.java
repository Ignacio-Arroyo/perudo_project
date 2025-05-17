package perudo_backend.exception;

public class NotEnoughPlayersException extends RuntimeException {
    
    public NotEnoughPlayersException() {
        super("Not enough players to start the game. Minimum required: 2");
    }

    public NotEnoughPlayersException(String message) {
        super(message);
    }

    public NotEnoughPlayersException(int currentPlayers, int requiredPlayers) {
        super(String.format("Not enough players to start the game. Current: %d, Required: %d", 
            currentPlayers, requiredPlayers));
    }
}
