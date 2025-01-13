// src/utils/auth.js

// Shared logout function
export const handleLogout = (navigate, setIsLoggedIn = null, setUserEmail = null) => {
    // Clear all auth-related items from localStorage
    localStorage.removeItem('userEmail');
    localStorage.removeItem('isAdmin');
    localStorage.removeItem('sessionToken');
    localStorage.removeItem('adminToken');
    
    // Reset state if setters are provided
    if (setIsLoggedIn) setIsLoggedIn(false);
    if (setUserEmail) setUserEmail('');
    
    // Navigate to home page
    navigate('/');
};

// Check if user is authenticated
export const isAuthenticated = () => {
    const token = localStorage.getItem('sessionToken');
    return !!token;
};

// Check if user is admin
export const isAdmin = () => {
    return localStorage.getItem('isAdmin') === 'true' && isAuthenticated();
};