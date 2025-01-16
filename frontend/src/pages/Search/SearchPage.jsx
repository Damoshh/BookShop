import React, { useContext, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { StoreContext } from '../../context/StoreContext';
import BookItem from '../../components/BookItem/BookItem';
import './Search.css';

const SearchPage = ({ isLoggedIn, setShowLogin }) => {
    const [searchParams] = useSearchParams();
    const { book_list } = useContext(StoreContext);
    const [filteredBooks, setFilteredBooks] = useState([]);
    
    useEffect(() => {
        const query = searchParams.get('q')?.toLowerCase() || '';
        
        if (query) {
            const filtered = book_list.filter(book => 
                book.title?.toLowerCase().includes(query) ||
                book.author?.toLowerCase().includes(query) ||
                book.category?.toLowerCase().includes(query)
            );
            setFilteredBooks(filtered);
        } else {
            setFilteredBooks(book_list);
        }
    }, [searchParams, book_list]);

    return (
        <div className="search-page">
            <h2 className="search-title">
                {searchParams.get('q') 
                    ? `Search results for "${searchParams.get('q')}"` 
                    : 'All Books'}
            </h2>

            <div className="search-results">
                {filteredBooks.length === 0 ? (
                    <p className="no-results">No books found</p>
                ) : (
                    <div className="book-grid">
                        {filteredBooks.map(book => (
                            <BookItem
                                key={book._id}
                                _id={book._id}
                                name={book.title}
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
                )}
            </div>
        </div>
    );
};

export default SearchPage;