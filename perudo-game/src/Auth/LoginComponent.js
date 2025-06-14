// LoginComponent.js
import React, { useState } from 'react';
import { useAuth } from './authcontext';
import axios from 'axios';

const LoginComponent = () => {
    const { login } = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = async () => {
        try {
            const response = await axios.post('/api/players/login', { username, password });
            const userData = response.data; // Assuming the response contains user data including the id
            console.log('User data:', userData); // Log the user data to verify that it includes the id
            login(userData); // Update AuthContext with user data
        } catch (error) {
            console.error('Error logging in:', error);
        }
    };

    return (
        <div>
            <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Username" />
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" />
            <button onClick={handleLogin}>Login</button>
        </div>
    );
};

export default LoginComponent;
