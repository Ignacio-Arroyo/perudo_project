import React, { useState, useEffect } from 'react';
import { useAuth } from '../Auth/authcontext';
import axios from 'axios';
import './mail.css';

const MailPage = () => {
    const { user } = useAuth();
    const [friendRequests, setFriendRequests] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [debugInfo, setDebugInfo] = useState({ userId: null, userObject: null });
    const [showDebug, setShowDebug] = useState(false);

    useEffect(() => {
        if (user) {
            // Store debug info about the user object
            setDebugInfo({
                userId: user.player_id || user.id,
                userObject: JSON.stringify(user, null, 2)
            });
            fetchFriendRequests();
        }
    }, [user]);

    const fetchFriendRequests = async () => {
        if (!user) return;

        // Determine which ID to use based on what's available in the user object
        const playerId = user.id || user.player_id;
        if (!playerId) {
            console.error("No valid player ID found in user object:", user);
            setError("Could not determine your player ID. Please try logging out and back in.");
            return;
        }

        console.log(`Fetching friend requests for player ID: ${playerId}, username: ${user.username}`);
        
        setIsLoading(true);
        try {
            // Try both endpoints to see which one works
            let response;
            try {
                // First try with regular id
                response = await axios.get(`http://localhost:8080/api/friends/requests/${playerId}`);
                console.log('Friend requests from standard endpoint:', response.data);
            } catch (err) {
                console.log('Standard endpoint failed, trying player_id endpoint');
                // If that fails, try with player_id endpoint
                response = await axios.get(`http://localhost:8080/api/friends/requests/player/${playerId}`);
                console.log('Friend requests from player_id endpoint:', response.data);
            }
            
            if (response && response.data) {
                console.log('Friend requests count:', response.data.length);
                console.log('Friend requests data:', JSON.stringify(response.data, null, 2));
                setFriendRequests(response.data);
            } else {
                console.log('Friend requests response has no data');
                setFriendRequests([]);
            }
        } catch (error) {
            console.error('Error fetching friend requests:', error);
            if (error.response) {
                console.error('Error response:', error.response.data);
                console.error('Status code:', error.response.status);
            }
            setError(`Failed to load friend requests: ${error.message}`);
        } finally {
            setIsLoading(false);
        }
    };

    const refreshRequests = () => {
        fetchFriendRequests();
    };

    const handleAcceptFriendRequest = async (requestId) => {
        setIsLoading(true);
        setError('');
        setSuccessMessage('');
        
        try {
            await axios.post('http://localhost:8080/api/friends/accept', null, {
                params: { requestId }
            });
            
            // Update the friend requests list
            setFriendRequests(friendRequests.filter(request => request.id !== requestId));
            setSuccessMessage('Friend request accepted!');
            
            // You could also refresh the friends list here
            // fetchFriends();
        } catch (error) {
            console.error('Error accepting friend request:', error);
            setError('Failed to accept friend request. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleRejectFriendRequest = async (requestId) => {
        setIsLoading(true);
        setError('');
        setSuccessMessage('');
        
        try {
            await axios.post('http://localhost:8080/api/friends/reject', null, {
                params: { requestId }
            });
            
            // Update the friend requests list
            setFriendRequests(friendRequests.filter(request => request.id !== requestId));
            setSuccessMessage('Friend request rejected.');
        } catch (error) {
            console.error('Error rejecting friend request:', error);
            setError('Failed to reject friend request. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    if (!user) {
        return (
            <div className="mail-container">
                <div className="error-message">Please log in to view your messages.</div>
            </div>
        );
    }

    return (
        <div className="mail-container">
            <h1>Mail</h1>
            
            {error && <div className="error-message">{error}</div>}
            {successMessage && <div className="success-message">{successMessage}</div>}
            
            <div className="debug-toggle">
                <button onClick={() => setShowDebug(!showDebug)} className="debug-toggle-button">
                    {showDebug ? "Hide Debug Info" : "Show Debug Info"}
                </button>
                
                {showDebug && (
                    <div className="debug-section">
                        <button onClick={refreshRequests} className="refresh-button">
                            Refresh Requests
                        </button>
                        <div className="debug-info">
                            <p>User ID: {debugInfo.userId}</p>
                            <p>Username: {user.username}</p>
                            <p>Friend Code: {user.friendCode}</p>
                        </div>
                    </div>
                )}
            </div>
            
            <div className="mail-card">
                <h2>Friend Requests</h2>
                {isLoading ? (
                    <div className="loading-message">Loading friend requests...</div>
                ) : friendRequests.length > 0 ? (
                    <ul className="request-list">
                        {friendRequests.map(request => (
                            <li key={request.id} className="request-item">
                                <div className="request-info">
                                    <span className="request-username">{request.fromPlayer.username}</span>
                                    <span className="request-text">wants to be your friend</span>
                                </div>
                                <div className="request-actions">
                                    <button 
                                        className="accept-button" 
                                        onClick={() => handleAcceptFriendRequest(request.id)}
                                        disabled={isLoading}
                                    >
                                        Accept
                                    </button>
                                    <button 
                                        className="reject-button" 
                                        onClick={() => handleRejectFriendRequest(request.id)}
                                        disabled={isLoading}
                                    >
                                        Reject
                                    </button>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <div className="no-requests">No friend requests at the moment.</div>
                )}
            </div>
        </div>
    );
};

export default MailPage;
