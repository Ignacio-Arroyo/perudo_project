import React, { useState } from 'react';
import axios from 'axios';
import './statistics.css';

const Statistics = () => {
  const [username, setUsername] = useState('');
  const [userStats, setUserStats] = useState(null);
  const [error, setError] = useState('');

  const handleUsernameChange = (e) => {
    setUsername(e.target.value);
  };

  const fetchUserStats = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const response = await axios.get(`http://localhost:8080/api/players/stats`, {
        params: { username: username }
      });
      setUserStats(response.data);
    } catch (error) {
      console.error('Error fetching user stats:', error);
      setError('User not found or an error occurred.');
    }
  };

  return (
    <div className="statistics-container">
      <form onSubmit={fetchUserStats} className="username-form">
        <input
          type="text"
          placeholder="Enter username"
          value={username}
          onChange={handleUsernameChange}
          className="username-input"
        />
        <button type="submit" className="fetch-button">Fetch Statistics</button>
      </form>
      {error && <p className="error-message">{error}</p>}
      {userStats && (
        <div className="stats-details">
          <h2>{userStats.username}'s Statistics</h2>
          <p>Total Games Played: {userStats.totalGamesPlayed}</p>
          <p>Total Wins: {userStats.totalWins}</p>
          <p>Win Rate: {userStats.winRate}%</p>
          {/* Add more statistics fields as needed */}
        </div>
      )}
    </div>
  );
};

export default Statistics;
