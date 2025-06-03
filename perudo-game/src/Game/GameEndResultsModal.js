import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom'; // Assuming you use React Router for navigation
import './GameEndResultsModal.css'; // We will create this CSS file later
import { useAuth } from '../Auth/authcontext'; // Import useAuth

const DiceFace = ({ value }) => {
    // Simple dice face display, can be enhanced
    const diceSymbols = ['', '⚀', '⚁', '⚂', '⚃', '⚄', '⚅'];
    return <span className="dice-face-small">{diceSymbols[value] || value}</span>;
};

const GameEndResultsModal = ({ results, onClose, currentUser }) => {
    const navigate = useNavigate();
    const { updateUser } = useAuth(); // Get updateUser from AuthContext

    useEffect(() => {
        if (results && results.playerResults && currentUser && updateUser) {
            const currentPlayerResult = results.playerResults.find(pr => pr.playerId === currentUser.id);
            if (currentPlayerResult) {
                const updates = {};
                if (currentPlayerResult.totalPieces !== undefined) {
                    updates.pieces = currentPlayerResult.totalPieces;
                }
                if (currentPlayerResult.totalTrophies !== undefined) {
                    updates.trophies = currentPlayerResult.totalTrophies;
                }
                // Add other stats if they need to be updated in AuthContext user object
                // For example, gamesPlayed, gamesWon, etc., if they are part of playerResults
                // and are meant to be the new totals.
                // Example: if (currentPlayerResult.totalGamesPlayed !== undefined) {
                // updates.gamesPlayed = currentPlayerResult.totalGamesPlayed;
                // }

                if (Object.keys(updates).length > 0) {
                    console.log("[GameEndResultsModal] Updating auth context for user:", currentUser.id, "with data:", updates);
                    updateUser(updates);
                }
            }
        }
    }, [results, currentUser, updateUser]); // Depend on results, currentUser, and updateUser

    if (!results || !results.playerResults) {
        return null; // Or some loading/error state
    }

    const handleReturnHome = () => {
        if (onClose) onClose(); // Call original onClose if provided
        navigate('/'); // Navigate to home page
    };
    
    // Sort player results by finalPosition
    const sortedPlayerResults = [...results.playerResults].sort((a, b) => a.finalPosition - b.finalPosition);

    return (
        <div className="modal-overlay">
            <div className="modal-content game-end-modal">
                <h2>Game Over!</h2>
                {results.gameFinished && sortedPlayerResults.length > 0 && (
                    <p className="winner-announcement">
                        <strong>Winner: {sortedPlayerResults[0].username}</strong>
                    </p>
                )}
                <hr />
                <h3>Final Standings:</h3>
                <table className="results-table">
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Player</th>
                            <th>Points Earned</th>
                            <th>Coins Earned</th>
                            <th>Dice Left</th>
                            <th>Challenges (Won/Total)</th>
                            <th>Eliminations</th>
                            {/* Potentially add columns for Total Pieces / Trophies if desired in table */}
                        </tr>
                    </thead>
                    <tbody>
                        {sortedPlayerResults.map((playerResult) => (
                            <tr key={playerResult.playerId} className={currentUser && currentUser.id === playerResult.playerId ? 'current-user-row' : ''}>
                                <td>{playerResult.finalPosition}</td>
                                <td>{playerResult.username}</td>
                                <td>{playerResult.pointsEarned}</td>
                                <td>{playerResult.coinsEarned}</td>
                                <td>{playerResult.diceRemaining}</td>
                                <td>{`${playerResult.successfulChallenges} / ${playerResult.totalChallenges}`}</td>
                                <td>{playerResult.playersEliminated}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                
                {sortedPlayerResults.map((playerResult) => (
                    playerResult.performanceMessage && (
                        <p key={`msg-${playerResult.playerId}`} className="performance-message">
                            <strong>{playerResult.username}'s summary:</strong> {playerResult.performanceMessage}
                        </p>
                    )
                ))}

                <hr />
                <button onClick={handleReturnHome} className="return-home-button">
                    Return to Home
                </button>
                {/* Optionally, a close button that only closes the modal without navigating */}
                {/* <button onClick={onClose}>Close Results</button> */}
            </div>
        </div>
    );
};

export default GameEndResultsModal; 