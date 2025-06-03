import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './MatchHistory.css';

const MatchHistory = () => {
    const [matchHistory, setMatchHistory] = useState([]);
    const [matchStats, setMatchStats] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showAll, setShowAll] = useState(false);

    const user = JSON.parse(localStorage.getItem('user'));

    useEffect(() => {
        if (user && user.id) {
            fetchMatchHistory();
            fetchMatchStats();
        } else {
            setError('Utilisateur non connecté');
            setLoading(false);
        }
    }, [user?.id, showAll]);

    const fetchMatchHistory = async () => {
        try {
            setLoading(true);
            const endpoint = showAll 
                ? `http://localhost:8080/api/match-history/${user.id}`
                : `http://localhost:8080/api/match-history/${user.id}/recent?limit=20`;
            
            const response = await axios.get(endpoint);
            setMatchHistory(response.data);
            setError(null);
        } catch (err) {
            console.error('Error fetching match history:', err);
            setError('Erreur lors du chargement de l\'historique des matchs');
        } finally {
            setLoading(false);
        }
    };

    const fetchMatchStats = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/match-history/${user.id}/stats`);
            setMatchStats(response.data);
        } catch (err) {
            console.error('Error fetching match stats:', err);
        }
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getResultIcon = (won) => {
        return won ? '🏆' : '❌';
    };

    const getResultClass = (won) => {
        return won ? 'match-won' : 'match-lost';
    };

    const getScoreChangeDisplay = (scoreChange) => {
        if (scoreChange > 0) {
            return `+${scoreChange}`;
        }
        return scoreChange.toString();
    };

    if (loading) {
        return (
            <div className="match-history-container">
                <div className="loading">Chargement de l'historique des matchs...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="match-history-container">
                <div className="error-message">{error}</div>
            </div>
        );
    }

    return (
        <div className="match-history-container">
            <div className="match-history-header">
                <h1>Historique des Matchs</h1>
                <div className="user-info">
                    <h2>Joueur : {user?.username}</h2>
                </div>
            </div>

            {/* Statistiques générales */}
            <div className="match-stats-summary">
                <div className="stat-card">
                    <h3>Total Matchs</h3>
                    <p className="stat-value">{matchStats.totalGames || 0}</p>
                </div>
                <div className="stat-card">
                    <h3>Victoires</h3>
                    <p className="stat-value win">{matchStats.gamesWon || 0}</p>
                </div>
                <div className="stat-card">
                    <h3>Taux de Victoire</h3>
                    <p className="stat-value">{matchStats.winRate ? `${matchStats.winRate.toFixed(1)}%` : '0%'}</p>
                </div>
            </div>

            {/* Contrôles d'affichage */}
            <div className="match-controls">
                <button 
                    className={`toggle-button ${!showAll ? 'active' : ''}`}
                    onClick={() => setShowAll(false)}
                >
                    20 Derniers Matchs
                </button>
                <button 
                    className={`toggle-button ${showAll ? 'active' : ''}`}
                    onClick={() => setShowAll(true)}
                >
                    Tout l'Historique
                </button>
            </div>

            {/* Liste des matchs */}
            <div className="match-history-list">
                {matchHistory.length === 0 ? (
                    <div className="no-matches">
                        <p>Aucun match joué pour le moment.</p>
                        <p>Commencez à jouer pour voir votre historique ici !</p>
                    </div>
                ) : (
                    <div className="matches-container">
                        <div className="matches-header">
                            <span>Résultat</span>
                            <span>Date</span>
                            <span>Points</span>
                        </div>
                        {matchHistory.map((match) => (
                            <div key={match.id} className={`match-item ${getResultClass(match.won)}`}>
                                <div className="match-result">
                                    <span className="result-icon">{getResultIcon(match.won)}</span>
                                    <span className="result-text">
                                        {match.won ? 'Victoire' : 'Défaite'}
                                    </span>
                                </div>
                                <div className="match-date">
                                    {formatDate(match.playedAt)}
                                </div>
                                <div className={`match-score ${match.scoreChange >= 0 ? 'positive' : 'negative'}`}>
                                    {getScoreChangeDisplay(match.scoreChange)} pts
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default MatchHistory; 