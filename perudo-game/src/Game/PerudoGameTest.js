import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useStompClient } from 'react-stomp-hooks';

const PerudoGameTest = () => {
    const { gameId } = useParams();
    const stompClient = useStompClient();
    const [gameState, setGameState] = useState({
        players: [],
        currentBid: null,
        gameStatus: 'WAITING'
    });
    const [testDice, setTestDice] = useState([]);

    // Test function to roll dice
    const rollDice = () => {
        const newDice = Array(5).fill(0).map(() => Math.floor(Math.random() * 6) + 1);
        setTestDice(newDice);
        
        if (stompClient) {
            stompClient.publish({
                destination: `/app/game/${gameId}/roll`,
                body: JSON.stringify({ dice: newDice })
            });
        }
    };

    // Subscribe to game updates
    useEffect(() => {
        if (!stompClient) return;

        const subscription = stompClient.subscribe(
            `/topic/game/${gameId}`,
            (message) => {
                console.log('Received game update:', message.body);
                try {
                    const data = JSON.parse(message.body);
                    setGameState(data);
                } catch (error) {
                    console.error('Error parsing game update:', error);
                }
            }
        );

        // Join game message
        stompClient.publish({
            destination: `/app/game/${gameId}/join`,
            body: JSON.stringify({ gameId })
        });

        return () => subscription.unsubscribe();
    }, [stompClient, gameId]);

    return (
        <div style={{ padding: '20px' }}>
            <h2>Perudo Game Test Page</h2>
            <div>Game ID: {gameId}</div>
            <div>Connection Status: {stompClient?.connected ? 'Connected' : 'Disconnected'}</div>
            
            <div style={{ marginTop: '20px' }}>
                <h3>Your Dice:</h3>
                <div style={{ display: 'flex', gap: '10px' }}>
                    {testDice.map((value, index) => (
                        <div
                            key={index}
                            style={{
                                width: '40px',
                                height: '40px',
                                border: '2px solid black',
                                borderRadius: '8px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '20px'
                            }}
                        >
                            {value}
                        </div>
                    ))}
                </div>
                <button 
                    onClick={rollDice}
                    style={{ marginTop: '10px' }}
                >
                    Roll Dice
                </button>
            </div>

            <div style={{ marginTop: '20px' }}>
                <h3>Game State:</h3>
                <pre>{JSON.stringify(gameState, null, 2)}</pre>
            </div>
        </div>
    );
};

export default PerudoGameTest;