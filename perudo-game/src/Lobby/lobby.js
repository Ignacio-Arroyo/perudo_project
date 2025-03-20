import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from '../NavBar/navbar';
import Footer from '../Footer/footer';
import Queue_box from '../Queue_box/queue_box';


const Lobby=()=> {
  return (
    
      
        <Queue_box/>
       
    
  );
}

export default Lobby;
