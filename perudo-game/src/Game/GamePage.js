import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useStompClient, useSubscription } from 'react-stomp-hooks';

const GamePage = ({ gameId }) => {
    const [gameState, setGameState] = useState({});
    const [connectionStatus, setConnectionStatus] = useState('disconnected');
    const stompClient = useStompClient();

    // Handle WebSocket subscription
    useSubscription(`/topic/game/${gameId}/updates`, (message) => {
        setGameState(JSON.parse(message.body));
    });

    // Monitor connection status
    useEffect(() => {
        if (stompClient) {
            setConnectionStatus('connected');

            // Connection error handling
            stompClient.onConnect = () => setConnectionStatus('connected');
            stompClient.onDisconnect = () => setConnectionStatus('disconnected');
            stompClient.onError = (error) => {
                console.error('WebSocket error:', error);
                setConnectionStatus('error');
            };
        }

        return () => {
            // Cleanup on unmount
            if (stompClient) {
                stompClient.disconnect();
            }
        };
    }, [stompClient]);

    useEffect(() => {
        // Fetch initial game state
        const fetchGameState = async () => {
            try {
                const response = await axios.get(`/api/games/${gameId}`);
                setGameState(response.data);
            } catch (error) {
                console.error('Error fetching game state:', error);
            }
        };

        fetchGameState();
    }, [gameId]);

    const handleMove = (move) => {
        if (stompClient) {
            stompClient.send('/app/game/move', {}, JSON.stringify(move));
        }
    };

    return (
        <div>
            <h1>Game Page</h1>
            <div>
                <h2>Game ID: {gameId}</h2>
                <p>Current Player: {gameState.currentPlayer}</p>
                <p>Current Bid: {gameState.currentBid}</p>
                <p>Connection Status: {connectionStatus}</p>
                {/* Add more game state details as needed */}
            </div>
            <div>
                <button onClick={() => handleMove({ type: 'bid', value: 5 })}>Make a Bid</button>
                <button onClick={() => handleMove({ type: 'challenge' })}>Challenge</button>
                {/* Add more move options as needed */}
            </div>
        </div>
    );
};

export default GamePage;
