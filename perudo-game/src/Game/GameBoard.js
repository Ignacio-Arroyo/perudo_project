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
            console.error('Missing data for join:', { gameId, playerId, gameState });
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
            setPlayerId(playerId);
            setGameState(response.data);
            
            // Subscribe to game updates
            stompClient?.subscribe(`/topic/game/${gameId}/state`, (message) => {
                const gameData = JSON.parse(message.body);
                setGameState(gameData);
            });
        } catch (error) {
            console.error('Join error:', error.response?.data || error);
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

    return (
        <div className="game-board">
            <h1>Perudo Game</h1>
            
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
                        <button onClick={() => startGame(gameState.id)}>Start Game</button>
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
