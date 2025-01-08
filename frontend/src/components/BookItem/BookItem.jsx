import React, { useContext } from 'react';
import './BookItem.css';
import { StoreContext } from '../../context/StoreContext';

const BookItem = ({ _id, name, price, description, image }) => {
    const { cartItems, addToCart, removeFromCart } = useContext(StoreContext);

    const handleAddToCart = (e) => {
        e.stopPropagation();
        addToCart(_id);
    };

    const handleRemoveFromCart = (e) => {
        e.stopPropagation();
        removeFromCart(_id);
    };

    return (
        <div className='book-item'>
            <div className='book-item-img-container'>
                <img 
                    className='book-item-image' 
                    src={image} 
                    alt={name} 
                    loading="lazy"
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
                <p className="book-item-desc">{description}</p>
                <p className="book-item-price">${price.toFixed(2)}</p>
            </div>
        </div>
    );
};

export default BookItem;