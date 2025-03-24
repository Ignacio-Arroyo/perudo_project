package perudo_backend.perudo_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import perudo_backend.perudo_backend.services.PlayerService;

@RestController
@RequestMapping("/api/signup")
public class Signup {

    @Autowired
    private PlayerService playerService;

    @PostMapping
    public ResponseEntity<String> addPlayer(@RequestBody Player player) {
        boolean isAdded = playerService.addPlayer(player);
        if (isAdded) {
            return new ResponseEntity<>("Player added successfully!", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Failed to add player. Username might already exist.", HttpStatus.BAD_REQUEST);
        }
    }
}
