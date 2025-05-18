import React, { useState, useEffect } from 'react';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import './gameboard.css';

const axiosConfig = {
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};

const GameBoard = () => {
    const [gameState, setGameState] = useState(null);
    const [playerId, setPlayerId] = useState(null);
    const [dice, setDice] = useState([]);
    const [bid, setBid] = useState({ quantity: 1, value: 2 });
    const [stompClient, setStompClient] = useState(null);
    const [error, setError] = useState(null);
    // Add new state for tracking dice rolling status
    const [hasRolled, setHasRolled] = useState(false);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);
        
        client.connect({}, () => {
            setStompClient(client);
            
            // Subscribe to lobby updates with enhanced debug logging
            client.subscribe('/topic/lobby', (message) => {
                console.log('Raw message received:', message);
                try {
                    const gameData = JSON.parse(message.body);
                    console.log('Parsed game data:', gameData);
                    
                    if (!gameData) {
                        console.error('No game data received');
                        return;
                    }
                    
                    if (!gameData.id || gameData.id === 'null' || gameData.id === null) {
                        console.error('Invalid game ID received:', gameData);
                        return;
                    }

                    console.log('Setting game state with valid ID:', gameData.id);
                    setGameState(gameData);
                } catch (error) {
                    console.error('Error processing game data:', error);
                }
            });
        }, (error) => {
            console.error('STOMP connection error:', error);
        });

        return () => {
            if (client) {
                client.disconnect();
            }
        };
    }, []);

    const createGame = () => {
        if (stompClient) {
            console.log('Sending game creation request');
            try {
                stompClient.send("/app/game/create", {}, JSON.stringify({}));
                console.log('Game creation request sent successfully');
            } catch (error) {
                console.error('Error sending game creation request:', error);
            }
        } else {
            console.error('WebSocket connection not established');
        }
    };

    // Update joinGame to use correct endpoint
    const joinGame = async (gameId, playerId) => {
        if (!gameId || !playerId) {
            console.error('Missing data for join:', { gameId, playerId });
            return;
        }
        
        try {
            console.log('Joining game:', { gameId, playerId });
            const response = await axios.post(
                `http://localhost:8080/api/games/${gameId}/join/${playerId}`,
                {},
                axiosConfig
            );
            console.log('Join response:', response.data);
            
            // Ensure playerId is stored as a string
            const playerIdString = String(playerId);
            setPlayerId(playerIdString);
            setGameState(response.data);
            
            // Log the stored player ID
            console.log('Stored player ID:', playerIdString);
            
            // Subscribe to game updates
            stompClient?.subscribe(`/user/${playerId}/queue/game/${gameId}/dice`, (message) => {
                const diceData = JSON.parse(message.body);
                console.log('Received dice roll:', diceData);
                if (diceData.values) {
                    setDice(diceData.values);
                    setHasRolled(true);
                }
            });

            stompClient?.subscribe(`/topic/game/${gameId}/state`, (message) => {
                const gameData = JSON.parse(message.body);
                console.log('Game status:', gameData.status); // Debug log
                
                if (gameData.status === 'ERROR') {
                    setError(gameData.errorMessage);
                    return;
                }
                
                if (gameData.status === 'PLAYING') {
                    // Reset roll status when game actually starts
                    setHasRolled(false);
                }
                
                setError(null);
                setGameState(gameData);
            });
        } catch (error) {
            console.error('Join error:', error.response?.data || error);
            setError(error.response?.data?.message || 'Failed to join game');
        }
    };

    const getDiceState = async (gameId, playerId) => {
        try {
            const response = await axios.get(`http://localhost:8080/api/games/${gameId}/players/${playerId}/dice`, axiosConfig);
            setDice(response.data.values);
        } catch (error) {
            console.error('Error getting dice state:', error);
        }
    };

    const startGame = (gameId) => {
        if (stompClient) {
            stompClient.send("/app/game/start", {}, JSON.stringify({ gameId }));
        }
    };

    const placeBid = (gameId, bid) => {
        if (stompClient) {
            stompClient.send(`/app/game/${gameId}/bid`, {}, 
                JSON.stringify({
                    gameId,
                    playerId,
                    quantity: bid.quantity,
                    value: bid.value
                })
            );
        }
    };

    const challenge = (gameId) => {
        if (stompClient) {
            stompClient.send(`/app/game/${gameId}/challenge`, {}, 
                JSON.stringify({
                    gameId,
                    playerId
                })
            );
        }
    };

    const updateGameState = async (gameId, playerId) => {
        try {
            const response = await axios.get(`http://localhost:8080/api/games/${gameId}/players/${playerId}`, axiosConfig);
            setGameState(response.data);
        } catch (error) {
            console.error('Error updating game state:', error);
        }
    };

    // Add rollDice function
    const rollDice = () => {
        if (!stompClient || !gameState || !playerId) {
            setError('Cannot roll dice: connection not established');
            return;
        }

        console.log('Rolling dice for player:', playerId, 'in game:', gameState.id);
        try {
            const rollRequest = {
                gameId: gameState.id,
                playerId: String(playerId) // Ensure playerId is a string
            };
            console.log('Sending roll request:', rollRequest);
            stompClient.send("/app/game/roll", {}, JSON.stringify(rollRequest));
        } catch (error) {
            console.error('Error rolling dice:', error);
            setError('Failed to roll dice: ' + error.message);
        }
    };

    return (
        <div className="game-board">
            <h1>Perudo Game</h1>
            
            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}
            
            {!gameState && (
                <button onClick={createGame}>Create New Game</button>
            )}

            {gameState && !playerId && (
                <div className="join-game">
                    <div className="game-id-display">
                        <p>Game ID: {gameState.id}</p> {/* Changed from gameState.gameId */}
                    </div>
                    <input 
                        type="text" 
                        placeholder="Enter Player ID" 
                        id="playerId"
                    />
                    <button 
                        onClick={() => {
                            const playerIdValue = document.getElementById('playerId').value;
                            if (!gameState?.id) {
                                console.error('No game ID available');
                                return;
                            }
                            if (!playerIdValue) {
                                console.error('No player ID entered');
                                return;
                            }
                            console.log('Join attempt:', {
                                gameId: gameState.id,
                                playerId: playerIdValue
                            });
                            joinGame(gameState.id, playerIdValue);
                        }}
                    >
                        Join Game
                    </button>
                </div>
            )}

            {gameState && playerId && (
                <div className="game-info">
                    <h2>Game ID: {gameState.id}</h2>
                    <div className="players">
                        <h3>Players:</h3>
                        <ul>
                            {gameState.players.map(player => (
                                <li key={player.id} className={player.id === playerId ? 'current-player' : ''}>
                                    {player.username} ({player.dice.length} dice)
                                </li>
                            ))}
                        </ul>
                    </div>
                    
                    {gameState.status === 'WAITING' && (
                        <button 
                            onClick={() => startGame(gameState.id)}
                            disabled={gameState.players.length < 2}
                        >
                            Start Game ({gameState.players.length}/2 players)
                        </button>
                    )}

                    {gameState.status === 'ROLLING' && (
                        <div className="rolling-phase">
                            <h3>Rolling Phase</h3>
                            {!hasRolled ? (
                                <button 
                                    onClick={rollDice}
                                    className="roll-button"
                                >
                                    Roll Your Dice
                                </button>
                            ) : (
                                <p>Waiting for other players to roll...</p>
                            )}
                            <div className="players-status">
                                {gameState.players.map(player => (
                                    <div key={player.id} className="player-status">
                                        {player.username}: {player.hasRolled ? '✓ Rolled' : 'Waiting to roll...'}
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {dice.length > 0 && (
                        <div className="your-dice">
                            <h3>Your Dice:</h3>
                            <div className="dice-list">
                                {dice.map((value, index) => (
                                    <span key={index} className="die">{value}</span>
                                ))}
                            </div>
                        </div>
                    )}
                    
                    {gameState.currentBid && (
                        <div className="current-bid">
                            <h3>Current Bid:</h3>
                            <p>{gameState.currentBid.quantity} x {gameState.currentBid.value}'s</p>
                        </div>
                    )}

                    {gameState.currentPlayer?.id === playerId && (
                        <div className="bid-controls">
                            <input 
                                type="number" 
                                value={bid.quantity}
                                onChange={(e) => setBid({...bid, quantity: parseInt(e.target.value)})}
                                min="1"
                            />
                            <input 
                                type="number" 
                                value={bid.value}
                                onChange={(e) => setBid({...bid, value: parseInt(e.target.value)})}
                                min="1"
                                max="6"
                            />
                            <button onClick={() => placeBid(gameState.id, bid)}>Place Bid</button>
                            <button onClick={() => challenge(gameState.id)}>Challenge!</button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default GameBoard;
