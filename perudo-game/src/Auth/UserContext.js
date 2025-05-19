// UserContext.js
import React, { createContext, useState, useEffect, useContext } from 'react';

export const UserContext = createContext();

export const UserProvider = ({ children }) => {
    const [user, setUser] = useState(null);

    const normalizeUserData = (userData) => {
        if (!userData) {
            console.error('No user data provided');
            return null;
        }

        // Log the received data for debugging
        console.log('Normalizing user data:', userData);

        // Ensure we have required fields
        if (!userData.id && !userData.playerId) {
            console.error('User data missing ID:', userData);
            return null;
        }

        if (!userData.username) {
            console.error('User data missing username:', userData);
            return null;
        }

        // Create normalized user object
        const normalizedUser = {
            id: userData.id || userData.playerId,
            username: userData.username,
            nom: userData.nom || '',
            prenom: userData.prenom || '',
            friendCode: userData.friendCode || ''
        };

        console.log('Normalized user data:', normalizedUser);
        return normalizedUser;
    };

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            try {
                const parsedUser = JSON.parse(storedUser);
                const normalizedUser = normalizeUserData(parsedUser);
                
                if (normalizedUser) {
                    setUser(normalizedUser);
                    console.log('Loaded normalized user:', normalizedUser);
                } else {
                    // Invalid stored user data, clear it
                    localStorage.removeItem('user');
                }
            } catch (error) {
                console.error('Error parsing stored user data:', error);
                localStorage.removeItem('user');
            }
        }
    }, []);

    const login = (userData) => {
        const normalizedUser = normalizeUserData(userData);
        
        if (!normalizedUser) {
            throw new Error('Invalid user data provided for login');
        }

        // Store user data and update state
        localStorage.setItem('user', JSON.stringify(normalizedUser));
        setUser(normalizedUser);
        console.log('User logged in successfully:', normalizedUser);
    };

    const logout = () => {
        localStorage.removeItem('user');
        setUser(null);
        console.log('User logged out');
    };

    return (
        <UserContext.Provider value={{ user, login, logout }}>
            {children}
        </UserContext.Provider>
    );
};

// Custom hook to use the UserContext
export const useUser = () => {
    return useContext(UserContext);
};

// Example component showing how to use the user context
const ExampleComponent = () => {
    const { user } = useUser();

    // The user object will always have an id if it exists
    console.log(user?.id); // Will be undefined only if not logged in

    return <div>User ID: {user?.id}</div>;
};