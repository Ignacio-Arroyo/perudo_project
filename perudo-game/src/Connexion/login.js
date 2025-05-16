import React, { useState, useContext } from 'react';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { UserContext } from '../Auth/UserContext';
import '../Home_middle_section/home_middle_section.css';
import '../Connexion/login.css';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });

  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { login } = useContext(UserContext);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

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
        console.log('Login successful:', response.data); // Log the user data to verify that it includes the id
        const userData = response.data;
        userData.id = userData.player_id; // Ensure the id is included in the user object
        login(userData);
        navigate('/home');
      }
    } catch (error) {
      if (error.response) {
        if (error.response.status === 401) {
          setError('Invalid username or password.');
        } else {
          console.error('Error logging in:', error);
          setError('An error occurred while logging in.');
        }
      } else {
        console.error('Error logging in:', error);
        setError('An error occurred while logging in.');
      }
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
