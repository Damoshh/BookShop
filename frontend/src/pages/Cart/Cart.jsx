import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Cart.css';
import { StoreContext } from '../../context/StoreContext';
import { isAuthenticated } from '../../utils/auth';

const Cart = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const { 
    cartItems, 
    cartSubtotal, 
    deliveryFee, 
    cartTotal,
    removeFromCart,
    refreshCart 
  } = useContext(StoreContext);

  useEffect(() => {
    const loadCartData = async () => {
      if (isAuthenticated()) {
        try {
          await refreshCart();
          console.log('Cart Items:', cartItems); // Debug log
        } catch (error) {
          console.error('Error loading cart:', error);
          setError('Failed to load cart items');
        } finally {
          setIsLoading(false);
        }
      } else {
        setIsLoading(false);
      }
    };

    loadCartData();
  }, [refreshCart]);

  if (!isAuthenticated()) {
    navigate('/');
    return null;
  }

  if (error) {
    return (
      <div className="cart">
        <div className="cart-error">
          <h2>Error Loading Cart</h2>
          <p>{error}</p>
          <button onClick={() => navigate('/')}>Return to Home</button>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="cart">
        <div className="cart-loading">
          <div className="cart-loading-spinner"></div>
          <p>Loading your cart...</p>
        </div>
      </div>
    );
  }

  if (!cartItems || cartItems.length === 0) {
    return (
      <div className="cart">
        <div className="cart-empty">
          <h2>Your cart is empty</h2>
          <p>Looks like you haven't added any books to your cart yet.</p>
          <button onClick={() => navigate('/')}>Continue Shopping</button>
        </div>
      </div>
    );
  }

  const handleImageError = (e) => {
    console.log('Image failed to load, using fallback');
    e.target.onerror = null;
    e.target.src = "/images/book-placeholder.jpg";
  };

  return (
    <div className="cart">
      <div className="cart-items">
        <div className="cart-items-title">
          <p>Items</p>
          <p>Title</p>
          <p>Price</p>
          <p>Quantity</p>
          <p>Total</p>
          <p>Remove</p>
        </div>
        <br />
        <hr />
        {cartItems.map((item) => (
          <div key={item.bookId} className="cart-item-container">
            <div className="cart-items-title cart-items-item">
              <div className="cart-item-image">
                <img 
                  src={item.image || '/images/book-placeholder.jpg'}
                  alt={item.title || 'Book cover'}
                  onError={handleImageError}
                />
              </div>
              <div className="cart-item-details">
                <p className="cart-item-title">{item.title}</p>
                <p className="cart-item-author">{item.author}</p>
              </div>
              <p className="cart-item-price">
                RM {item.price ? item.price.toFixed(2) : '0.00'}
              </p>
              <div className="cart-item-quantity">
                <span>{item.quantity}</span>
              </div>
              <p className="cart-item-total">
                RM {((item.price || 0) * (item.quantity || 0)).toFixed(2)}
              </p>
              <button
                className="cart-items-remove-icon"
                onClick={() => removeFromCart(item.bookId)}
                aria-label="Remove item"
              >
                ×
              </button>
            </div>
            <hr />
          </div>
        ))}
      </div>

      <div className="cart-total">
        <h2>Cart Totals</h2>
        <div className="cart-total-content">
          <div className="cart-total-details">
            <p>Subtotal</p>
            <p>RM {cartSubtotal.toFixed(2)}</p>
          </div>
          <hr />
          <div className="cart-total-details">
            <p>Delivery Fee</p>
            <p>RM {deliveryFee.toFixed(2)}</p>
            {deliveryFee === 0 && (
              <p className="delivery-notice">Free Delivery for orders above RM 100!</p>
            )}
          </div>
          <hr />
          <div className="cart-total-details">
            <b>Total</b>
            <b>RM {cartTotal.toFixed(2)}</b>
          </div>
          {cartTotal < 100 && (
            <p className="free-shipping-notice">
              Add RM {(100 - cartTotal).toFixed(2)} more to get FREE delivery!
            </p>
          )}
        </div>
        <button 
          onClick={() => navigate('/order')}
          className="checkout-button"
        >
          PROCEED TO CHECKOUT
        </button>
      </div>
    </div>
  );
};

export default Cart;