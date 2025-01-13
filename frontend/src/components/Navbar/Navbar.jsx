import React, { useContext } from 'react';
import { Link, useNavigate } from "react-router-dom";
import "./Navbar.css";
import { StoreContext } from '../../context/StoreContext';

export const Navbar = ({
  theme, 
  setTheme, 
  setShowLogin, 
  isLoggedIn, 
  setIsLoggedIn, 
  userEmail, 
  setUserEmail,
  setInitialState 
}) => {
  const navigate = useNavigate();
  const { cartItems, getCartItemCount } = useContext(StoreContext);
  const cartItemCount = getCartItemCount ? getCartItemCount() : 0;

  const handleSignOut = () => {
    localStorage.removeItem('userEmail');
    localStorage.removeItem('isAdmin');  
    localStorage.removeItem('sessionToken');
    setIsLoggedIn(false);
    setUserEmail('');
    navigate('/');  
  };

  const handleLogin = () => {
    setInitialState('Login');
    setShowLogin(true);
  };

  const handleSignUp = () => {
    setInitialState('Sign Up');
    setShowLogin(true);
  };

  return (
    <div className='navbar'>
      <Link to='/' className='nav-title'>
        Readify.
      </Link>
      
      <div className='nav-menu'>
        <Link to='/'>HOME</Link>
        <Link to='/'>MENU</Link>
        <Link to='/'>MOBILE APP</Link>
        <Link to='/'>CONTACT US</Link>
      </div>
      
      <div className='nav-right'>
        <i className='fa-solid fa-magnifying-glass'></i>
        
        <Link to='./cart'>
          <div className='cart-icon'>
            <i className="fa-solid fa-cart-shopping"></i>
            {cartItemCount > 0 && <div className='dot'>{cartItemCount}</div>}
          </div>
        </Link>

        {isLoggedIn ? (
          <div className='auth-section'>
            <div className='user-profile'>
              <i className="fa-solid fa-user"></i>
              <span className='user-email'>{userEmail}</span>
            </div>
            <button className='logout-btn' onClick={handleSignOut}>Logout</button>
          </div>
        ) : (
          <div className='auth-buttons'>
            <button className='login-btn' onClick={handleLogin}>Login</button>
            <button className='signup-btn' onClick={handleSignUp}>Open Account</button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Navbar;