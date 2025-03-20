import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from '../NavBar/navbar';
import Footer from '../Footer/footer';
import Home_middle_section from '../Home_middle_section/home_middle_section';
import Connexion from '../Connexion/connexion';
import Register from '../Register/register';
import Lobby from '../Lobby/lobby';

//copied from the restfull api example
async function invokePost(method, data, successMsg, failureMsg, setMessage) {
  const requestOptions = {
       method: "POST",
       headers: { "Content-Type": "application/json; charset=utf-8" },
       body: JSON.stringify(data)
   };
   const res = await fetch("perudo/"+method,requestOptions);
   setMessage(res.ok ? successMsg : failureMsg);
}
//copied from the restfull api example
async function invokeGet(method, failureMsg, setMessage) {
 const res = await fetch("perudo/"+method);
 if (res.ok) return await res.json();	
 setMessage(failureMsg);
 return null;
}  


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

        </Routes>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
