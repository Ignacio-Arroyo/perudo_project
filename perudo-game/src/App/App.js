import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
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
import ProductList from '../ProductList/product_list';
import { UserProvider } from '../Auth/UserContext';
import ProtectedRoute from '../Auth/ProtectedRoute';

function App() {
  return (
    <UserProvider>
      <Router>
        <div className="App">
          <Navbar sticky="top" />
          <Routes>
            <Route path="/" element={<Home_middle_section />} />
            <Route path="/connexion" element={<Connexion />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/lobby"
              element={
                <ProtectedRoute>
                  <Lobby />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <Profile_page />
                </ProtectedRoute>
              }
            />
            <Route
              path="/home"
              element={
                <ProtectedRoute>
                  <Home />
                </ProtectedRoute>
              }
            />
            <Route
              path="/leaderboard"
              element={
                <ProtectedRoute>
                  <Leaderboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/shop"
              element={
                <ProtectedRoute>
                  <Shop />
                </ProtectedRoute>
              }
            />
            <Route
              path="/products"
              element={
                <ProtectedRoute>
                  <ProductList />
                </ProtectedRoute>
              }
            />
            <Route
              path="/friends"
              element={
                <ProtectedRoute>
                  <FriendsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/mail"
              element={
                <ProtectedRoute>
                  <MailPage />
                </ProtectedRoute>
              }
            />
          </Routes>
          <Footer />
        </div>
      </Router>
    </UserProvider>
  );
}

export default App;
