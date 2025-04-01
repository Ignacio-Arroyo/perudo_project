import React, { useState } from 'react';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import './login.css';

function Log_in() {
  // État local pour stocker les champs username et password
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  // État local pour afficher un message d'erreur ou de succès
  const [message, setMessage] = useState('');

  // Fonction de soumission du formulaire
  const handleSubmit = async (e) => {
    e.preventDefault(); // Empêche le rafraîchissement de la page

    try {
      const response = await fetch('http://localhost:8080/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ 
          username: username, 
          password: password 
        })
      });

      if (response.ok) {
        // Connexion réussie
        const data = await response.text(); 
        // Par exemple, on peut stocker un token JWT,
        // ou juste afficher un message
        setMessage('Connexion réussie !');
        console.log('Réponse du backend : ', data);
        
        // Vous pouvez rediriger ou stocker l’info en localStorage
        // localStorage.setItem('token', data.token) // ex si data contenait un token
      } else {
        // Erreur d’identifiants
        const errorText = await response.text();
        setMessage('Échec de la connexion : ' + errorText);
      }
    } catch (error) {
      console.error('Erreur réseau ou autre : ', error);
      setMessage('Une erreur est survenue : ' + error.toString());
    }
  };

  return (
    <div className='home-middle-section'>
      <div className='login-block'>
        <h1>Log in to Perudo Game Online Account</h1>
        
        {/* Affichage d'un message si nécessaire */}
        {message && (
          <div style={{ marginBottom: '10px', color: 'red' }}>
            {message}
          </div>
        )}

        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3" controlId="formUsername">
            <Form.Label>Username</Form.Label>
            <Form.Control 
              type="text"
              placeholder="Player1"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required 
            />
          </Form.Group>

          <Form.Group className="mb-3" controlId="formPassword">
            <Form.Label>Password</Form.Label>
            <Form.Control 
              type="password" 
              placeholder="********"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required 
            />
          </Form.Group>

          {/* 
            Par défaut, un Button de type "submit" va déclencher handleSubmit 
            défini sur la balise <Form onSubmit={handleSubmit}> 
          */}
          <Button 
            variant="outline-success"
            id='connexion-button'
            type="submit"
          >
            Log In
          </Button>
        </Form>
      </div>
    </div>
  );
}

export default Log_in;
