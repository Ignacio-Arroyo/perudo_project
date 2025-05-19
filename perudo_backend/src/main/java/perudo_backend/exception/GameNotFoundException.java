package perudo_backend.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String gameId) {
        super("Game not found: " + gameId);
    }
}