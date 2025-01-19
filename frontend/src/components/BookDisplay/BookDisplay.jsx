import React, { useEffect, useState } from 'react';
import './BookDisplay.css';
import BookItem from '../BookItem/BookItem';

const BookDisplay = ({ category, isLoggedIn, setShowLogin, setInitialState }) => {
    const [displayedBooks, setDisplayedBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchBooksByCategory = async () => {
            try {
                setLoading(true);
                setError(null);
                
                const url = category && category !== 'All' 
                    ? `/api/books/category/${encodeURIComponent(category)}`
                    : `/api/books`;
        
                console.log('Fetching books from:', url);
        
                const response = await fetch(url);
                const data = await response.json();
                
                // Debug logs
                console.log('Raw response data:', data);
                
                if (!Array.isArray(data)) {
                    throw new Error('Invalid data format received from server');
                }
        
                const validatedBooks = data.map(book => {
                    console.log('Processing book:', book); 
                    return {
                        _id: book._id,
                        title: book.title || book.name, 
                        author: book.author || 'Unknown Author',
                        category: book.category || 'Uncategorized',
                        description: book.description || '',
                        price: typeof book.price === 'number' ? book.price : 0,
                        image: book.image || book.coverImg || '/placeholder-book.jpg'
                    };
                });
        
                console.log('Validated books:', validatedBooks);
                setDisplayedBooks(validatedBooks);
                
            } catch (error) {
                console.error('Error fetching books:', error);
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };
    
        fetchBooksByCategory();
    }, [category]);

    if (loading) {
        return (
            <div className="book-display">
                <div className="loading-message">Loading books...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="book-display">
                <div className="error-message">
                    Error loading books: {error}. Please try refreshing the page.
                </div>
            </div>
        );
    }

    if (!displayedBooks || displayedBooks.length === 0) {
        return (
            <div className="book-display">
                <div className="empty-display">
                    No books available in {category === 'All' ? 'any category' : `the ${category} category`}
                </div>
            </div>
        );
    }

    return (
        <div className="book-display">
            <div className="book-grid">
                {displayedBooks.map((book) => (
                    <BookItem
                        key={book._id}
                        _id={book._id}
                        title={book.title}
                        author={book.author}
                        category={book.category}
                        description={book.description}
                        price={book.price}
                        image={book.image}
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