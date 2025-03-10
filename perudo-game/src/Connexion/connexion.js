import React from 'react';
import './connexion.css';
import Navbar from '../NavBar/navbar';
import Footer from '../Footer/footer';
import Log_in from './login';

const Connexion = () => {
    console.log("Connexion component rendered");
    return (
      <div className="App">
        <Log_in />
        <Footer />
      </div>
    );
  };
  

export default Connexion;
