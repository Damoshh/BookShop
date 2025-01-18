import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Header.css';

const Header = () => {
  const navigate = useNavigate();

  const handleViewMenu = () => {
    navigate('/search');
  };

  return (
    <div className='header'>
      <div className="header-content">
        <h2>Discover Your Next Read</h2>
        <p>Explore our vast collection of books across various genres. Find your perfect read today!</p>
        <button onClick={handleViewMenu}>Browse Books</button>
      </div>
    </div>
  );
};

export default Header;