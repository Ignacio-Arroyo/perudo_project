// ProtectedRoute.js
import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { UserContext } from './UserContext';

const ProtectedRoute = ({ children }) => {
    const { user } = useContext(UserContext);

    if (!user) {
        return <Navigate to="/connexion" replace />;
    }
    console.log('ProtectedRoute - User:', user);

    return children;
};

export default ProtectedRoute;
