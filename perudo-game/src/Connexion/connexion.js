import React from 'react';
import './connexion.css';
import Log_in from './login';

const Connexion = () => {
    console.log("Connexion component rendered");
    return (
        <Log_in />
    );
  };
  

export default Connexion;

//possible de supprimer completement le composant connexion et garder que Log in