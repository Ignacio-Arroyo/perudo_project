import React from 'react';
import Button from 'react-bootstrap/Button';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import { Link, useNavigate } from 'react-router-dom';
import './navbar.css';
import { useAuth } from '../Auth/authcontext';


function NavScrollExample() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <Navbar expand="lg" className="bg-body-tertiary" data-bs-theme="dark">
      <Container fluid>
        <Navbar.Brand as={Link} to="/">Perudo</Navbar.Brand>
        <Navbar.Toggle aria-controls="navbarScroll" />
        <Navbar.Collapse id="navbarScroll">
          <Nav
            className="me-auto my-2 my-lg-0"
            style={{ maxHeight: '100px' }}
            navbarScroll
          >
            <Nav.Link as={Link} to="/home">Home</Nav.Link>
            <Nav.Link as={Link} to="/leaderboard">Leaderboard</Nav.Link>
            <Nav.Link as={Link} to="/shop">Shop</Nav.Link>
            {isAuthenticated && (
              <>
                <Nav.Link as={Link} to="/inventory">Inventory</Nav.Link>
                <Nav.Link as={Link} to="/friends">Friends</Nav.Link>
              </>
            )}
          </Nav>
          
          {isAuthenticated ? (
            <Nav className="ms-auto">
              <NavDropdown title={user?.username || "Account"} id="nav-dropdown-account">
                <NavDropdown.Item as={Link} to="/profile">Profile</NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/mail">Messages</NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item onClick={handleLogout}>Logout</NavDropdown.Item>
              </NavDropdown>
              <Link to="/lobby" className="ms-2">
                <Button variant="outline-success" id="lobby-button">
                  Play
                </Button>
              </Link>
            </Nav>
          ) : (
            <>
              <Link to="/lobby" className="me-2">
                <Button variant="outline-success" id="lobby-button">
                  Play
                </Button>
              </Link>
              <Link to="/connexion" className="me-2">
                <Button variant="outline-success" id="connexion-button">Log In</Button>
              </Link>
              <Link to="/register">
                <Button variant="outline-success" id="connexion-button">Register</Button>
              </Link>
            </>
          )}
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default NavScrollExample;
