import React, { useState } from 'react';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import '../Home_middle_section/home_middle_section.css';
import '../Connexion/login.css';
import { useAuth } from '../Auth/authcontext';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });

  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    
    try {
      const response = await axios.post('http://localhost:8080/api/players/login', {
        username: formData.username,
        password: formData.password
      }, {
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.status === 200) {
        console.log('Login successful:', response.data);
        // Stocker le token ou les informations de l'utilisateur si nécessaire
        localStorage.setItem('user', JSON.stringify(response.data));
        // Mettre à jour l'état d'authentification
        login(response.data);
        // Rediriger vers une autre page après la connexion
        navigate('/home'); // Assurez-vous que cette route est définie dans votre application
      }
    } catch (error) {
        console.error('Error logging in:', error);
        setError(error.response?.data?.message || error.message || 'Login failed');
    }
};

  return (
    <div className='home-middle-section'>
      <div className='login-block'>
        <h1>Login</h1>
        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3" controlId="formUsername">
            <Form.Label>Username</Form.Label>
            <Form.Control
              type="text"
              name="username"
              placeholder="Enter username"
              value={formData.username}
              onChange={handleChange}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="formPassword">
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              name="password"
              placeholder="Password"
              value={formData.password}
              onChange={handleChange}
            />
          </Form.Group>
          {error && <p className="text-danger">{error}</p>}
          <Button variant="outline-success" type="submit" id='connexion-button'>
            Login
          </Button>
        </Form>
      </div>
    </div>
  );
};

export default Login;
