package perudo_backend.perudo_backend;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import perudo_backend.perudo_backend.repositories.PlayerRepository;

@RestController
@RequestMapping("/api/login")
public class Login {

    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping
    public ResponseEntity<String> verifyPlayer(@RequestBody Player player) {
        String username = player.getUsername();
        String password = player.getPassword();
        Optional<Player> foundPlayer = playerRepository.findByUsername(username);
        if (foundPlayer.isPresent() && foundPlayer.get().getPassword().equals(password)) {
            return new ResponseEntity<>("Player logged in!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }
}