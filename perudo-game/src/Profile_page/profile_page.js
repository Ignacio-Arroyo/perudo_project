import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Profile_page = ({ playerId }) => {
    const [player, setPlayer] = useState(null);

    useEffect(() => {
        const fetchPlayer = async () => {
            try {
                const response = await axios.get(`/api/players/${playerId}`);
                setPlayer(response.data);
            } catch (error) {
                console.error('Error fetching player data:', error);
            }
        };

        fetchPlayer();
    }, [playerId]);

    if (!player) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>Player Profile</h1>
            <p>Name: {player.nom} {player.prenom}</p>
            <p>Username: {player.username}</p>
            <p>Friend Code: {player.friendCode}</p>
            <p>Win Rate: {player.winRate}%</p>
            <h2>Friends</h2>
            <ul>
                {player.friends && player.friends.map(friend => (
                    <li key={friend.player_id}>{friend.username}</li>
                ))}
            </ul>
            <h2>Game Records</h2>
            <ul>
                {player.gameRecords && player.gameRecords.map(record => (
                    <li key={record.record_id}>
                        Victory: {record.victory ? 'Yes' : 'No'}, Score: {record.score}
                    </li>
                ))}
            </ul>
            <h2>Inventory</h2>
            <ul>
                {player.ownedDice && player.ownedDice.map(dice => (
                    <li key={dice.dice_id}>{dice.type}</li>
                ))}
            </ul>
        </div>
    );
};

export default Profile_page;
