import React, { useEffect, useState } from 'react';
import { useStompClient } from 'react-stomp-hooks';

const GameBoard = () => {
    const [gameState, setGameState] = useState({});
    const stompClient = useStompClient();

    useEffect(() => {
        if (stompClient) {
            stompClient.subscribe('/topic/game/updates', (message) => {
                setGameState(JSON.parse(message.body));
            });
        }
    }, [stompClient]);

    const handleMove = (move) => {
        stompClient.send('/app/game/move', {}, JSON.stringify(move));
    };

    return (
        <div>
            {/* Render the game board and controls */}
        </div>
    );
};

export default GameBoard;
