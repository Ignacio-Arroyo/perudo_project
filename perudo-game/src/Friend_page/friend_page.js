import React, { useState, useContext, useEffect } from 'react';
import { UserContext } from '../Auth/UserContext';
import axios from 'axios';

const FriendsPage = () => {
    const { user } = useContext(UserContext);
    const [friends, setFriends] = useState([]);
    const [friendRequestSent, setFriendRequestSent] = useState(false);
    const [friendRequestId, setFriendRequestId] = useState('');

    useEffect(() => {
        const fetchFriends = async () => {
            if (user && user.id) {
                try {
                    const response = await axios.get(`/api/players/${user.id}/friends`);
                    setFriends(response.data);
                } catch (error) {
                    console.error('Error fetching friends:', error);
                }
            }
        };

        fetchFriends();
    }, [user]);

    const handleSendFriendRequest = async () => {
        if (!user.username || !friendRequestId) {
            console.error('Username or Friend Code is missing');
            return;
        }

        try {
            await axios.post('/api/friends/request', null, {
                params: {
                    fromPlayerUsername: user.username,
                    toPlayerFriendCode: friendRequestId
                }
            });
            setFriendRequestSent(true);
        } catch (error) {
            console.error('Error sending friend request:', error);
        }
    };

    if (!user) {
        return <div>Please log in to view your friends.</div>;
    }

    return (
        <div>
            <h1>Friends</h1>
            <div>
                <h2>Add Friend</h2>
                <input
                    type="text"
                    value={friendRequestId}
                    onChange={(e) => setFriendRequestId(e.target.value)}
                    placeholder="Enter friend's Friend Code"
                />
                <button onClick={handleSendFriendRequest}>Send Friend Request</button>
                {friendRequestSent && <p>Friend request sent!</p>}
            </div>
            <div>
                <h2>Your Friends</h2>
                <ul>
                    {friends.length > 0 ? (
                        friends.map(friend => (
                            <li key={friend.player_id}>{friend.username}</li>
                        ))
                    ) : (
                        <p>No friends yet.</p>
                    )}
                </ul>
            </div>
        </div>
    );
};

export default FriendsPage;
