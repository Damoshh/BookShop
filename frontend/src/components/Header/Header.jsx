import React from 'react';
import './Header.css';

const Header = ({ onBrowseBooks }) => {
  return (
    <div className='header'>
      <div className="header-content">
        <h2>Discover Your Next Read</h2>
        <p>Explore our vast collection of books across various genres. Find your perfect read today!</p>
        <button onClick={onBrowseBooks}>Browse Books</button>
      </div>
      <div className="scroll-indicator" onClick={onBrowseBooks}>
        <i className="fa-solid fa-chevron-down"></i>
      </div>
    </div>
  );
};

export default Header;