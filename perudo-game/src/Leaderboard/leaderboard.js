import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './leaderboard.css';
import trophyIcon from '../assets/trophy-icon.png';

const Leaderboard = () => {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLeaderboard = async () => {
      try {
        setLoading(true);
        const response = await axios.get('http://localhost:8080/api/players/leaderboard');
        console.log('API response:', response.data);
        if (Array.isArray(response.data)) {
          setUsers(response.data);
        } else {
          console.error('API response is not an array:', response.data);
          setError('Error fetching leaderboard. Please try again later.');
        }
      } catch (error) {
        console.error('Error fetching leaderboard:', error);
        setError('Error fetching leaderboard. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchLeaderboard();
  }, []);

  // Fonction pour déterminer la couleur en fonction du classement
  const getRankColor = (index) => {
    if (index === 0) return 'gold-rank'; // 1er
    if (index === 1) return 'silver-rank'; // 2e
    if (index === 2) return 'bronze-rank'; // 3e
    return '';
  };

  if (loading) {
    return (
      <div className="leaderboard-container">
        <h1>Leaderboard</h1>
        <div className="loading">Chargement du classement...</div>
      </div>
    );
  }

  return (
    <div className="leaderboard-container">
      <h1>Leaderboard <img src={trophyIcon} alt="Trophy" className="trophy-icon" /></h1>
      {error && <p className="error-message">{error}</p>}
      
      <div className="leaderboard-info">
        <p>Classement basé sur le nombre de trophées gagnés par les joueurs.</p>
        <p>Gagnez des parties pour augmenter votre nombre de trophées !</p>
      </div>
      
      <table className="leaderboard-table">
        <thead>
          <tr>
            <th>Rang</th>
            <th>Joueur</th>
            <th>Trophées</th>
            <th>Pièces</th>
          </tr>
        </thead>
        <tbody>
          {users.length > 0 ? (
            users.map((user, index) => (
              <tr key={user.player_id} className={`leaderboard-row ${getRankColor(index)}`}>
                <td className="rank">{index + 1}</td>
                <td className="player-info">
                  <span className="username">{user.username}</span>
                  <span className="player-details">{user.nom} {user.prenom}</span>
                </td>
                <td className="trophies">
                  <span className="trophy-count">{user.trophies}</span>
                </td>
                <td className="pieces">{user.pieces}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="4">Aucun joueur trouvé.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default Leaderboard;
