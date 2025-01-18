import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { isAuthenticated, isAdmin } from '../utils/auth.js';

const ProtectedRoute = ({ children, requiresAdmin = true }) => {
    const location = useLocation();
    const authenticated = isAuthenticated();
    const adminStatus = isAdmin();

    console.log("Authenticated: ", authenticated);
    console.log("Admin Status: ", adminStatus);
    console.log("Requires Admin: ", requiresAdmin);

    if (!authenticated) {
        return <Navigate to="/" state={{ from: location }} replace />;
    }

    if (requiresAdmin && !adminStatus) {
        return <Navigate to="/" replace />;
    }

    return children;
};


export default ProtectedRoute;