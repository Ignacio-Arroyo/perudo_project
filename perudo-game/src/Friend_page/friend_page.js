import React, { useState, useEffect } from 'react';
import { useAuth } from '../Auth/authcontext';
import axios from 'axios';
import './friend_page.css';

const FriendsPage = () => {
    const { user } = useAuth();
    const [friends, setFriends] = useState([]);
    const [friendRequestSent, setFriendRequestSent] = useState(false);
    const [friendRequestId, setFriendRequestId] = useState('');
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [showDebug, setShowDebug] = useState(false);

    useEffect(() => {
        if (user) {
            fetchFriends();
        }
    }, [user]);

    const fetchFriends = async () => {
        if (!user) return;
        
        // Determine which ID to use based on what's available in the user object
        const playerId = user.player_id || user.id;
        if (!playerId) {
            console.error("No valid player ID found in user object:", user);
            setError("Could not determine your player ID. Please try logging out and back in.");
            return;
        }

        console.log(`Fetching friends for player ID: ${playerId}`);
        
        setIsLoading(true);
        setError(''); // Clear previous errors
        try {
            console.log(`Making API call to: http://localhost:8080/api/friends/${playerId}/friends`);
            const response = await axios.get(`http://localhost:8080/api/friends/${playerId}/friends`);
            console.log('Friends API raw response:', response);
            
            // Ensure we're setting an array
            if (Array.isArray(response.data)) {
                console.log(`Received ${response.data.length} friends`);
                setFriends(response.data);
            } else if (response.data === null || response.data === undefined) {
                console.error('Friends data is null or undefined');
                setFriends([]);
                setError('No friend data received from server');
            } else if (typeof response.data === 'object') {
                console.log('Response is an object, trying to extract friends array');
                // Check if response.data has a property that contains the friends array
                const possibleArrayProps = Object.keys(response.data).filter(key => 
                    Array.isArray(response.data[key])
                );
                
                if (possibleArrayProps.length > 0) {
                    console.log(`Found possible friends array in property: ${possibleArrayProps[0]}`);
                    setFriends(response.data[possibleArrayProps[0]]);
                } else {
                    // Try to convert the object to an array
                    try {
                        const friendsArray = Object.values(response.data);
                        console.log('Converted object to array:', friendsArray);
                        setFriends(friendsArray);
                    } catch (conversionError) {
                        console.error('Error converting to array:', conversionError);
                        setFriends([]);
                        setError('Received invalid friend data format from server');
                    }
                }
            } else if (typeof response.data === 'string') {
                console.log('Response is a string, attempting to parse as JSON');
                let dataToParse = response.data;
                console.log('Original string content for parsing:', dataToParse);

                // Attempt to fix the specific malformed JSON pattern like "friends":]
                const malformedPattern = /"friends":\s*]/g;
                const correctPattern = '"friends":[]';

                if (malformedPattern.test(dataToParse)) {
                    console.log('Malformed pattern "friends":] detected. Attempting to patch.');
                    dataToParse = dataToParse.replace(malformedPattern, correctPattern);
                    console.log('Patched string for parsing:', dataToParse);
                }

                try {
                    const parsedData = JSON.parse(dataToParse);
                    console.log('Successfully parsed string to:', parsedData);
                    
                    if (Array.isArray(parsedData)) {
                        setFriends(parsedData);
                    } else if (parsedData && typeof parsedData === 'object') {
                        // Try to find an array in the parsed object
                        const possibleArrays = Object.values(parsedData).filter(val => Array.isArray(val));
                        if (possibleArrays.length > 0) {
                            setFriends(possibleArrays[0]);
                        } else {
                            // Just use the object values as an array
                            setFriends(Object.values(parsedData));
                        }
                    } else {
                        setFriends([]);
                        setError('Could not extract friends list from server response');
                    }
                } catch (parseError) {
                    console.error('Error parsing response string (even after attempting to patch):', parseError);
                    console.error('String that failed parsing:', dataToParse); // Log the string that actually failed
                    setFriends([]);
                    setError('Server returned invalid data format, and patching failed.');
                }
            } else {
                console.error('Friends data has unexpected format:', typeof response.data);
                setFriends([]);
                setError('Received invalid friend data format from server');
            }
        } catch (error) {
            console.error('Error fetching friends:', error);
            if (error.response) {
                console.error('Error response:', error.response.data);
                console.error('Status code:', error.response.status);
                setError(`Server error (${error.response.status}): ${error.response.data || 'Could not load friends'}`);
            } else if (error.request) {
                console.error('No response received:', error.request);
                setError('No response from server. Please check your connection.');
            } else {
                console.error('Error message:', error.message);
                setError('Could not load friends. Please try again later.');
            }
            setFriends([]); // Ensure friends is an array even on error
        } finally {
            setIsLoading(false);
        }
    };
    
    const refreshFriends = () => {
        fetchFriends();
    };

    const handleSendFriendRequest = async () => {
        // Reset states
        setError('');
        setSuccessMessage('');
        setFriendRequestSent(false);
        
        if (!user || !user.username) {
            setError('You must be logged in to send friend requests.');
            return;
        }

        if (!friendRequestId || friendRequestId.trim() === '') {
            setError('Please enter a valid friend code.');
            return;
        }

        console.log(`Sending friend request from ${user.username} to friend code: ${friendRequestId}`);
        setIsLoading(true);
        
        try {
            const response = await axios.post('http://localhost:8080/api/friends/request', null, {
                params: {
                    fromPlayerUsername: user.username,
                    toPlayerFriendCode: friendRequestId
                }
            });
            
            console.log('Friend request response:', response);
            setFriendRequestSent(true);
            setSuccessMessage(response.data || 'Friend request sent successfully!');
            setFriendRequestId(''); // Clear the input field
        } catch (error) {
            console.error('Error sending friend request:', error);
            if (error.response) {
                console.error('Error response:', error.response.data);
                console.error('Status code:', error.response.status);
                
                if (error.response.status === 409) {
                    setError(error.response.data || 'Friend request already exists or you are already friends.');
                } else if (error.response.status === 404) {
                    setError(error.response.data || 'No user found with that friend code.');
                } else if (error.response.status === 400) {
                    setError(error.response.data || 'Invalid request. Please check the friend code.');
                } else if (error.response.data) {
                    setError(error.response.data);
                } else {
                    setError(`Error (${error.response.status}): Please try again later.`);
                }
            } else {
                setError('Network error. Please check your connection and try again.');
            }
        } finally {
            setIsLoading(false);
        }
    };

    if (!user) {
        return (
            <div className="friends-container">
                <div className="error-message">Please log in to view your friends.</div>
            </div>
        );
    }

    // Extra protection to ensure friends is always an array
    const friendsList = Array.isArray(friends) ? friends : [];

    return (
        <div className="friends-container">
            <h1>Friends</h1>
            
            <div className="debug-toggle">
                <button onClick={() => setShowDebug(!showDebug)} className="debug-toggle-button">
                    {showDebug ? "Hide Debug Info" : "Show Debug Info"}
                </button>
                
                {showDebug && (
                    <div className="debug-section">
                        <button onClick={refreshFriends} className="refresh-button">
                            Refresh Friends List
                        </button>
                        <div className="debug-info">
                            <p>User ID: {user.player_id || user.id}</p>
                            <p>Username: {user.username}</p>
                        </div>
                    </div>
                )}
            </div>
            
            <div className="friends-section">
                <div className="add-friend-card">
                    <h2>Add Friend</h2>
                    <p className="friend-code-info">
                        Your friend code: <span className="friend-code">{user.friendCode}</span>
                        <button 
                            className="copy-button" 
                            onClick={() => {
                                navigator.clipboard.writeText(user.friendCode);
                                alert("Friend code copied to clipboard!");
                            }}
                        >
                            Copy
                        </button>
                    </p>
                    
                    <div className="friend-form">
                        <input
                            type="text"
                            value={friendRequestId}
                            onChange={(e) => setFriendRequestId(e.target.value)}
                            placeholder="Enter friend's Friend Code"
                            className="friend-input"
                        />
                        <button 
                            onClick={handleSendFriendRequest}
                            disabled={isLoading}
                            className="friend-button"
                        >
                            {isLoading ? "Sending..." : "Send Friend Request"}
                        </button>
                    </div>
                    
                    {error && <p className="error-message">{error}</p>}
                    {successMessage && <p className="success-message">{successMessage}</p>}
                </div>
                
                <div className="friends-list-card">
                    <h2>Your Friends</h2>
                    {showDebug && (
                        <div className="debug-info">
                            <p>Friends count: {friendsList.length}</p>
                            <p>Raw friends data: {JSON.stringify(friends).substring(0, 100)}</p>
                        </div>
                    )}
                    {isLoading ? (
                        <p className="loading-message">Loading friends...</p>
                    ) : friendsList.length > 0 ? (
                        <div className="friends-list-wrapper">
                            <ul className="friends-list">
                                {friendsList.map((friend, index) => (
                                    <li key={friend.player_id || friend.id || `friend-${index}`} className="friend-item">
                                        <div className="friend-info">
                                            <span className="friend-username">{friend.username}</span>
                                            {(friend.prenom || friend.nom) && (
                                                <span className="friend-name">{friend.prenom} {friend.nom}</span>
                                            )}
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    ) : (
                        <p className="no-friends">No friends yet. Send some requests!</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default FriendsPage;
