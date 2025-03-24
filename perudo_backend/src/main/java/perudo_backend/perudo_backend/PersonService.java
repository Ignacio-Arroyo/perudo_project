package perudo_backend.perudo_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Spring Security

    public Person findByUsername(String username) {
        return personRepository.findByUsername(username);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        // si on veut utiliser un encodeur Bcrypt ou autre
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
