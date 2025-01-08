import React, { useContext } from 'react';
import { Link } from "react-router-dom";
import "./Navbar.css";
import { StoreContext } from '../../context/StoreContext';

export const Navbar = ({theme, setTheme, setShowLogin}) => {
  const { getCartItemCount } = useContext(StoreContext);
  const cartItemCount = getCartItemCount();

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
        
        <button onClick={() => setShowLogin(true)}>Sign In</button>
      </div>
    </div>
  );
};

export default Navbar;