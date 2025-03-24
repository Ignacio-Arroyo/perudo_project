import React from 'react';
import Form from 'react-bootstrap/Form';
import '../Home_middle_section/home_middle_section.css';
import './login.css';
import Button from 'react-bootstrap/Button';

function Log_in() {
  // On stocke les valeurs du form dans le state
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  
  // Pour la navigation après login réussi
  // const navigate = useNavigate();

  const handleLogin = async () => {
    // Appel au backend
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      });
      
      if (response.ok) {
        // OK
        // const data = await response.json() ou response.text() selon ce que tu renvoies
        // Par exemple:
        // console.log("Success:", data);
        // navigate("/lobby"); // Redirection vers le lobby
        alert("Login success!");
      } else {
        // Erreur
        // Tu peux récupérer le message d’erreur
        const errorText = await response.text();
        console.error("Error:", errorText);
        alert("Login failed");
      }
    } catch (error) {
      console.error("Error:", error);
      alert("Something went wrong");
    }
  }

  return (
    <div className='home-middle-section'>
      <div className='login-block'>
        <h1>Log in to Perudo Game Online Account</h1>
        <Form>
          <Form.Group className="mb-3" controlId="usernameInput">
            <Form.Label>U0.
            sername</Form.Label>
            <Form.Control 
              type="text" 
              placeholder="Player1"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </Form.Group>
          
          <Form.Group className="mb-3" controlId="passwordInput">
            <Form.Label>Password</Form.Label>
            <Form.Control 
              type="password" 
              placeholder="********" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </Form.Group>
        </Form>
        
        <Button variant="outline-success" id='connexion-button' onClick={handleLogin}>
          Log In
        </Button>
      </div>
    </div>
  );
}

export default Log_in;