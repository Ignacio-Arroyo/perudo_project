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
import Inventory from '../Inventory/Inventory';
import { AuthProvider } from '../Auth/authcontext';
import TestCoins from './TestCoins';
import UpdatePlayerCoinsPage from './UpdatePlayerCoinsPage';


function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Navbar sticky="top" />
          <Routes>
            <Route path="/" element={<Home_middle_section />} />
            <Route path="/connexion" element={<Connexion />} />
            <Route path="/register" element={<Register />} />
            <Route path="/lobby" element={<Lobby />} />
            <Route path="/profile" element={<Profile_page />} />
            <Route path="/home" element={<Home />} />
            <Route path="/leaderboard" element={<Leaderboard />} />
            <Route path="/shop" element={<Shop />} />
            <Route path="/products" element={<ProductList />} />
            <Route path="/inventory" element={<Inventory />} />
            <Route path="/test-coins" element={<TestCoins />} />
            <Route path="/update-player-coins" element={<UpdatePlayerCoinsPage />} />
            <Route path="/friends" element={<FriendsPage />} />
            <Route path="/mail" element={<MailPage />} />
            <Route path="/game/board" element={<GameBoard />} />
            {/* <Route path="/statistics" element={<Statistics />} /> */}
          </Routes> 
          <Footer  />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
