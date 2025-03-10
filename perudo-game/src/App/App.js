import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from '../NavBar/navbar';
import Footer from '../Footer/footer';
import Home_middle_section from '../Home_middle_section/home_middle_section';
import Connexion from '../Connexion/connexion';

function App() {
  return (
    <Router>
      <div className="App">
        <Navbar sticky="top" />
        <Routes>
          <Route path="/" element={<Home_middle_section />} />
          <Route path="/connexion" element={<Connexion />} />
        </Routes>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
