import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useStompClient } from 'react-stomp-hooks';

const PerudoGame = () => {
    const { gameId } = useParams();
    const stompClient = useStompClient();
    const [gameState, setGameState] = useState({
        players: [],
        currentPlayer: null,
        currentBid: null,
        myDice: [],
        round: 0,
        status: 'WAITING'
    });

    useEffect(() => {
        if (!stompClient) return;

        const subscriptions = [
            // Game state updates
            stompClient.subscribe(`/topic/game/${gameId}/state`, (message) => {
                const newState = JSON.parse(message.body);
                setGameState(prev => ({ ...prev, ...newState }));
            }),

            // Dice rolls
            stompClient.subscribe(`/user/topic/game/${gameId}/dice`, (message) => {
                const { dice } = JSON.parse(message.body);
                setGameState(prev => ({ ...prev, myDice: dice }));
            }),

            // Turn notifications
            stompClient.subscribe(`/topic/game/${gameId}/turn`, (message) => {
                const { currentPlayer } = JSON.parse(message.body);
                setGameState(prev => ({ ...prev, currentPlayer }));
            })
        ];

        return () => subscriptions.forEach(sub => sub.unsubscribe());
    }, [stompClient, gameId]);

    const handleMakeBid = (quantity, value) => {
        stompClient.publish({
            destination: `/app/game/${gameId}/bid`,
            body: JSON.stringify({ quantity, value })
        });
    };

    const handleChallenge = () => {
        stompClient.publish({
            destination: `/app/game/${gameId}/challenge`
        });
    };

    return (
        <div className="perudo-game">
            <div className="game-info">
                <h2>Round {gameState.round}</h2>
                <div className="current-player">
                    Current Turn: {gameState.currentPlayer?.name}
                </div>
            </div>

            <div className="dice-area">
                <h3>Your Dice</h3>
                <div className="dice-container">
                    {gameState.myDice.map((value, index) => (
                        <div key={index} className="die">
                            {value}
                        </div>
                    ))}
                </div>
            </div>

            <div className="game-actions">
                {gameState.currentBid && (
                    <div className="current-bid">
                        Current Bid: {gameState.currentBid.quantity} x {gameState.currentBid.value}s
                    </div>
                )}
                
                {/* Add bid controls and challenge button here */}
            </div>
        </div>
    );
};

export default PerudoGame;