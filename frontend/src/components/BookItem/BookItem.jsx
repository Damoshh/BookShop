import React, { useContext } from 'react';
import './BookItem.css';
import { StoreContext } from '../../context/StoreContext';

const BookItem = ({ _id, name, price, description, image, author, category }) => {
    const { cartItems, addToCart, removeFromCart } = useContext(StoreContext);

    const handleAddToCart = (e) => {
        e.stopPropagation();
        addToCart(_id);
    };

    const handleRemoveFromCart = (e) => {
        e.stopPropagation();
        removeFromCart(_id);
    };

    // Debug log to check individual book data
    console.log('Rendering book:', { _id, name, price, image });

    // Fallback image in case the book image is missing
    const fallbackImage = '/placeholder-book.jpg';

    return (
        <div className='book-item'>
            <div className='book-item-img-container'>
                <img 
                    className='book-item-image' 
                    src={image || fallbackImage}
                    alt={name} 
                    loading="lazy"
                    onError={(e) => {
                        e.target.onerror = null; // Prevent infinite loop
                        e.target.src = fallbackImage;
                    }}
                />
                {!cartItems[_id] ? (
                    <i 
                        className="fa-solid fa-circle-plus" 
                        onClick={handleAddToCart}
                    />
                ) : (
                    <div className='book-item-counter'>
                        <i 
                            className="fa-solid fa-circle-minus"
                            onClick={handleRemoveFromCart}
                        />
                        <span>{cartItems[_id]}</span>
                        <i 
                            className="fa-solid fa-circle-plus"
                            onClick={handleAddToCart}
                        />
                    </div>
                )}
            </div>
            <div className="book-item-info">
                <h3 className="book-item-name">{name}</h3>
                {author && <p className="book-item-author">{author}</p>}
                {category && <p className="book-item-category">{category}</p>}
                <p className="book-item-desc">{description}</p>
                <p className="book-item-price">${typeof price === 'number' ? price.toFixed(2) : '0.00'}</p>
            </div>
        </div>
    );
};

export default BookItem;