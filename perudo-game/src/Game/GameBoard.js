import React, { useState, useEffect, useContext, useRef } from 'react';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from '../Auth/authcontext';
import GameEndResultsModal from './GameEndResultsModal';
import './gameboard.css';

const axiosConfig = {
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};

// Mapping des couleurs de dés selon l'ID du produit équipé
const diceColorMapping = {
    1: { // Dé Bois
        background: 'linear-gradient(145deg, #d4a574, #b8935e)',
        border: '#8b6f47',
        color: '#2c1810',
        shadow: 'rgba(139, 111, 71, 0.3)'
    },
    2: { // Dé Rouge
        background: 'linear-gradient(145deg, #ff4757, #ff3838)',
        border: '#c0392b',
        color: '#ffffff',
        shadow: 'rgba(192, 57, 43, 0.3)'
    },
    3: { // Dé Orange
        background: 'linear-gradient(145deg, #ff9500, #ff7675)',
        border: '#e17055',
        color: '#ffffff',
        shadow: 'rgba(225, 112, 85, 0.3)'
    },
    4: { // Dé Multicolore
        background: 'linear-gradient(145deg, #ff6b6b, #4ecdc4, #45b7d1, #f39c12)',
        border: '#2d3436',
        color: '#ffffff',
        shadow: 'rgba(45, 52, 54, 0.3)'
    },
    5: { // Dé Gris/Noir
        background: 'linear-gradient(145deg, #636e72, #2d3436)',
        border: '#000000',
        color: '#ffffff',
        shadow: 'rgba(0, 0, 0, 0.3)'
    },
    6: { // Dé Vert
        background: 'linear-gradient(145deg, #00b894, #00a085)',
        border: '#006f5e',
        color: '#ffffff',
        shadow: 'rgba(0, 111, 94, 0.3)'
    },
    7: { // Dé Bleu Clair
        background: 'linear-gradient(145deg, #74b9ff, #0984e3)',
        border: '#2d3436',
        color: '#ffffff',
        shadow: 'rgba(45, 52, 54, 0.3)'
    },
    8: { // Dé Bleu
        background: 'linear-gradient(145deg, #0984e3, #6c5ce7)',
        border: '#2d3436',
        color: '#ffffff',
        shadow: 'rgba(45, 52, 54, 0.3)'
    },
    9: { // Dé Noir
        background: 'linear-gradient(145deg, #2d3436, #000000)',
        border: '#636e72',
        color: '#ffffff',
        shadow: 'rgba(99, 110, 114, 0.3)'
    },
    default: { // Dé par défaut (blanc)
        background: 'linear-gradient(145deg, #ffffff, #f1f2f6)',
        border: '#007bff',
        color: '#007bff',
        shadow: 'rgba(0, 123, 255, 0.2)'
    }
};

