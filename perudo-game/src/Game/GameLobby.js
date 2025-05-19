import React, { useEffect, useState } from 'react';
import { useStompClient } from 'react-stomp-hooks';
import { useNavigate } from 'react-router-dom';

const GameLobby = () => {
    const [players, setPlayers] = useState([]);
    const [gameId, setGameId] = useState(null);
    const [isHost, setIsHost] = useState(false);
    const stompClient = useStompClient();
    const navigate = useNavigate();

    useEffect(() => {
        if (!stompClient) return;

        // Subscribe to lobby updates
        const subscription = stompClient.subscribe('/topic/lobby', (message) => {
            const data = JSON.parse(message.body);
            setPlayers(data.players);
            setGameId(data.gameId);

            // If game is starting, navigate to game page
            if (data.status === 'STARTING') {
                navigate(`/perudogame/${data.gameId}`);
            }
        });

        return () => subscription.unsubscribe();
    }, [stompClient, navigate]);

    const handleCreateGame = () => {
        stompClient.publish({
            destination: '/app/game/create',
            body: JSON.stringify({ action: 'CREATE_GAME' })
        });
        setIsHost(true);
    };

    const handleJoinGame = (gameId) => {
        stompClient.publish({
            destination: '/app/game/join',
            body: JSON.stringify({ gameId })
        });
    };

    const handleStartGame = () => {
        if (!isHost || players.length < 2) return;
        
        stompClient.publish({
            destination: '/app/game/start',
            body: JSON.stringify({ gameId })
        });
    };

    return (
        <div className="game-lobby">
            <h2>Game Lobby</h2>
            
            {!gameId && (
                <button onClick={handleCreateGame}>Create New Game</button>
            )}

            <div className="players-list">
                <h3>Players ({players.length}/6)</h3>
                {players.map(player => (
                    <div key={player.id} className="player-item">
                        {player.name}
                    </div>
                ))}
            </div>

            {isHost && players.length >= 2 && (
                <button onClick={handleStartGame}>
                    Start Game
                </button>
            )}
        </div>
    );
};

export default GameLobby;