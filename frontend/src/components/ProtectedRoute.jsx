import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

const ProtectedRoute = ({ children, requiresAdmin = false }) => {
    const location = useLocation();
    
    // Check for tokens
    const sessionToken = localStorage.getItem('sessionToken');
    const adminToken = localStorage.getItem('adminToken');
    const userRole = localStorage.getItem('userRole');
    
    // Determine authentication status
    const isAuthenticated = Boolean(sessionToken || adminToken);
    const isAdminUser = userRole === 'admin' && adminToken;

    // Debug logging (you can remove this in production)
    console.log('Protected Route Status:', {
        path: location.pathname,
        isAuthenticated,
        isAdminUser,
        requiresAdmin,
        userRole
    });

    // Case 1: Not authenticated at all - redirect to home
    if (!isAuthenticated) {
        return <Navigate 
            to="/" 
            state={{ from: location, message: "Please login to continue" }} 
            replace 
        />;
    }

    // Case 2: Requires admin but user is not admin - redirect to home
    if (requiresAdmin && !isAdminUser) {
        return <Navigate 
            to="/" 
            state={{ from: location, message: "Admin access required" }} 
            replace 
        />;
    }

    // Case 3: Admin trying to access user routes - allow it
    if (!requiresAdmin && isAdminUser) {
        return children;
    }

    // Case 4: Regular user accessing user routes - allow it
    if (!requiresAdmin && isAuthenticated) {
        return children;
    }

    // Case 5: Admin accessing admin routes - allow it
    if (requiresAdmin && isAdminUser) {
        return children;
    }

    // Default case: shouldn't reach here, but redirect to home just in case
    return <Navigate to="/" replace />;
};

export default ProtectedRoute;