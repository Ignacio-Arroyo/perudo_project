import React, { useState, useContext, useEffect } from 'react';
import { UserContext } from '../Auth/UserContext';
import axios from 'axios';

const MailPage = () => {
    const { user } = useContext(UserContext);
    const [friendRequests, setFriendRequests] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchFriendRequests = async () => {
            try {
                if (!user) {
                    setError('User is not logged in');
                    return;
                }

                const userId = user.id || user.playerId;
                if (!userId) {
                    setError('No valid user ID found');
                    console.error('User object is missing ID:', user);
                    return;
                }

                console.log('Fetching friend requests for user:', userId);
                const response = await axios.get(`/api/friends/requests/${userId}`);
                setFriendRequests(response.data);
                setError(null);
            } catch (error) {
                const errorMessage = error.response?.data?.message || 'Error fetching friend requests';
                setError(errorMessage);
                console.error('Error:', errorMessage);
            }
        };

        if (user) {
            fetchFriendRequests();
        }
    }, [user]);

    const handleAcceptFriendRequest = async (requestId) => {
        try {
            await axios.post('/api/friends/accept', null, {
                params: {
                    requestId: requestId
                }
            });
            setFriendRequests(friendRequests.filter(request => request.id !== requestId));
        } catch (error) {
            console.error('Error accepting friend request:', error);
        }
    };

    const handleRejectFriendRequest = async (requestId) => {
        try {
            await axios.post('/api/friends/reject', null, {
                params: {
                    requestId: requestId
                }
            });
            setFriendRequests(friendRequests.filter(request => request.id !== requestId));
        } catch (error) {
            console.error('Error rejecting friend request:', error);
        }
    };

    if (!user) {
        return <div>Please log in to view your friend requests.</div>;
    }

    return (
        <div className="mail-page">
            <h1>Mail</h1>
            
            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            <div>
                <h2>Friend Requests</h2>
                <ul>
                    {friendRequests.map(request => (
                        <li key={request.id}>
                            {request.fromPlayer.username} wants to be your friend.
                            <button onClick={() => handleAcceptFriendRequest(request.id)}>Accept</button>
                            <button onClick={() => handleRejectFriendRequest(request.id)}>Reject</button>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default MailPage;
