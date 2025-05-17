// ProtectedRoute.js
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './authcontext';

const ProtectedRoute = ({ children }) => {
    const { user } = useAuth();

    if (!user) {
        return <Navigate to="/connexion" replace />;
    }
    console.log('ProtectedRoute - User:', user);

    return children;
};

export default ProtectedRoute;
