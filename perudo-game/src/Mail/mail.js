import React, { useState, useContext, useEffect } from 'react';
import { UserContext } from '../Auth/UserContext';
import axios from 'axios';

const MailPage = () => {
    const { user } = useContext(UserContext);
    const [friendRequests, setFriendRequests] = useState([]);

    useEffect(() => {
        const fetchFriendRequests = async () => {
            try {
                console.log('User object:', user); // Log the user object to the console

                if (!user) {
                    console.error('User is undefined');
                    return;
                }

                if (!user.id) {
                    console.error('user.id is undefined');
                    return;
                }

                const response = await axios.get(`/api/friends/requests/${user.id}`);
                setFriendRequests(response.data);
            } catch (error) {
                console.error('Error fetching friend requests:', error);
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
        <div>
            <h1>Mail</h1>
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
