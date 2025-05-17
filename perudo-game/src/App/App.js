import React, { useContext, useState, useEffect, useCallback } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { StompSessionProvider } from 'react-stomp-hooks';
import Navbar from '../NavBar/navbar';
import Footer from '../Footer/footer';
import Home_middle_section from '../Home_middle_section/home_middle_section';
import Connexion from '../Connexion/connexion';
import Register from '../Register/register';
import Lobby from '../Lobby/lobby';
import Profile_page from '../Profile_page/profile_page';
import Home from '../Home/home';
import Leaderboard from '../Leaderboard/leaderboard';
import Shop from '../Shop/shop';
import FriendsPage from '../Friend_page/friend_page';
import MailPage from '../Mail/mail';
import GameSearchPage from '../Game/GameSearchPage';
import GamePage from '../Game/GamePage';
import GameBoard from '../Game/GameBoard';
import ProductList from '../ProductList/product_list';
import { UserProvider, UserContext } from '../Auth/UserContext';
import ProtectedRoute from '../Auth/ProtectedRoute';
import WebSocketTestPage from '../App/WebSocketTest/websockettest';
import { Client } from '@stomp/stompjs';
import PerudoGame from '../Game/PerudoGame';

function App() {
  return (
    <UserProvider>
      <WebSocketProvider />
    </UserProvider>
  );
}

// Create a separate component for WebSocket functionality
function WebSocketProvider({ children }) {
  const { user } = useContext(UserContext);
  const [stompClient, setStompClient] = useState(null);
  const [connectionStatus, setConnectionStatus] = useState('disconnected');

  const connectToWebSocket = useCallback(() => {
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws/websocket',
      connectHeaders: {
        'Authorization': user?.token ? `Bearer ${user.token}` : ''
      },
      debug: (str) => {
        console.log('WebSocket Debug:', str);
      },
      onConnect: () => {
        console.log('WebSocket Connected');
        setConnectionStatus('connected');
        // Subscribe to relevant topics
        client.subscribe('/topic/game', (message) => {
          console.log('Game update:', message.body);
        });
      },
      onDisconnect: () => {
        console.log('WebSocket Disconnected');
        setConnectionStatus('disconnected');
      },
      onStompError: (frame) => {
        console.error('STOMP Error:', frame);
        setConnectionStatus('error');
      },
      onWebSocketError: (event) => {
        console.error('WebSocket Error:', event);
        setConnectionStatus('error');
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    client.beforeConnect = () => {
      console.log('Attempting to connect...');
    };

    return client;
  }, [user]);

  useEffect(() => {
    let client;
    try {
      client = connectToWebSocket();
      setStompClient(client);
      client.activate();
    } catch (error) {
      console.error('Error creating STOMP client:', error);
      setConnectionStatus('error');
    }

    return () => {
      if (client?.active) {
        client.deactivate()
          .catch(error => console.error('Error deactivating client:', error));
      }
    };
  }, [connectToWebSocket]);

  // Display connection status
  return (
    <StompSessionProvider
      url={process.env.REACT_APP_WEBSOCKET_URL || 'ws://localhost:8080/ws'}
      connectHeaders={{
        'Authorization': user?.token ? `Bearer ${user.token}` : ''
      }}
      debug={(str) => {
        console.log('WebSocket Debug:', str);
      }}
      onConnect={() => {
        console.log('WebSocket Connected');
        setConnectionStatus('connected');
      }}
      onDisconnect={() => {
        console.log('WebSocket Disconnected');
        setConnectionStatus('disconnected');
      }}
      onError={(error) => {
        console.error('WebSocket Error:', error);
        setConnectionStatus('error');
      }}
      reconnectDelay={5000}
      heartbeatIncoming={4000} // Adjust heartbeat settings
      heartbeatOutgoing={4000} // Adjust heartbeat settings
    >
      {/* Display connection status */}
      {connectionStatus === 'connected' && (
        <div style={{ color: 'green', padding: '10px' }}>
          WebSocket Connected
        </div>
      )}
      {connectionStatus === 'disconnected' && (
        <div style={{ color: 'orange', padding: '10px' }}>
          WebSocket Disconnected
        </div>
      )}
      {connectionStatus === 'error' && (
        <div style={{ color: 'red', padding: '10px' }}>
          WebSocket connection failed. Please check if the server is running.
        </div>
      )}

      <Router>
        <div className="App">
          <Navbar sticky="top" />
          <Routes>
            <Route path="/" element={<Home_middle_section />} />
            <Route path="/connexion" element={<Connexion />} />
            <Route path="/register" element={<Register />} />
            <Route path="/lobby" element={<ProtectedRoute><Lobby /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><Profile_page /></ProtectedRoute>} />
            <Route path="/home" element={<ProtectedRoute><Home /></ProtectedRoute>} />
            <Route path="/leaderboard" element={<ProtectedRoute><Leaderboard /></ProtectedRoute>} />
            <Route path="/shop" element={<ProtectedRoute><Shop /></ProtectedRoute>} />
            <Route path="/products" element={<ProtectedRoute><ProductList /></ProtectedRoute>} />
            <Route path="/friends" element={<ProtectedRoute><FriendsPage /></ProtectedRoute>} />
            <Route path="/mail" element={<ProtectedRoute><MailPage /></ProtectedRoute>} />
            <Route path="/game/search" element={<ProtectedRoute><GameSearchPage /></ProtectedRoute>} />
            <Route path="/game/:gameId" element={<ProtectedRoute><GamePage /></ProtectedRoute>} />
            <Route path="/game/board" element={<GameBoard />} />
            <Route path="/websocket-test" element={<WebSocketTestPage />} />
            <Route path="/perudogame/:gameId" element={<PerudoGame />} />
            
            
          </Routes>
          <Footer />
        </div>
      </Router>
    </StompSessionProvider>

  );
}

export default App;
