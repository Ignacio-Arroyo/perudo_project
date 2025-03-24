package perudo_backend.perudo_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PersonService personService; // Un service qui permet de récupérer des Person par username, etc.

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. On récupère la personne via le service
        Person person = personService.findByUsername(loginRequest.getUsername());
        if(person == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // 2. On compare les mots de passe 
        //   (Attention : normalement on stocke en base un mot de passe hashé,
        //    et on utilise un PasswordEncoder pour vérifier la correspondance)

        boolean passwordMatches = personService.checkPassword(loginRequest.getPassword(), person.getPassword());
        if(!passwordMatches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password");
        }

        // 3. Si tout est bon, on renvoie une réponse 200 OK
        //    Tu peux renvoyer des infos, un token JWT, etc.
        return ResponseEntity.ok("Login success");
    }
}
