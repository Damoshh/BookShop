import React, { useContext } from 'react';
import './BookItem.css';
import { StoreContext } from '../../context/StoreContext';

const BookItem = ({ 
    _id, 
    title,
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
        <div className='single-book-item'>
            <div className='single-book-img-container'>
                <img 
                    className='single-book-image'
                    src={image || '/placeholder-book.jpg'}
                    alt={title}
                    loading="lazy"
                    onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = '/placeholder-book.jpg';
                    }}
                />
                <div className="single-book-actions">
                    {quantity === 0 ? (
                        <i 
                            className="fa-solid fa-circle-plus single-book-add"
                            onClick={handleAddToCart}
                            title={isLoggedIn ? "Add to cart" : "Login to add to cart"}
                        />
                    ) : (
                        <div className="single-book-counter">
                            <i 
                                className="fa-solid fa-circle-minus single-book-remove"
                                onClick={handleRemoveFromCart}
                            />
                            <span>{quantity}</span>
                            <i 
                                className="fa-solid fa-circle-plus single-book-add"
                                onClick={handleAddToCart}
                            />
                        </div>
                    )}
                </div>
            </div>
            <div className="single-book-info">
    <h3 className="single-book-title">
        {title || 'Untitled Book'}
    </h3>
    {author && <p className="single-book-author">{author}</p>}
    {category && <p className="single-book-category">{category}</p>}
    <p className="single-book-description">{description || 'No description available'}</p>
    <p className="single-book-price">
        RM {typeof price === 'number' ? price.toFixed(2) : '0.00'}
    </p>
</div>
        </div>
    );
};

export default BookItem;