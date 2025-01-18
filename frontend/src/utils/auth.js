// utils/auth.js

export const isAuthenticated = () => {
    try {
        const sessionToken = localStorage.getItem('sessionToken');
        const userEmail = localStorage.getItem('userEmail');
        const userId = localStorage.getItem('userId');
        return Boolean(sessionToken && userEmail && userId);
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

export const getCurrentUser = () => {
    try {
        return {
            id: localStorage.getItem('userId'),
            email: localStorage.getItem('userEmail'),
            role: localStorage.getItem('userRole')
        };
    } catch (error) {
        console.error('Get user error:', error);
        return null;
    }
};

export const handleLogout = (navigate, setIsLoggedIn, setUserEmail) => {
    try {
        const userId = localStorage.getItem('userId');
        
        // Remove user-specific data
        if (userId) {
            localStorage.removeItem(`cart_${userId}`);
            localStorage.removeItem(`wishlist_${userId}`);
        }
        
        // Remove auth data
        localStorage.removeItem('sessionToken');
        localStorage.removeItem('userEmail');
        localStorage.removeItem('userRole');
        localStorage.removeItem('userId');
        
        if (setIsLoggedIn) setIsLoggedIn(false);
        if (setUserEmail) setUserEmail('');
        
        if (navigate) navigate('/');
    } catch (error) {
        console.error('Logout error:', error);
    }
};

export const handleLogin = async (email, password) => {
    try {
        const response = await fetch('/api/users', {  // Changed from http://localhost:8000/api/login
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ 
                action: 'login',
                email, 
                password 
            }),
        });

        if (!response.ok) {
            throw new Error('Login failed');
        }

        const data = await response.json();
        
        localStorage.setItem('sessionToken', data.token);
        localStorage.setItem('userEmail', email);
        localStorage.setItem('userId', data.userId);
        if (data.role) {
            localStorage.setItem('userRole', data.role);
        }

        window.dispatchEvent(new Event('loginStateChange'));
        
        return true;
    } catch (error) {
        console.error('Login error:', error);
        return false;
    }
};