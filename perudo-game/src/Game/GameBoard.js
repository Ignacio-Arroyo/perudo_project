import React, { useState, useEffect } from 'react';
import axios from 'axios';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import './gameboard.css';

const GameBoard = () => {
    const [gameState, setGameState] = useState(null);
    const [playerId, setPlayerId] = useState(null);
    const [dice, setDice] = useState([]);
    const [bid, setBid] = useState({ quantity: 1, value: 2 });
    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        // Initialize WebSocket connection
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);
        
        client.connect({}, () => {
            setStompClient(client);
            
            // Subscribe to lobby updates
            client.subscribe('/topic/lobby', (message) => {
                const gameData = JSON.parse(message.body);
                setGameState(gameData);
            });
        });

        return () => {
            if (client) {
                client.disconnect();
            }
        };
    }, []);

    const createGame = () => {
        if (stompClient) {
            stompClient.send("/app/game/create", {}, {});
        }
    };

    const joinGame = async (gameId, playerId) => {
        try {
            const response = await axios.post(`http://localhost:8080/api/games/${gameId}/join/${playerId}`);
            setPlayerId(playerId);
            setGameState(response.data);
            
            // Subscribe to game updates
            stompClient.subscribe(`/topic/game/${gameId}/state`, (message) => {
                const gameData = JSON.parse(message.body);
                setGameState(gameData);
            });

            // Subscribe to personal dice updates
            stompClient.subscribe(`/user/topic/game/${gameId}/dice`, (message) => {
                const diceData = JSON.parse(message.body);
                setDice(diceData.values);
            });
        } catch (error) {
            console.error('Error joining game:', error);
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

    return (
        <div className="game-board">
            <h1>Perudo Game</h1>
            
            {!gameState && (
                <button onClick={createGame}>Create New Game</button>
            )}

            {gameState && !playerId && (
                <div className="join-game">
                    <input type="text" placeholder="Enter Player ID" id="playerId" />
                    <button onClick={() => joinGame(gameState.id, document.getElementById('playerId').value)}>
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
