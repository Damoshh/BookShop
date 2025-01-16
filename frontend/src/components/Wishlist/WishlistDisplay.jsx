// WishlistDisplay.jsx
import React, { useContext } from 'react';
import { StoreContext } from '../../context/StoreContext';
import BookItem from '../BookItem/BookItem';
import './WishlistDisplay.css';

const WishlistDisplay = ({ isLoggedIn, setShowLogin, setInitialState }) => {
    const { book_list, wishlistItems } = useContext(StoreContext);
    
    const wishlistBooks = book_list.filter(book => wishlistItems.has(book._id));

    if (wishlistBooks.length === 0) {
        return (
            <div className="empty-wishlist">
                <h3>Your wishlist is empty</h3>
                <p>Browse our collection and add items to your wishlist!</p>
            </div>
        );
    }

    return (
        <div className="wishlist-grid">
            {wishlistBooks.map(book => (
                <BookItem
                    key={book._id}
                    {...book}
                    isLoggedIn={isLoggedIn}
                    setShowLogin={setShowLogin}
                    setInitialState={setInitialState}
                />
            ))}
        </div>
    );
};

export default WishlistDisplay;