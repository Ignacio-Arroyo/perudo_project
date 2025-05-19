package perudo_backend.perudo_backend.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import perudo_backend.perudo_backend.LoginRequest;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(@RequestBody Player player) {
        try {
            Player registeredPlayer = authService.registerPlayer(player);
            log.info("Player registered successfully with ID: {}", registeredPlayer.getId());
            return ResponseEntity.ok(registeredPlayer);
        } catch (Exception e) {
            log.error("Registration failed: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginPlayer(@RequestBody LoginRequest loginRequest) {
        try {
            Player player = authService.authenticatePlayer(loginRequest);

            // Create response with all necessary fields
            Map<String, Object> response = new HashMap<>();
            response.put("id", player.getId());
            response.put("username", player.getUsername());
            response.put("nom", player.getNom());
            response.put("prenom", player.getPrenom());
            response.put("friendCode", player.getFriendCode());

            log.info("Player logged in successfully with ID: {}", player.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}