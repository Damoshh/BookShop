import React, { useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Cart.css';
import { StoreContext } from '../../context/StoreContext';
import { isAuthenticated } from '../../utils/auth';

const Cart = () => {
  const { 
    cartItems, 
    cartSubtotal, 
    cartDeliveryFee, 
    cartTotal,
    removeFromCart 
  } = useContext(StoreContext);
  
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/');
      return;
    }
  }, [navigate]);

  if (!isAuthenticated()) {
    return null;
  }

  const hasItems = cartItems.length > 0;

  return (
    <div className="cart">
      {!hasItems ? (
        <div className="cart-empty">
          <h2>Your cart is empty</h2>
          <button onClick={() => navigate('/')}>Continue Shopping</button>
        </div>
      ) : (
        <>
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
              <div key={item.bookId}>
                <div className="cart-items-title cart-items-item">
                  <img src={item.image} alt={item.name} />
                  <p>{item.name}</p>
                  <p>RM {item.price.toFixed(2)}</p>
                  <div>{item.quantity}</div>
                  <p>RM {(item.price * item.quantity).toFixed(2)}</p>
                  <p
                    className="cart-items-remove-icon"
                    onClick={() => removeFromCart(item.bookId)}
                  >
                    x
                  </p>
                </div>
                <hr />
              </div>
            ))}
          </div>
          <div className="cart-total">
            <h2>Cart Totals</h2>
            <div>
              <div className="cart-total-details">
                <p>Subtotal</p>
                <p>RM {cartSubtotal.toFixed(2)}</p>
              </div>
              <hr />
              <div className="cart-total-details">
                <p>Delivery Fee</p>
                <p>RM {cartDeliveryFee.toFixed(2)}</p>
              </div>
              <hr />
              <div className="cart-total-details">
                <b>Total</b>
                <b>RM {cartTotal.toFixed(2)}</b>
              </div>
            </div>
            <button onClick={() => navigate('/order')}>PROCEED TO CHECKOUT</button>
          </div>
        </>
      )}
    </div>
  );
};

export default Cart;