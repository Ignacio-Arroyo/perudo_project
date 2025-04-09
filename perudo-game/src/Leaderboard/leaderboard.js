import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './leaderboard.css';

const Leaderboard = () => {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/players');
        setUsers(response.data);
      } catch (error) {
        console.error('Error fetching users:', error);
        setError('Error fetching users. Please try again later.');
      }
    };

    fetchUsers();
  }, []);

  return (
    <div className="leaderboard-container">
      <h1>Leaderboard</h1>
      {error && <p className="error-message">{error}</p>}
      <ul className="users-list">
        {users.map((user) => (
          <li key={user.player_id} className="user-item">
            <div className="user-details">
              <h2>{user.username}</h2>
              <p>{user.nom} {user.prenom}</p>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Leaderboard;
