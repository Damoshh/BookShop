import React, { useContext } from 'react';
import './BookDisplay.css';
import { StoreContext } from '../../context/StoreContext';
import BookItem from '../BookItem/BookItem';

const BookDisplay = ({ category, isLoggedIn, setShowLogin }) => {
    const { book_list, getBooksByCategory, loading, error } = useContext(StoreContext);
    
    const displayedBooks = category ? getBooksByCategory(category) : book_list;

    if (loading) {
        return (
            <div className='book-display'>
                <h2>Top Book Seller</h2>
                <div className="loading">Loading books...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className='book-display'>
                <h2>Top Book Seller</h2>
                <div className="error">Error loading books: {error}</div>
            </div>
        );
    }

    return (
        <div className='book-display' id='book-display'>
            <h2>Top Book Seller</h2>
            <div className="book-display-list">
                {displayedBooks && displayedBooks.length > 0 ? (
                    displayedBooks.map((book) => (
                        <BookItem 
                            key={book._id}
                            _id={book._id}  
                            name={book.title || book.name}
                            description={book.description}
                            price={parseFloat(book.price)}
                            image={book.coverImg || book.image}
                            author={book.author}
                            category={book.category}
                            isLoggedIn={isLoggedIn}
                            setShowLogin={setShowLogin}
                        />
                    ))
                ) : (
                    <div className="no-books">No books available in this category</div>
                )}
            </div>
        </div>
    );
};

export default BookDisplay;