import React, { useState } from 'react';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import '../Home_middle_section/home_middle_section.css';
import '../Connexion/login.css';
import './register.css';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    firstName: '',
    lastName: '',
    password: '',
    confirmPassword: ''
  });

  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const validatePassword = (password) => {
    const minLength = 1;
    /* const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumbers = /[0-9]/.test(password);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password); */

    return (
      password.length >= minLength /* &&
      hasUpperCase &&
      hasLowerCase &&
      hasNumbers &&
      hasSpecial */
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const { password, confirmPassword } = formData;

    if (!validatePassword(password)) {
      setError('Password does not meet the requirements.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    try {
      const response = await axios.post('http://localhost:8080/api/players', {
        username: formData.username,
        nom: formData.lastName,
        prenom: formData.firstName,
        password: formData.password
      }, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Basic ' + btoa('username:password')
        }
      });
      console.log('Player created:', response.data);
      // Redirect to the home page after successful registration
      navigate('/home');
    } catch (error) {
      console.error('Error creating player:', error);
      if (error.response && error.response.status === 409) {
        setError('Username already exists. Please choose a different username.');
      } else {
        setError('An error occurred while creating the player.');
      }
    }
  };

  return (
    <div className='home-middle-section'>
      <div className='login-block'>
        <h1>Create a Perudo Game Online Account</h1>
        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3" controlId="formUsername">
            <Form.Label>Username</Form.Label>
            <Form.Control
              type="text"
              name="username"
              placeholder="Player1"
              value={formData.username}
              onChange={handleChange}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="formFirstName">
            <Form.Label>First Name</Form.Label>
            <Form.Control
              type="text"
              name="firstName"
              placeholder="Pepe"
              value={formData.firstName}
              onChange={handleChange}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="formLastName">
            <Form.Label>Last Name</Form.Label>
            <Form.Control
              type="text"
              name="lastName"
              placeholder="Jose"
              value={formData.lastName}
              onChange={handleChange}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="formPassword">
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              name="password"
              placeholder="********"
              value={formData.password}
              onChange={handleChange}
            />
          </Form.Group>
          <Form.Group className="mb-3" controlId="formConfirmPassword">
            <Form.Label>Confirm Password</Form.Label>
            <Form.Control
              type="password"
              name="confirmPassword"
              placeholder="********"
              value={formData.confirmPassword}
              onChange={handleChange}
            />
          </Form.Group>
          {error && <p className="text-danger">{error}</p>}
          <Button variant="outline-success" type="submit" id='connexion-button'>
            Create
          </Button>
        </Form>
      </div>
    </div>
  );
};

export default Register;
