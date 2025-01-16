// utils/auth.js

export const isAuthenticated = () => {
    try {
        const sessionToken = localStorage.getItem('sessionToken');
        const userEmail = localStorage.getItem('userEmail');
        return Boolean(sessionToken && userEmail);
    } catch (error) {
        console.error('Auth check error:', error);
        return false;
    }
};

export const isAdmin = () => {
    try {
        const userRole = localStorage.getItem('userRole');
        return isAuthenticated() && userRole === 'admin';
    } catch (error) {
        console.error('Admin check error:', error);
        return false;
    }
};

export const handleLogout = (navigate, setIsLoggedIn, setUserEmail) => {
    try {
        localStorage.removeItem('sessionToken');
        localStorage.removeItem('userEmail');
        localStorage.removeItem('userRole');
        localStorage.removeItem('cart');
        
        if (setIsLoggedIn) setIsLoggedIn(false);
        if (setUserEmail) setUserEmail('');
        
        if (navigate) navigate('/');
    } catch (error) {
        console.error('Logout error:', error);
    }
};

export const handleLogin = async (email, password) => {
    try {
        const response = await fetch('http://localhost:8000/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            throw new Error('Login failed');
        }

        const data = await response.json();
        
        // Store the authentication data
        localStorage.setItem('sessionToken', data.token);
        localStorage.setItem('userEmail', email);
        if (data.role) {
            localStorage.setItem('userRole', data.role);
        }

        // Dispatch a custom event to notify about login
        window.dispatchEvent(new Event('loginStateChange'));
        
        return true;
    } catch (error) {
        console.error('Login error:', error);
        return false;
    }
};