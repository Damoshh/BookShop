// BookDisplay.jsx
import React, { useContext } from 'react';
import './BookDisplay.css';
import BookItem from '../BookItem/BookItem';
import { StoreContext } from '../../context/StoreContext';

const BookDisplay = ({ category, isLoggedIn, setShowLogin, setInitialState }) => {
    const { book_list, getBooksByCategory, wishlistItems } = useContext(StoreContext);

    // Get books based on category
    let displayedBooks = getBooksByCategory(category);

    // If category is "Wishlist", filter to show only wishlist items
    if (category === 'Wishlist') {
        displayedBooks = book_list.filter(book => wishlistItems.has(book._id));
    }

    if (displayedBooks.length === 0) {
        return (
            <div className="empty-display">
                {category === 'Wishlist' ? (
                    <h2>Your wishlist is empty. Start adding some books!</h2>
                ) : (
                    <h2>No books available in this category.</h2>
                )}
            </div>
        );
    }

    return (
        <div className="book-display">
            <div className="book-grid">
                {displayedBooks.map((book) => (
                    <BookItem
                        key={book._id}
                        {...book}
                        isLoggedIn={isLoggedIn}
                        setShowLogin={setShowLogin}
                        setInitialState={setInitialState}
                    />
                ))}
            </div>
        </div>
    );
};

export default BookDisplay;