import React from 'react';
import Form from 'react-bootstrap/Form';
import '../Home_middle_section/home_middle_section.css';
import './login.css';

function Log_in() {
  return (
    <div className='home-middle-section'>
      <div className='login-block'>
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
      </div>
    </div>
  );
}

export default Log_in;
