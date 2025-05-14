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
import ProductList from '../ProductList/product_list';


function App() {
  return (
    <Router>
      <div className="App">
        <Navbar sticky="top" />
        <Routes>
          <Route path="/" element={<Home_middle_section />} />
          <Route path="/connexion" element={<Connexion />} />
          <Route path="/register" element={<Register />} />
          <Route path="/lobby" element={<Lobby />} />
          <Route path="/profile" element={<Profile_page playerId={4} />} />
          <Route path="/home" element={<Home />} />
          <Route path="/leaderboard" element={<Leaderboard />} />
          <Route path="/shop" element={<Shop />} />
          <Route path="/products" element={<ProductList />} />
          {/* <Route path="/statistics" element={<Statistics />} /> */}


        </Routes>
        <Footer  />
      </div>
    </Router>
  );
}

export default App;