// Define DiceFace component here if not imported from elsewhere
const DiceFace = ({ value, isHighlighted }) => {
    const diceSymbols = [
        '', // 0 index
        <span role="img" aria-label="dice value 1">⚀</span>, // 1
        <span role="img" aria-label="dice value 2">⚁</span>, // 2
        <span role="img" aria-label="dice value 3">⚂</span>, // 3
        <span role="img" aria-label="dice value 4">⚃</span>, // 4
        <span role="img" aria-label="dice value 5">⚄</span>, // 5
        <span role="img" aria-label="dice value 6">⚅</span>  // 6
    ];
    const highlightClass = isHighlighted ? 'highlighted-die' : '';
    return <span className={`dice-face ${highlightClass}`}>{diceSymbols[value] || value}</span>;
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
    const [equippedDiceId, setEquippedDiceId] = useState(null);

    // Game end and scoring state
    const [gameEndData, setGameEndData] = useState(null);
    const [showGameEndModal, setShowGameEndModal] = useState(false);

    // Récupérer l'ID du dé équipé depuis le localStorage
    useEffect(() => {
        const getUserEquippedDice = () => {
            try {
                const userStr = localStorage.getItem('user');
                if (userStr) {
                    const userData = JSON.parse(userStr);
                    const equippedProduct = userData.equippedProduct || userData.equippedDice;
                    console.log('Equipped dice ID found:', equippedProduct);
                    setEquippedDiceId(equippedProduct);
                }
            } catch (error) {
                console.error('Error getting equipped dice from localStorage:', error);
            }
        };

        getUserEquippedDice();
    }, []);

    // Fonction pour obtenir les styles du dé selon l'ID équipé
    const getDiceStyles = (equippedId) => {
        return diceColorMapping[equippedId] || diceColorMapping.default;
    };

    // All useEffect hooks grouped here
    useEffect(() => { // Turn sequence initialization
        if (gameState.status === 'PLAYING' && (!gameState.turnSequence || !gameState.turnSequence.length) && gameState.players?.length > 0) {
            console.log('Initializing turn sequence from players:', gameState.players);
            setGameState(prev => ({ ...prev, turnSequence: [...prev.players] }));
        }
    }, [gameState.status, gameState.players, gameState.turnSequence]);

    useEffect(() => { // STOMP connection and initial lobby subscription
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);
        
        client.connect({}, () => {
            setStompClient(client);
            console.log("STOMP client connected.");
            
            // Subscribe to the general lobby topic for game creation/listing
            client.subscribe('/topic/lobby', (message) => {
                console.log('Raw lobby message received:', message.body);
                try {
                    const receivedData = JSON.parse(message.body);
                    // Check if it's a single game object (not an array, has an id)
                    if (receivedData && !Array.isArray(receivedData) && receivedData.id && receivedData.id !== 'null') {
                        const gameData = receivedData;
                        console.log('Lobby message is a single game object, ID:', gameData.id, 'Updating game state.');
                        
                        setGameState(prev => ({
                            // Preserve essential non-game-specific parts if any from prev, though gameState is mostly game-specific
                            // For a new game from lobby, we primarily use gameData and reset others.
                            id: gameData.id,
                            status: gameData.status,
                            players: gameData.players || [],
                            turnSequence: gameData.turnSequence || [],
                            currentPlayer: gameData.currentPlayer || null,
                            currentPlayerId: gameData.currentPlayerId || null,
                            currentBid: gameData.currentBid || null,
                            round: gameData.round || 0,

                            // Reset local frontend states related to a specific game instance
                            dice: [],
                            error: null,
                            hasRolled: false,
                            challengeResult: null,
                            showChallengeResult: false,
                            gameEndData: null, // Clear previous game end data
                            showGameEndModal: false, // Hide previous game end modal
                        }));
                        // PlayerId must be cleared because this is a new game context from the lobby.
                        // The user has not yet "joined" this specific new game instance.
                        setPlayerId(null); 

                    } else if (Array.isArray(receivedData)) {
                        console.log('Lobby message is a list of games. Currently not handling display of game lists.');
                        // Placeholder for future functionality: setAvailableGames(receivedData);
                    } else {
                        console.log('Lobby message is not a recognized game object or list:', receivedData);
                    }
                } catch (error) {
                    console.error('Error processing game data from lobby:', error);
                }
            });
        }, (stompError) => {
            console.error('STOMP connection error:', stompError);
            setError('Failed to connect to game server. Please refresh.');
        });

        return () => {
            if (client && client.connected) {
                console.log("Disconnecting STOMP client.");
                client.disconnect();
            }
            setStompClient(null);
        };
    }, []); // Runs once to establish STOMP connection and lobby subscription

    useEffect(() => { // Game-specific STOMP subscriptions
        let stateSubscription;
        let diceSubscription;
        let challengeSubscription;
        let resultsSubscription;

        if (stompClient && stompClient.connected && gameState.id && user && user.id) {
            console.log(`Setting up subscriptions for game ${gameState.id} and user ${user.id}`);

            stateSubscription = stompClient.subscribe(`/topic/game/${gameState.id}/state`, (message) => {
                console.log('Game state update received:', message.body);
                const updatedGameData = JSON.parse(message.body);
                
                if (updatedGameData.status === 'ERROR') {
                    setError(updatedGameData.errorMessage || 'An unknown error occurred.');
                    // Do not wipe the game state if it's just an action error.
                    // The game should continue with the previous valid state.
                    // We might want to update specific fields if necessary, e.g., just the status.
                    setGameState(prev => ({ ...prev, status: updatedGameData.status })); 
                    return; // Stop further processing for this error message
                }

                setError(null); // Clear previous errors if this is a valid state update
                const previousRound = gameState.round; 
                setGameState(prev => ({ ...prev, ...updatedGameData }));

                if (updatedGameData.currentBid === null && previousRound < updatedGameData.round) {
                    console.log("New round detected, resetting bid input.");
                    setBidInput({ quantity: 1, value: 1 });
                }
                if (updatedGameData.round > previousRound && user.id) { // Ensure user.id is available
                    console.log("Round changed, refreshing dice state for player", user.id);
                    getDiceState(updatedGameData.id, user.id);
                }
            });

            diceSubscription = stompClient.subscribe(`/user/${user.id}/queue/game/${gameState.id}/dice`, (message) => {
                console.log('Dice update received:', message.body);
                const diceData = JSON.parse(message.body);
                setDice(diceData.values || []); 
            });

            challengeSubscription = stompClient.subscribe(`/topic/game/${gameState.id}/challenge`, (message) => {
                console.log('Challenge result received:', message.body);
                const resultData = JSON.parse(message.body);
                setChallengeResult(resultData);
                setShowChallengeResult(true);
                 // Auto-close challenge modal after some time
                if (challengeResultTimerRef.current) {
                    clearTimeout(challengeResultTimerRef.current);
                }
                challengeResultTimerRef.current = setTimeout(() => {
                    handleChallengeResultClose(); // Use the unified close handler
                }, 8000); // 8 seconds
            });

            resultsSubscription = stompClient.subscribe(`/topic/game/${gameState.id}/results`, (message) => {
                console.log('Game end results received:', message.body);
                const endData = JSON.parse(message.body);
                setGameEndData(endData);
                setShowGameEndModal(true);
            });

            // Cleanup function for this effect
            return () => {
                console.log(`Attempting to clean up subscriptions for game ${gameState.id || 'unknown'}`);
                if (stateSubscription) stateSubscription.unsubscribe();
                if (diceSubscription) diceSubscription.unsubscribe();
                if (challengeSubscription) challengeSubscription.unsubscribe();
                if (resultsSubscription) resultsSubscription.unsubscribe();
            };
        }
    }, [stompClient, gameState.id, user]); // user dependency is important here for user.id

    useEffect(() => { // Check for game end based on gameEndData received via WebSocket
        if (gameEndData && !showGameEndModal) {
            console.log('Game finished, received end results, showing modal.');
            setShowGameEndModal(true);
        }
    }, [gameEndData, showGameEndModal]);

    // All helper functions grouped here
    const handleGameEndModalClose = () => {
        setShowGameEndModal(false);
        setGameEndData(null);
        console.log("Game end modal closed by user. Should navigate home/lobby.");
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
    
    const isMatchingDie = (dieValue, bidValue) => {
        if (bidValue === 1) {
            return dieValue === 1;
        }
        return dieValue === bidValue || dieValue === 1;
    };

    const handleChallengeResultClose = () => {
        if (challengeResultTimerRef.current) {
            clearTimeout(challengeResultTimerRef.current);
            challengeResultTimerRef.current = null;
        }
        setShowChallengeResult(false);
        // Check if the game is finished from the challengeResult
        // If not finished, then proceed to start the next round.
        if (challengeResult && !challengeResult.gameFinished) {
            startNextRound(); 
        }
        setChallengeResult(null); // Clear the result after handling
    };

    // Ensure playerId state is set correctly upon joining
    useEffect(() => {
        if (user && user.id) {
            setPlayerId(String(user.id)); // Keep playerId as string if it's used for comparison with string IDs from backend
        }
    }, [user]);

    // Return JSX
    return (
        <div className="game-board">
            <h1>Perudo Game</h1>
            
            {error && <div className="error-message">{error}</div>}

            {showChallengeResult && challengeResult && (
                <div className="modal-overlay">
                    <div className="modal-content challenge-result-modal">
                        <h3>Challenge Result</h3>
                        <p><strong>Challenger:</strong> {challengeResult.challengerName}</p>
                        <p><strong>Player Challenged:</strong> {challengeResult.bidPlayerName}</p>
                        <p><strong>Their Bid:</strong> {challengeResult.bid ? `${challengeResult.bid.quantity} x ` : 'Bid Info N/A'} {challengeResult.bid && <DiceFace value={challengeResult.bid.value} />}</p>
                        <hr />
                        <p><strong>All Dice Revealed:</strong></p>
                        <div className="all-dice-reveal">
                            {Object.entries(challengeResult.allPlayerDiceMap || {}).map(([pId, pDice]) => {
                                const playerDetails = gameState.players.find(pl => String(pl.id) === pId);
                                return (
                                    <div key={pId} className="player-dice-reveal">
                                        <strong>{playerDetails ? playerDetails.username : `Player ${pId}`}: </strong>
                                        {pDice.map((dieValue, index) => {
                                            const isMatch = challengeResult.bid ? isMatchingDie(dieValue, challengeResult.bid.value) : false;
                                            return (
                                                <DiceFace key={index} value={dieValue} isHighlighted={isMatch} />
                                            );
                                        })}
                                    </div>
                                );
                            })}
                        </div>
                        <p><strong>Actual Count of {challengeResult.bid ? <DiceFace value={challengeResult.bid.value} /> : 'dice'}:</strong> {challengeResult.actualCount}</p>
                        <hr />
                        {challengeResult.challengeSuccessful ? (
                            <p className="success-message"><strong>Challenge SUCCEEDED!</strong> {challengeResult.losingPlayerName} loses a die.</p>
                        ) : (
                            <p className="failure-message"><strong>Challenge FAILED!</strong> {challengeResult.losingPlayerName} loses a die.</p>
                        )}
                        {challengeResult.gameFinished && challengeResult.winnerName && (
                            <p className="winner-message"><strong>GAME OVER! Winner is {challengeResult.winnerName}!</strong></p>
                        )}
                        <button onClick={handleChallengeResultClose}>OK</button>
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

            {gameState.id && playerId && (
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
                                    {dice.map((value, index) => {
                                        const diceStyles = getDiceStyles(equippedDiceId);
                                        return (
                                            <span 
                                                key={`player-die-${index}-${value}`} 
                                                className="die custom-dice"
                                                style={{
                                                    background: diceStyles.background,
                                                    border: `2px solid ${diceStyles.border}`,
                                                    color: diceStyles.color,
                                                    boxShadow: `0 4px 8px ${diceStyles.shadow}`,
                                                    fontWeight: 'bold',
                                                    textShadow: diceStyles.color === '#ffffff' ? '1px 1px 2px rgba(0,0,0,0.3)' : 'none'
                                                }}
                                            >
                                                {value}
                                            </span>
                                        );
                                    })}
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

            {showGameEndModal && gameEndData && (
                <GameEndResultsModal 
                    results={gameEndData} 
                    onClose={handleGameEndModalClose} 
                    currentUser={user}
                />
            )}
        </div>
    );
};

export default GameBoard;
