import React from 'react';
import { useNavigate } from 'react-router-dom';
import './home.css';

const Home = () => {
  const navigate = useNavigate();

  const handleNavigation = (path) => {
    navigate(path);
  };

  return (
    <div className="home-container">
      <div className="profile-block" onClick={() => handleNavigation('/profile')}>
        Profile
      </div>
      <div className="blocks-container">
        <div className="block" onClick={() => handleNavigation('/shop')}>
          Shop
        </div>
        <div className="block" onClick={() => handleNavigation('/inventory')}>
          Inventory
        </div>
        <div className="block" onClick={() => handleNavigation('/leaderboard')}>
          Leaderboard
        </div>
        <div className="block" onClick={() => handleNavigation('/friends')}>
          Friends
        </div>
        <div className="block" onClick={() => handleNavigation('/statistics')}>
          Statistics
        </div>
        <div className="block" onClick={() => handleNavigation('/match-history')}>
          Match History
        </div>
      </div>
    </div>
  );
};

export default Home;
