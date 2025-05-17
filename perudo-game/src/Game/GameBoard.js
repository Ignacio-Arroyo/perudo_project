import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';

const GameBoard = () => {
    const [gameState, setGameState] = useState({});
    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        const client = new Client({
            brokerURL: 'ws://localhost:8080/ws',
            onConnect: () => {
                client.subscribe('/topic/game/updates', (message) => {
                    setGameState(JSON.parse(message.body));
                });
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.activate();
        setStompClient(client);

        return () => {
            client.deactivate();
        };
    }, []);

    const handleMove = (move) => {
        if (stompClient) {
            stompClient.publish({
                destination: '/app/game/move',
                body: JSON.stringify(move),
            });
        }
    };

    return (
        <div>
            <h1>Game Board</h1>
            <div>
                <button onClick={() => handleMove({ type: 'bid', value: 5 })}>Make a Bid</button>
                <button onClick={() => handleMove({ type: 'challenge' })}>Challenge</button>
            </div>
        </div>
    );
};

export default GameBoard;
