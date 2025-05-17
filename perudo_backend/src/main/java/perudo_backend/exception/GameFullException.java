package perudo_backend.exception;

public class GameFullException extends RuntimeException {
    public GameFullException() {
        super("Game is full");
    }
}