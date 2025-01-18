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
                
                // Use relative URL since we've configured the proxy
                const url = category && category !== 'All' 
                    ? `/api/books/category/${encodeURIComponent(category)}`
                    : `/api/books`;
        
                console.log('Fetching books from:', url);
        
                const response = await fetch(url, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json'
                    }
                });
                
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
        
                const data = await response.json();
                console.log('Received data:', data);
                
                if (!Array.isArray(data)) {
                    throw new Error('Invalid data format received from server');
                }
        
                const validatedBooks = data.map(book => ({
                    _id: book._id,
                    name: book.name,
                    author: book.author || 'Unknown Author',
                    category: book.category || 'Uncategorized',
                    description: book.description || '',
                    price: typeof book.price === 'number' ? book.price : 0,
                    image: book.image || '/placeholder-book.jpg'
                }));
        
                setDisplayedBooks(validatedBooks);
                setError(null);
            } catch (error) {
                console.error('Error fetching books:', error);
                setError(error.message);
                setDisplayedBooks([]);
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