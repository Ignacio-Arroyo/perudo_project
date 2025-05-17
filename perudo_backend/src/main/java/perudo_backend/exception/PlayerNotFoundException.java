package perudo_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PlayerNotFoundException extends RuntimeException {
    
    public PlayerNotFoundException(String playerId) {
        super("Could not find player with id: " + playerId);
    }

    public PlayerNotFoundException(Long playerId) {
        this(String.valueOf(playerId));
    }
}
