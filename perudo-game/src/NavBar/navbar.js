import React, { useContext } from 'react';
import Button from 'react-bootstrap/Button';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import { Link, useNavigate } from 'react-router-dom';
import { UserContext } from '../Auth/UserContext';
import './navbar.css';

function NavScrollExample() {
  const { user, logout } = useContext(UserContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/connexion');
  };

  return (
    <Navbar expand="lg" className="bg-body-tertiary" data-bs-theme="dark">
      <Container fluid>
        <Navbar.Brand href="/">Perudo</Navbar.Brand>
        <Navbar.Toggle aria-controls="navbarScroll" />
        <Navbar.Collapse id="navbarScroll">
          <Nav
            className="me-auto my-2 my-lg-0"
            style={{ maxHeight: '100px' }}
            navbarScroll
          >
            <Nav.Link as={Link} to="/home">Home</Nav.Link>
            <Nav.Link href="#action2">Rules</Nav.Link>
          </Nav>
        </Navbar.Collapse>

        <Link to="/lobby">
          <Button variant="outline-success" id="lobby-button">
            Play
          </Button>
        </Link>
        {user ? (
          <Button variant="outline-success" id="logout-button" onClick={handleLogout}>
            Log Out
          </Button>
        ) : (
          <>
            <Link to="/connexion">
              <Button variant="outline-success" id="connexion-button">
                Log In
              </Button>
            </Link>
            <Link to="/register">
              <Button variant="outline-success" id="register-button">
                Register
              </Button>
            </Link>
          </>
        )}
      </Container>
    </Navbar>
  );
}

export default NavScrollExample;
