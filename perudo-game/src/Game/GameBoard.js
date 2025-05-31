import React, { useState, useEffect, useContext, useRef } from 'react';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from '../Auth/authcontext';
import GameEndResults from './GameEndResults';
import './gameboard.css';

const axiosConfig = {
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};

const GameBoard = () => {
    // All useState and useRef hooks grouped here
    const { user } = useAuth();
    const [gameState, setGameState] = useState({
        id: null,
        status: 'WAITING',
        players: [],
        turnSequence: [],
        currentPlayer: null,
        currentBid: null,
        round: 0 // Added round to gameState for dice refresh logic
    });
    const [playerId, setPlayerId] = useState(null);
    const [dice, setDice] = useState([]);
    const [bid, setBid] = useState({ quantity: 1, value: 2 }); // Not directly used for input anymore, but kept for potential future use
    const [stompClient, setStompClient] = useState(null);
    const [error, setError] = useState(null);
    const [hasRolled, setHasRolled] = useState(false);
    const [bidInput, setBidInput] = useState({
        quantity: 1,
        value: 1 // Default to 1x1 for initial bid or after reset
    });
    const [challengeResult, setChallengeResult] = useState(null);
    const [showChallengeResult, setShowChallengeResult] = useState(false);
    const challengeResultTimerRef = useRef(null);

    // Game end and scoring state
    const [allOriginalPlayers, setAllOriginalPlayers] = useState([]);
    const [showGameEndResults, setShowGameEndResults] = useState(false);
    const [isSpectating, setIsSpectating] = useState(false);

    // All useEffect hooks grouped here
    useEffect(() => { // Turn sequence initialization
        if (gameState.status === 'PLAYING' && (!gameState.turnSequence || !gameState.turnSequence.length) && gameState.players?.length > 0) {
            console.log('Initializing turn sequence from players:', gameState.players);
            setGameState(prev => ({ ...prev, turnSequence: [...prev.players] }));
        }
    }, [gameState.status, gameState.players]);

    useEffect(() => { // STOMP connection and subscriptions
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);
        
        client.connect({}, () => {
            setStompClient(client);
            
            client.subscribe('/topic/lobby', (message) => {
                console.log('Raw message received:', message);
                try {
                    const gameData = JSON.parse(message.body);
                    if (!gameData || !gameData.id || gameData.id === 'null') {
                        console.error('Invalid game ID received from lobby:', gameData);
                        return;
                    }
                    console.log('Setting game state from lobby with valid ID:', gameData.id);
                    setGameState(gameData);
                } catch (error) {
                    console.error('Error processing game data from lobby:', error);
                }
            });
        }, (stompError) => {
            console.error('STOMP connection error:', stompError);
            setError('Failed to connect to game server. Please refresh.');
        });

        return () => {
            if (client) {
                client.disconnect();
            }
        };
    }, []); // Empty dependency array for STOMP setup

    useEffect(() => { // Track original players when game starts
        if (gameState.status === 'PLAYING' && allOriginalPlayers.length === 0 && gameState.players?.length > 0) {
            const originalPlayers = gameState.players.map(p => ({
                id: p.id,
                username: p.username,
                currentGameChallenges: 0,
                currentGameSuccessfulChallenges: 0,
                currentGameEliminatedPlayers: 0
            }));
            setAllOriginalPlayers(originalPlayers);
            console.log('Stored original players:', originalPlayers);
        }
    }, [gameState.status, gameState.players, allOriginalPlayers.length]);

    useEffect(() => { // Check for game end
        if (gameState.status === 'FINISHED' && !showGameEndResults && !isSpectating) {
            console.log('Game finished, showing end results');
            setShowGameEndResults(true);
        }
    }, [gameState.status, showGameEndResults, isSpectating]);

    // All helper functions grouped here
    const trackChallengeActivity = (challengerId, isSuccessful, losingPlayerId) => {
        setAllOriginalPlayers(prevPlayers => 
            prevPlayers.map(player => {
                if (String(player.id) === String(challengerId)) {
                    const playerLosing = gameState.players?.find(p => String(p.id) === String(losingPlayerId));
                    const willBeEliminated = playerLosing && playerLosing.dice && playerLosing.dice.length <= 1;
                    return {
                        ...player,
                        currentGameChallenges: player.currentGameChallenges + 1,
                        currentGameSuccessfulChallenges: isSuccessful ? 
                            player.currentGameSuccessfulChallenges + 1 : 
                            player.currentGameSuccessfulChallenges,
                        currentGameEliminatedPlayers: isSuccessful && willBeEliminated ?
                            player.currentGameEliminatedPlayers + 1 : 
                            player.currentGameEliminatedPlayers
                    };
                }
                return player;
            })
        );
    };

    const handleSpectate = () => {
        setIsSpectating(true);
        setShowGameEndResults(false); // Close results modal if choosing to spectate
    };

    const handleGameEndResultsClose = () => { // This function might be passed to GameEndResults if a close button independent of navigation is needed
        setShowGameEndResults(false);
        // Decide what happens next, e.g., navigate to home or allow spectating
    };
    
    const createGame = () => {
        if (stompClient) {
            stompClient.send("/app/game/create", {}, JSON.stringify({}));
        } else {
            console.error('WebSocket connection not established for createGame');
        }
    };

    const joinGame = async (gameIdToJoin, userId) => {
        if (!gameIdToJoin || !userId) {
            console.error('Missing data for join:', { gameIdToJoin, userId });
            return;
        }
        
        try {
            const response = await axios.post(
                `http://localhost:8080/api/games/${gameIdToJoin}/join/${userId}`,
                {},
                axiosConfig
            );
            setPlayerId(userId);
            setGameState(response.data);
            
            // Clear previous subscriptions before making new ones for this game
            // This requires managing subscription objects if StompJS allows individual unsubscribes easily,
            // or careful conditional subscribing. For simplicity, ensure client.subscribe is only called once per topic per game.
            
            // Subscribe to private dice updates
            stompClient?.subscribe(`/user/${userId}/queue/game/${gameIdToJoin}/dice`, (message) => {
                const diceData = JSON.parse(message.body);
                if (diceData.values && Array.isArray(diceData.values)) {
                    setDice(diceData.values);
                    setHasRolled(true); // Assuming receiving dice means a roll happened or it's an update
                }
            });

            // Subscribe to public challenge results
            stompClient?.subscribe(`/topic/game/${gameIdToJoin}/challenge`, (message) => {
                const challengeData = JSON.parse(message.body);
                setChallengeResult(challengeData);
                setShowChallengeResult(true);
                trackChallengeActivity(
                    challengeData.challengerId,
                    challengeData.challengeSuccessful,
                    challengeData.losingPlayerId
                );
                
                if (challengeResultTimerRef.current) clearTimeout(challengeResultTimerRef.current);
                challengeResultTimerRef.current = setTimeout(() => {
                    setShowChallengeResult(false); 
                    if (challengeData && !challengeData.gameFinished) {
                        startNextRound(); 
                    }
                    setChallengeResult(null); 
                    challengeResultTimerRef.current = null;
                }, 8000);
            });

            // Subscribe to public game state updates
            stompClient?.subscribe(`/topic/game/${gameIdToJoin}/state`, (message) => {
                const gameData = JSON.parse(message.body);
                if (gameData.status === 'ERROR') {
                    setError(gameData.errorMessage);
                    return;
                }
                
                setError(null); // Clear previous errors
                setGameState(prevGameState => ({...prevGameState, ...gameData})); // Merge new state

                if (gameData.currentBid === null) {
                    setBidInput({ quantity: 1, value: 1 });
                }
                
                // If round changes, it implies dice might need refresh if not sent privately
                if (gameData.round !== gameState.round && gameData.round > 0 && user?.id && gameData.status !== 'FINISHED') {
                     setTimeout(() => getDiceState(gameData.id, user.id), 500);
                }
                 if (gameData.status === 'PLAYING') { // When game becomes PLAYING (e.g. after ROLLING)
                    setHasRolled(false); // Reset for the next round's rolling phase
                }
            });

        } catch (joinError) {
            console.error('Join error:', joinError.response?.data || joinError);
            setError(joinError.response?.data?.message || 'Failed to join game');
        }
    };

    const getDiceState = async (gameIdForDice, pId) => {
        try {
            const response = await axios.get(`http://localhost:8080/api/games/${gameIdForDice}/players/${pId}/dice`, axiosConfig);
            if (response.data && response.data.values) {
                setDice(response.data.values);
            }
        } catch (fetchDiceError) {
            console.error('Error getting dice state:', fetchDiceError);
        }
    };

    const startGame = (gameIdToStart) => {
        if (stompClient) {
            stompClient.send("/app/game/start", {}, JSON.stringify({ gameId: gameIdToStart }));
        }
    };

    const placeBid = (gameIdForBid, currentBidInput) => {
        if (stompClient && user?.id) {
            stompClient.send(`/app/game/${gameIdForBid}/bid`, {}, 
                JSON.stringify({
                    gameId: gameIdForBid,
                    playerId: user.id,
                    quantity: currentBidInput.quantity,
                    value: currentBidInput.value
                })
            );
        }
    };

    const challenge = (gameIdForChallenge) => {
        if (stompClient && user?.id) {
            stompClient.send(`/app/game/${gameIdForChallenge}/challenge`, {}, 
                JSON.stringify({
                    gameId: gameIdForChallenge,
                    playerId: user.id
                })
            );
        }
    };
    
    const rollDice = () => {
        if (!stompClient || !gameState?.id || !playerId) {
            setError('Cannot roll dice: connection or game state issue.');
            return;
        }
        stompClient.send("/app/game/roll", {}, JSON.stringify({
            gameId: gameState.id,
            playerId: String(playerId)
        }));
    };

    const startNextRound = async () => {
        if (!challengeResult || !gameState?.id) return;
        
        try {
            // No need to call POST /next-round here anymore if GameService handles it post-challenge
            // and broadcasts the new state. The challengeResult modal closing should trigger this.
            // However, if challengeResult.gameFinished is true, we should not start a new round.
            // The backend processChallenge now determines the next player and updates dice.
            // The frontend mainly needs to react to the new GameStateDTO after a challenge.
            // The call to startNextRound was to get new dice. If GameStateDTO doesn't have them
            // or private dice messages aren't sufficient, this might still be needed.
            // For now, let's assume the backend handles the round transition and dice rolling/distribution.
            // The timeout on the challenge modal closing is the main trigger for proceeding.

            console.log("Proceeding after challenge modal for game:", gameState.id);
            // If the game is NOT finished, the UI will simply reflect the new state.
            // If a new round is automatically started by backend, GameState will update.
            // If frontend action is needed to start a new round explicitly (beyond closing modal):
            // This might be where an API call to `startNextRoundAfterChallenge` if it purely exists for client trigger.
            // The current `startNextRoundAfterChallenge` in backend is called by a POST, which this function did.
            // Let's ensure this POST is still relevant IF the backend logic for challenge doesn't fully transition.
            // Given `processChallenge` in backend now sets up for next round and relies on `startNextRoundAfterChallenge`
            // being called via client, this remains.

            const response = await axios.post(
                `http://localhost:8080/api/games/${gameState.id}/next-round`,
                { losingPlayerId: challengeResult.losingPlayerId },
                axiosConfig
            );
            // GameState will be updated via WebSocket subscription from this POST's broadcast
        } catch (error) {
            console.error('Error in startNextRound (POST /next-round):', error);
        }
    };
    
    // Return JSX
    return (
        <div className="game-board">
            <h1>Perudo Game</h1>
            
            {error && <div className="error-message">{error}</div>}

            {showChallengeResult && challengeResult && (
                <div className="challenge-result-modal">
                    <div className="challenge-result-content">
                        <h2>Challenge Result</h2>
                        <div className="challenge-summary">
                            <p><strong>{challengeResult.challengerName}</strong> challenged <strong>{challengeResult.bidPlayerName}</strong>'s bid:</p>
                            <p className="bid-info">
                                <strong>{challengeResult.challengedBid.quantity} × {challengeResult.challengedBid.value}'s</strong>
                            </p>
                        </div>
                        
                        <div className="revealed-dice">
                            <h3>All Dice Revealed:</h3>
                            {Object.entries(challengeResult.allPlayerDice).map(([pId, diceVals]) => {
                                const playerDetails = allOriginalPlayers.find(p => String(p.id) === String(pId)) || gameState.players?.find(p => String(p.id) === String(pId));
                                return (
                                    <div key={pId} className="player-dice-reveal">
                                        <div className="player-name">{playerDetails?.username || `Player ${pId}`}:</div>
                                        <div className="dice-values">
                                            {diceVals.map((value, index) => {
                                                const isMatchingDie = (
                                                    value === challengeResult.challengedBid.value || 
                                                    (value === 1 && challengeResult.challengedBid.value !== 1)
                                                );
                                                return (
                                                    <span key={`${pId}-die-${index}`} className={`die ${isMatchingDie ? 'matching' : ''}`}>
                                                        {value}
                                                    </span>
                                                );
                                            })}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                        
                        <div className="challenge-outcome">
                            <p className="actual-count">
                                Actual count of {challengeResult.challengedBid.value}'s
                                {challengeResult.challengedBid.value !== 1 ? " (including 1's as wild)" : " (1's only, no wilds)"}: 
                                <strong>{challengeResult.actualCount}</strong>
                            </p>
                            <p className={`result ${challengeResult.challengeSuccessful ? 'success' : 'failure'}`}>
                                {challengeResult.challengeSuccessful ? 
                                    `✅ Challenge SUCCESSFUL! ${challengeResult.losingPlayerName} loses a die.` : 
                                    `❌ Challenge FAILED! ${challengeResult.losingPlayerName} loses a die.`
                                }
                            </p>
                            {challengeResult.gameFinished && (
                                <p className="game-winner">
                                    🏆 <strong>{challengeResult.winnerName}</strong> wins the game!
                                </p>
                            )}
                        </div>
                        
                        <button 
                            className="close-challenge-result"
                            onClick={() => {
                                clearTimeout(challengeResultTimerRef.current);
                                setShowChallengeResult(false);
                                if (challengeResult && !challengeResult.gameFinished) {
                                    startNextRound(); 
                                }
                                setChallengeResult(null);
                            }}
                        >
                            Close
                        </button>
                    </div>
                </div>
            )}

            {(!gameState.id || gameState.id === null) && !stompClient && (
                 <p>Connecting to server...</p>
            )}
            {(!gameState.id || gameState.id === null) && stompClient && (
                <button onClick={createGame}>Create New Game</button>
            )}

            {gameState.id && !playerId && user && (
                <div className="join-game">
                    <p>Game ID: {gameState.id}</p>
                    <button onClick={() => joinGame(gameState.id, user.id)}>
                        Join Game as {user.username}
                    </button>
                </div>
            )}

            {gameState.id && playerId && !isSpectating && ( // Don't show game controls if spectating
                <div className="game-info">
                    <h2>Game ID: {gameState.id} (Round: {gameState.round || 0})</h2>
                    <div className="players">
                        <h3>Players:</h3>
                        <ul>
                            {(gameState.players || []).map(player => (
                                <li key={player.id} className={String(player.id) === String(playerId) ? 'current-player-marker' : ''}>
                                    <div className="player-info">
                                        <span className="player-username">{player.username}</span>
                                        <span className="dice-count">({player.dice?.length || 0} dice)</span>
                                        {String(gameState.currentPlayerId) === String(player.id) && <span className="current-turn-indicator">🎲</span>}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>
                    
                    {gameState.status === 'WAITING' && (
                        <button 
                            onClick={() => startGame(gameState.id)}
                            disabled={!gameState.players || gameState.players.length < 2}
                        >
                            Start Game ({gameState.players ? gameState.players.length : 0}/2+ players)
                        </button>
                    )}

                    {gameState.status === 'ROLLING' && (
                        <div className="rolling-phase">
                            <h3>Rolling Phase</h3>
                            {!hasRolled ? (
                                <button onClick={rollDice} className="roll-button">Roll Your Dice</button>
                            ) : (
                                <p>Waiting for other players to roll...</p>
                            )}
                            <div className="players-status">
                                {(gameState.players || []).map(player => (
                                    <div key={player.id} className="player-status">
                                        {player.username}: {player.hasRolled ? '✓ Rolled' : 'Waiting...'}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {dice.length > 0 && gameState.status !== 'ROLLING' && (
                        <div className="your-dice">
                            <h3>{user?.username}'s Dice:</h3>
                            <div className="dice-container">
                                <div className="dice-list">
                                    {dice.map((value, index) => <span key={`player-die-${index}-${value}`} className="die">{value}</span>)}
                                </div>
                            </div>
                        </div>
                    )}
                    
                    {gameState.currentBid && (
                        <div className="current-bid">
                            <h3>Current Bid:</h3>
                            <p>{gameState.currentBid.quantity} x {gameState.currentBid.value}'s by Player {gameState.currentBid.playerId}</p>
                        </div>
                    )}
                    
                    {gameState.status === 'PLAYING' && String(gameState.currentPlayerId) === String(user?.id) && (
                        <div className="action-controls">
                            <div className="bid-form">
                                <h4>Your Turn - Place Your Bid</h4>
                                <div className="bid-inputs">
                                    <label>Quantity:
                                        <input type="number" min="1" value={bidInput.quantity}
                                            onChange={(e) => setBidInput({...bidInput, quantity: Math.max(1, parseInt(e.target.value) || 1)})}
                                        />
                                    </label>
                                    <label>Value:
                                        <input type="number" min="1" max="6" value={bidInput.value}
                                            onChange={(e) => setBidInput({...bidInput, value: Math.min(6, Math.max(1, parseInt(e.target.value) || 1))})}
                                        />
                                    </label>
                                </div>
                                <div className="action-buttons">
                                    <button className="bid-button" onClick={() => placeBid(gameState.id, bidInput)}>Place Bid</button>
                                    {gameState.currentBid && (
                                        <button className="challenge-button" onClick={() => challenge(gameState.id)}>Challenge Last Bid</button>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                     {gameState.status === 'PLAYING' && String(gameState.currentPlayerId) !== String(user?.id) && (
                        <p>Waiting for Player {gameState.currentPlayerId} ({gameState.turnSequence?.find(p=>String(p.id) === String(gameState.currentPlayerId))?.username}) to make a move...</p>
                    )}
                </div>
            )}
            
            {isSpectating && gameState.id && (
                 <div className="spectating-info">
                    <h2>Spectating Game ID: {gameState.id} (Round: {gameState.round || 0})</h2>
                     {/* Simplified spectator view or reuse parts of game-info */}
                     <p>Current turn: Player {gameState.currentPlayerId} ({gameState.turnSequence?.find(p=>String(p.id) === String(gameState.currentPlayerId))?.username})</p>
                     {gameState.currentBid && <p>Current Bid: {gameState.currentBid.quantity} x {gameState.currentBid.value}'s</p>}
                     <h3>Players:</h3>
                        <ul>
                            {(gameState.players || []).map(player => (
                                <li key={player.id}>
                                   {player.username} ({player.dice?.length || 0} dice)
                                   {String(gameState.currentPlayerId) === String(player.id) && " 🎲"}
                                </li>
                            ))}
                        </ul>
                     <button onClick={() => setIsSpectating(false)}>Stop Spectating & Return to Game End Screen</button>
                 </div>
            )}

            {showGameEndResults && gameState.id && (
                <GameEndResults
                    gameId={gameState.id}
                    gameState={gameState}
                    allOriginalPlayers={allOriginalPlayers} // This now contains the activity counts
                    onSpectate={handleSpectate}
                    onClose={handleGameEndResultsClose} 
                />
            )}
        </div>
    );
};

export default GameBoard;
