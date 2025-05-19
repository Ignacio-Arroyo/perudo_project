import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const GameSearchPage = () => {
    const [searchCriteria, setSearchCriteria] = useState('');
    const [availableGames, setAvailableGames] = useState([]);
    const navigate = useNavigate();

    const handleSearch = async () => {
        try {
            const response = await axios.get('/api/games/search', {
                params: { criteria: searchCriteria }
            });
            setAvailableGames(response.data);
        } catch (error) {
            console.error('Error searching for games:', error);
        }
    };

    const handleJoinGame = (gameId) => {
        navigate(`/game/${gameId}`);
    };

    return (
        <div>
            <h1>Search for a Game</h1>
            <div>
                <input
                    type="text"
                    value={searchCriteria}
                    onChange={(e) => setSearchCriteria(e.target.value)}
                    placeholder="Enter search criteria"
                />
                <button onClick={handleSearch}>Search</button>
            </div>
            <div>
                <h2>Available Games</h2>
                <ul>
                    {availableGames.map((game) => (
                        <li key={game.id}>
                            {game.name} - {game.status}
                            <button onClick={() => handleJoinGame(game.id)}>Join</button>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default GameSearchPage;
