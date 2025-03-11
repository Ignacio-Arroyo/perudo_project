import React from 'react';
import Form from 'react-bootstrap/Form';
import '../Home_middle_section/home_middle_section.css';
import '../Connexion/login.css';
import './register.css';
import Button from 'react-bootstrap/Button';

const Register = () => {
  return (
    <div className='home-middle-section'>
      <div className='login-block'>
        <h1>Create a Perudo Game Online Account</h1>
        <Form>
          <Form.Group className="mb-3" controlId="exampleForm.ControlInput1">
            <Form.Label>Username</Form.Label>
            <Form.Control type="username" placeholder="Player1" />
          </Form.Group>
          <Form.Group className="mb-3" controlId="exampleForm.ControlControlInput2">
            <Form.Label>Password</Form.Label>
            <Form.Control type="password" placeholder="********" />
          </Form.Group>
        </Form>
        <Button variant="outline-success" id='connexion-button'>Create</Button>
      </div>
    </div>
  );
};

export default Register;
