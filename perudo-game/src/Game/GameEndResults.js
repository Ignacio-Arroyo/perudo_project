import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './gameendresults.css';

const GameEndResults = ({ gameId, gameState, allOriginalPlayers, onSpectate, onClose }) => {
    console.log('GameEndResults: Component RENDERED with props:', { gameId, gameState, allOriginalPlayers });

    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        console.log('GameEndResults: useEffect TRIGGERED with gameId:', gameId, 'allOriginalPlayers:', allOriginalPlayers);
        if (gameId && allOriginalPlayers && allOriginalPlayers.length > 0) {
            fetchGameEndResults();
        } else {
            console.log('GameEndResults: useEffect - conditions not met to fetch results.');
            setLoading(false);
        }
    }, [gameId, allOriginalPlayers]);

    const fetchGameEndResults = async () => {
        console.log('GameEndResults: fetchGameEndResults CALLED');
        try {
            setLoading(true);
            const payload = {
                players: allOriginalPlayers.map(player => ({
                    id: player.id,
                    username: player.username,
                    currentGameChallenges: player.currentGameChallenges || 0,
                    currentGameSuccessfulChallenges: player.currentGameSuccessfulChallenges || 0,
                    currentGameEliminatedPlayers: player.currentGameEliminatedPlayers || 0
                }))
            };
            console.log('GameEndResults: Sending to /end-results:', payload);
            const response = await axios.post(`http://localhost:8080/api/games/${gameId}/end-results`, payload);
            
            console.log('GameEndResults: Received from /end-results:', response.data);
            setResults(response.data);
        } catch (err) {
            console.error('GameEndResults: Error fetching game end results:', err.response ? err.response.data : err.message, err);
            setError('Erreur lors du calcul des résultats. Veuillez réessayer.');
        } finally {
            console.log('GameEndResults: fetchGameEndResults FINISHED');
            setLoading(false);
        }
    };

    const handleGoHome = () => {
        navigate('/home');
    };

    const handleViewLeaderboard = () => {
        navigate('/leaderboard');
    };

    const handleSpectate = () => {
        if (onSpectate) {
            onSpectate();
        }
    };

    const getRankClass = (position) => {
        if (position === 1) return 'first-place';
        if (position === 2) return 'second-place';
        if (position === 3) return 'third-place';
        return '';
    };

    const getRankPositionClass = (position) => {
        if (position === 1) return 'first';
        if (position === 2) return 'second';
        if (position === 3) return 'third';
        return '';
    };

    const getRankEmoji = (position) => {
        if (position === 1) return '🏆';
        if (position === 2) return '🥈';
        if (position === 3) return '🥉';
        return position;
    };

    const hasRemainingPlayers = gameState && gameState.players && gameState.players.length > 1;

    if (loading) {
        return (
            <div className="game-end-modal">
                <div className="game-end-content">
                    <div className="loading-spinner">
                        <div className="spinner"></div>
                        <p>Calcul des résultats...</p>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="game-end-modal">
                <div className="game-end-content">
                    <div className="error-message">
                        <h3>Erreur</h3>
                        <p>{error}</p>
                        <div className="game-end-actions">
                            <button onClick={handleGoHome} className="action-button btn-home">
                                🏠 Retour à l'accueil
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (!results) {
        return null;
    }

    console.log('GameEndResults: Rendering with playerResults:', results.playerResults);

    return (
        <div className="game-end-modal">
            <div className="game-end-content">
                <div className="game-end-header">
                    <h1 className="game-end-title">Fin de Partie</h1>
                    <p className="game-end-subtitle">Résultats et classement final</p>
                </div>

                <div className="rankings-container">
                    {results.playerResults.map((playerResult, index) => (
                        <div 
                            key={playerResult.playerId} 
                            className={`ranking-item ${getRankClass(playerResult.finalPosition)}`}
                        >
                            <div className="player-rank">
                                <div className={`rank-position ${getRankPositionClass(playerResult.finalPosition)}`}>
                                    {getRankEmoji(playerResult.finalPosition)}
                                </div>
                                <div className="player-info">
                                    <div className="player-name">{playerResult.username}</div>
                                    {playerResult.performanceMessage && (
                                        <div className="performance-message">
                                            {playerResult.performanceMessage}
                                        </div>
                                    )}
                                    <div className="player-stats">
                                        <span className="stat-item">
                                            {playerResult.totalChallenges} challenges
                                        </span>
                                        <span className="stat-item">
                                            {playerResult.successfulChallenges} réussis
                                        </span>
                                        <span className="stat-item">
                                            {playerResult.playersEliminated} éliminations
                                        </span>
                                        <span className="stat-item">
                                            {playerResult.diceRemaining} dés restants
                                        </span>
                                    </div>
                                </div>
                            </div>
                            
                            <div className="player-rewards">
                                <div className={`points-earned ${playerResult.pointsEarned >= 0 ? 'positive' : 'negative'}`}>
                                    {playerResult.pointsEarned >= 0 ? '+' : ''}{playerResult.pointsEarned} pts
                                </div>
                                {playerResult.coinsEarned > 0 && (
                                    <div className="coins-earned">
                                        +{playerResult.coinsEarned} 🪙
                                    </div>
                                )}
                                <div className="total-rewards">
                                    {playerResult.finalPosition === 1 ? 'Victoire !' : 
                                     playerResult.finalPosition <= 3 ? 'Podium !' : 
                                     'Participation'}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="game-end-actions">
                    <button 
                        onClick={handleGoHome} 
                        className="action-button btn-home"
                    >
                        🏠 Retour à l'accueil
                    </button>
                    
                    <button 
                        onClick={handleViewLeaderboard} 
                        className="action-button btn-leaderboard"
                    >
                        🏆 Voir le classement
                    </button>

                    {hasRemainingPlayers && (
                        <button 
                            onClick={handleSpectate} 
                            className="action-button btn-spectate"
                        >
                            👁️ Continuer à regarder
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default GameEndResults; 