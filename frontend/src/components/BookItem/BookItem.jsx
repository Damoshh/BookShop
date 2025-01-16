// BookItem.jsx
import React, { useContext } from 'react';
import './BookItem.css';
import { StoreContext } from '../../context/StoreContext';

const BookItem = ({ _id, name, price, description, image, author, category, isLoggedIn, setShowLogin, setInitialState }) => {
    const { cartItems, addToCart, removeFromCart, toggleWishlistItem, isInWishlist } = useContext(StoreContext);

    const handleAddToCart = (e) => {
        e.stopPropagation();
        if (!isLoggedIn) {
            alert('Please login first to add items to cart');
            setInitialState('Login');
            setShowLogin(true);
            return;
        }
        addToCart(_id);
    };

    const handleRemoveFromCart = (e) => {
        e.stopPropagation();
        if (!isLoggedIn) {
            alert('Please login first to modify cart');
            setInitialState('Login');
            setShowLogin(true);
            return;
        }
        removeFromCart(_id);
    };

    const handleWishlistToggle = (e) => {
        e.stopPropagation();
        if (!isLoggedIn) {
            alert('Please login first to add items to wishlist');
            setInitialState('Login');
            setShowLogin(true);
            return;
        }
        toggleWishlistItem(_id);
    };

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
                        e.target.onerror = null;
                        e.target.src = fallbackImage;
                    }}
                />
                <div className="book-item-actions">
                    <i 
                        className={`fa-${isInWishlist(_id) ? 'solid' : 'regular'} fa-star wishlist-icon`}
                        onClick={handleWishlistToggle}
                        title={isLoggedIn ? "Toggle wishlist" : "Login to add to wishlist"}
                    />
                    {!cartItems[_id] ? (
                        <i 
                            className="fa-solid fa-circle-plus"
                            onClick={handleAddToCart}
                            title={isLoggedIn ? "Add to cart" : "Login to add to cart"}
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
            </div>
            <div className="book-item-info">
                <h3 className="book-item-name">{name}</h3>
                {author && <p className="book-item-author">{author}</p>}
                {category && <p className="book-item-category">{category}</p>}
                <p className="book-item-desc">{description}</p>
                <p className="book-item-price">RM {typeof price === 'number' ? price.toFixed(2) : '0.00'}</p>
            </div>
        </div>
    );
};

export default BookItem;