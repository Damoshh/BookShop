import React, { useState } from 'react';
import './LoginPopup.css';

const LoginPopup = ({ setShowLogin, setIsLoggedIn, setUserEmail, initialState, navigate }) => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isAdminLogin, setIsAdminLogin] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (isSubmitting) return;

        try {
            setIsSubmitting(true);

            if (isAdminLogin) {
                console.log('Starting admin login process...');
                
                const response = await fetch('http://localhost:8000/api/admin/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    credentials: 'include',
                    body: JSON.stringify({ 
                        email: formData.email, 
                        password: formData.password 
                    }),
                });

                console.log('Login response status:', response.status);
                
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Admin login failed');
                }

                const data = await response.json();
                console.log('Login response data:', data);
                
                if (!data.token) {
                    console.error('No token received in response');
                    throw new Error('No token received from server');
                }
                
                // Store admin data
                localStorage.setItem('sessionToken', data.token);
                localStorage.setItem('userEmail', formData.email);
                localStorage.setItem('userRole', 'admin');
                
                // Verify storage
                console.log('Stored auth data:', {
                    token: localStorage.getItem('sessionToken'),
                    email: localStorage.getItem('userEmail'),
                    role: localStorage.getItem('userRole')
                });

                setIsLoggedIn(true);
                setUserEmail(formData.email);
                
                window.dispatchEvent(new Event('loginStateChange'));
                
                console.log('About to navigate to admin dashboard...');
                navigate('/admin/dashboard'); // Changed from '/admin' to '/admin/dashboard'
                setShowLogin(false);
                
                alert('Admin login successful!');
            } else {
                // Regular user login/signup remains the same
                const payload = {
                    action: initialState === 'Sign Up' ? 'register' : 'login',
                    ...formData
                };

                const response = await fetch('http://localhost:8000/api/users', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Authentication failed');
                }

                const data = await response.json();
                
                localStorage.setItem('sessionToken', data.token);
                localStorage.setItem('userEmail', formData.email);
                localStorage.setItem('userRole', 'user');
                localStorage.setItem('userId', data.userId);

                window.dispatchEvent(new Event('loginStateChange'));

                setIsLoggedIn(true);
                setUserEmail(formData.email);
                setShowLogin(false);

                navigate('/'); // Redirect regular users to home
                alert(initialState === 'Sign Up' ? 'Account created successfully!' : 'Login successful');
            }
        } catch (error) {
            console.error('Login error:', error);
            alert(error.message || 'An unexpected error occurred');
        } finally {
            setIsSubmitting(false);
            setFormData({ name: '', email: '', password: '' });
        }
    };

    // Rest of the component remains the same
    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const toggleAdminLogin = () => {
        setIsAdminLogin(!isAdminLogin);
        setFormData({
            name: '',
            email: '',
            password: ''
        });
    };

    return (
        <div className="login-popup">
            <form className="login-popup-container" onSubmit={handleSubmit}>
                <div className="login-popup-title">
                    <h2>{isAdminLogin ? 'Admin Login' : initialState}</h2>
                    <i 
                        onClick={() => !isSubmitting && setShowLogin(false)} 
                        className="fa-solid fa-xmark"
                    ></i>
                </div>

                <div className="login-popup-inputs">
                    {initialState === 'Sign Up' && !isAdminLogin && (
                        <input 
                            type="text" 
                            name="name"
                            placeholder="Your name" 
                            required
                            value={formData.name}
                            onChange={handleChange}
                            disabled={isSubmitting}
                        />
                    )}
                    <input 
                        type="email" 
                        name="email"
                        placeholder={isAdminLogin ? 'Admin email' : 'Your email'}
                        required
                        value={formData.email}
                        onChange={handleChange}
                        disabled={isSubmitting}
                    />
                    <input 
                        type="password" 
                        name="password"
                        placeholder={isAdminLogin ? 'Admin password' : 'Your password'}
                        required
                        value={formData.password}
                        onChange={handleChange}
                        disabled={isSubmitting}
                    />
                </div>

                <button 
                    type="submit" 
                    disabled={isSubmitting}
                    className={isSubmitting ? 'button-processing' : ''}
                >
                    {isSubmitting 
                        ? 'Processing...' 
                        : (isAdminLogin 
                            ? 'Admin Login' 
                            : (initialState === 'Sign Up' 
                                ? 'Create account' 
                                : 'Login'))}
                </button>
                
                {!isSubmitting && (
                    <button 
                        type="button"
                        onClick={toggleAdminLogin}
                        className="admin-toggle-button"
                    >
                        {isAdminLogin ? 'Switch to User Login' : 'Admin Login'}
                    </button>
                )}

                <div className="login-popup-condition">
                    <input 
                        type="checkbox" 
                        required 
                        disabled={isSubmitting}
                    />
                    <p>By continuing, I agree to the terms of use & privacy policy.</p>
                </div>
            </form>
        </div>
    );
};

export default LoginPopup;