import React, { useContext } from 'react';
import './BookItem.css';
import { StoreContext } from '../../context/StoreContext';

const BookItem = ({ 
    _id, 
    name, 
    price, 
    description, 
    image, 
    author, 
    category, 
    isLoggedIn, 
    setShowLogin, 
    setInitialState 
}) => {
    const { cartItems, addToCart, removeFromCart } = useContext(StoreContext);

    // Find cart item quantity
    const quantity = cartItems?.find(item => item.bookId === _id)?.quantity || 0;

    const handleAddToCart = async (e) => {
        e.stopPropagation();
        if (!isLoggedIn) {
            alert('Please login first to add items to cart');
            setInitialState('Login');
            setShowLogin(true);
            return;
        }
        try {
            await addToCart(_id);
        } catch (error) {
            console.error('Error adding to cart:', error);
            alert('Failed to add item to cart. Please try again.');
        }
    };

    const handleRemoveFromCart = async (e) => {
        e.stopPropagation();
        if (!isLoggedIn) {
            alert('Please login first to modify cart');
            setInitialState('Login');
            setShowLogin(true);
            return;
        }
        await removeFromCart(_id);
    };

    return (
        <div className='book-item'>
            <div className='book-item-img-container'>
                <img 
                    className='book-item-image'
                    src={image || '/placeholder-book.jpg'}
                    alt={name}
                    loading="lazy"
                    onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = '/placeholder-book.jpg';
                    }}
                />
                <div className="book-item-actions">
                    {quantity === 0 ? (
                        <i 
                            className="fa-solid fa-circle-plus"
                            onClick={handleAddToCart}
                            title={isLoggedIn ? "Add to cart" : "Login to add to cart"}
                        />
                    ) : (
                        <div className="book-item-counter">
                            <i 
                                className="fa-solid fa-circle-minus"
                                onClick={handleRemoveFromCart}
                            />
                            <span>{quantity}</span>
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
                <p className="book-item-price">
                    RM {typeof price === 'number' ? price.toFixed(2) : '0.00'}
                </p>
            </div>
        </div>
    );
};

export default BookItem;