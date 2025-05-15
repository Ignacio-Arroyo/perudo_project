import React, { useContext } from 'react';
import { UserContext } from '../Auth/UserContext';

const Profile_page = () => {
    const { user } = useContext(UserContext);

    if (!user) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>Player Profile</h1>
            <p>Name: {user.nom} {user.prenom}</p>
            <p>Username: {user.username}</p>
            <p>Friend Code: {user.friendCode}</p>
            <p>Win Rate: {user.winRate}%</p>
            <h2>Friends</h2>
            <ul>
                {user.friends && user.friends.map(friend => (
                    <li key={friend.player_id}>{friend.username}</li>
                ))}
            </ul>
            <h2>Game Records</h2>
            <ul>
                {user.gameRecords && user.gameRecords.map(record => (
                    <li key={record.record_id}>
                        Victory: {record.victory ? 'Yes' : 'No'}, Score: {record.score}
                    </li>
                ))}
            </ul>
            <h2>Inventory</h2>
            <ul>
                {user.ownedDice && user.ownedDice.map(dice => (
                    <li key={dice.dice_id}>{dice.type}</li>
                ))}
            </ul>
        </div>
    );
};

export default Profile_page;
