import React, { useState } from 'react'
import './LoginPopup.css'

const LoginPopup = ({setShowLogin, setIsLoggedIn, setUserEmail, initialState, navigate}) => {  const [formData, setFormData] = useState({
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
        
        const endpoint = isAdminLogin ? '/api/admin/login' : '/api/users';
        const payload = isAdminLogin ? {
          email: formData.email,
          password: formData.password
        } : {
          action: initialState === 'Sign Up' ? 'register' : 'login',
          ...formData
        };
        
        const response = await fetch(`http://localhost:8000${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        
        if (response.ok) {
          setIsLoggedIn(true);
          setUserEmail(formData.email);
          localStorage.setItem('userEmail', formData.email);
          if (isAdminLogin) {
              localStorage.setItem('isAdmin', 'true');
              navigate('/admin'); // Add this line to redirect to admin dashboard
          }
          alert(data.message);
          setShowLogin(false);
      } else {
            throw new Error(data.message || 'Login failed');
        }
    } catch (error) {
        console.error('Error:', error);
        alert(error.message || 'An error occurred during login');
    } finally {
        setIsSubmitting(false);
        setFormData({
            name: '',
            email: '',
            password: ''
        });
    }
  };

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
    <div className='login-popup'>
      <form className='login-popup-container' onSubmit={handleSubmit}>
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
              placeholder='Your name' 
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
          {isSubmitting ? 'Processing...' : (isAdminLogin ? 'Admin Login' : (initialState === 'Sign Up' ? 'Create account' : 'Login'))}
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
          <input type="checkbox" required disabled={isSubmitting}/>
          <p>By continuing, i agree to the terms of use & privacy policy.</p>
        </div>
      </form>
    </div>
  );
};

export default LoginPopup;