import React, { useContext } from 'react';
import './BookDisplay.css';
import { StoreContext } from '../../context/StoreContext';
import BookItem from '../BookItem/BookItem';

const BookDisplay = ({ category }) => {
    const { book_list, getBooksByCategory } = useContext(StoreContext);
    
    const displayedBooks = category ? getBooksByCategory(category) : book_list;

    // Debug log to check data
    console.log('Books to display:', displayedBooks);

    return (
        <div className='book-display' id='book-display'>
            <h2>Top Book Seller</h2>
            <div className="book-display-list">
                {displayedBooks && displayedBooks.length > 0 ? (
                    displayedBooks.map((book) => (
                        <BookItem 
                            key={book._id}
                            _id={book._id}  
                            name={book.title || book.name} // Handle both title and name
                            description={book.description}
                            price={parseFloat(book.price)}
                            image={book.coverImg || book.image} // Handle both coverImg and image
                            author={book.author}
                            category={book.category}
                        />
                    ))
                ) : (
                    <div>No books available</div>
                )}
            </div>
        </div>
    );
};

export default BookDisplay;