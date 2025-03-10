import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import Navbar from '../NavBar/navbar';
import './App.css'; // Optional: Add some basic styling


function App() {
  return (
    <Router>
      <div className="App">
        <Navbar sticky="top" />
      </div>
    </Router>
  );
}

export default App;
