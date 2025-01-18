import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import BookItem from '../../components/BookItem/BookItem';
import './Search.css';

const SearchPage = ({ isLoggedIn, setShowLogin }) => {
    const [searchParams] = useSearchParams();
    const [searchResults, setSearchResults] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    
    useEffect(() => {
        const fetchSearchResults = async () => {
            setIsLoading(true);
            const query = searchParams.get('q');
            
            try {
                const baseUrl = 'http://localhost:8000';
                const url = query 
                    ? `${baseUrl}/api/books/search?q=${encodeURIComponent(query)}`
                    : `${baseUrl}/api/books`;
                
                console.log('Fetching from URL:', url);
                const response = await fetch(url);
                
                if (response.ok) {
                    const data = await response.json();
                    setSearchResults(data);
                } else {
                    console.error('Server returned an error:', response.status);
                    setSearchResults([]);
                }
            } catch (error) {
                console.error('Error fetching search results:', error);
                setSearchResults([]);
            } finally {
                setIsLoading(false);
            }
        };
    
        fetchSearchResults();
    }, [searchParams]);

    const renderResults = () => {
        if (isLoading) {
            return <p className="loading">Loading...</p>;
        }

        if (searchResults.length === 0) {
            return <p className="no-results">No books found</p>;
        }

        return (
            <div className="book-grid">
                {searchResults.map(book => (
                    <BookItem
                        key={book._id}
                        _id={book._id}
                        title={book.title}
                        description={book.description}
                        price={book.price}
                        image={book.coverImg}
                        author={book.author}
                        category={book.category}
                        isLoggedIn={isLoggedIn}
                        setShowLogin={setShowLogin}
                    />
                ))}
            </div>
        );
    };

    return (
        <div className="search-page">
            <h2 className="search-title">
                {searchParams.get('q') 
                    ? `Search results for "${searchParams.get('q')}"` 
                    : 'All Books'}
            </h2>
            <div className="search-results">
                {renderResults()}
            </div>
        </div>
    );
};

export default SearchPage;