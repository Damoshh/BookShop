import React, { useState, useContext } from 'react';
import { Link, useNavigate } from "react-router-dom";
import "./Navbar.css";
import { StoreContext } from '../../context/StoreContext';
import { handleLogout, isAuthenticated } from '../../utils/auth.js';

const Navbar = ({
  setShowLogin,
  isLoggedIn,
  setIsLoggedIn,
  setUserEmail,
  setInitialState
}) => {
  const navigate = useNavigate();
  const { getCartItemCount } = useContext(StoreContext);
  const cartItemCount = getCartItemCount ? getCartItemCount() : 0;
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);

  const handleCartClick = (e) => {
    if (!isLoggedIn) {
      e.preventDefault();
      alert('Please login first to access your cart');
      setInitialState('Login');
      setShowLogin(true);
    }
  };

  const handleSearch = async (e) => {
    const query = e.target.value;
    setSearchQuery(query);

    if (query.length >= 2) {
      try {
        const response = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
        const data = await response.json();
        setSearchResults(data);
      } catch (error) {
        console.error('Search error:', error);
        setSearchResults([]);
      }
    } else {
      setSearchResults([]);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
      setSearchResults([]);
    }
  };

  const handleSignOut = () => {
    handleLogout(navigate, setIsLoggedIn, setUserEmail);
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
    <nav className='navbar'>
      <Link to='/' className='nav-logo'>
        Readify.
      </Link>

      <div className='nav-search'>
        <form onSubmit={handleSearchSubmit} className='search-form'>
          <div className='search-input-container'>
            <i className="fa-solid fa-magnifying-glass search-icon"></i>
            <input 
              type="text" 
              placeholder="Search..." 
              className='search-input-home'
              value={searchQuery}
              onChange={handleSearch}
              aria-label="Search books"
            />
          </div>
          {searchResults.length > 0 && (
            <div className="search-results">
              {searchResults.map((result) => (
                <Link 
                  key={result.id} 
                  to={`/book/${result.id}`}
                  className="search-result-item"
                  onClick={() => {
                    setSearchQuery('');
                    setSearchResults([]);
                  }}
                >
                  {result.title}
                </Link>
              ))}
            </div>
          )}
        </form>
      </div>

      <div className='nav-right'>
        {isLoggedIn ? (
          <>
            <div className='cart-container-logged-in'>
              <Link to='/cart' className='cart-link'>
                <i className="fa-solid fa-cart-shopping"></i>
                <span>RM {cartItemCount > 0 ? (cartItemCount * 2).toFixed(2) : '0.00'}</span>
                {cartItemCount > 0 && <div className='cart-badge'>{cartItemCount}</div>}
              </Link>
            </div>

            <Link to="/profile" className="icon-container">
              <i className="fa-solid fa-user"></i>
            </Link>

            <button onClick={handleSignOut} className='logout-btn'>
              Logout
            </button>
          </>
        ) : (
          <>
            <div className='cart-container-logged-out'>
              <button onClick={handleCartClick} className='cart-link'>
                <i className="fa-solid fa-cart-shopping"></i>
              </button>
            </div>
            <div className='auth-buttons'>
              <button onClick={handleLogin} className='login-btn'>
                LOGIN
              </button>
              <button onClick={handleSignUp} className='signup-btn'>
                Open Account
              </button>
            </div>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;