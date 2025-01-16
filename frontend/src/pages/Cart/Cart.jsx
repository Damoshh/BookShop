import React, { useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Cart.css';
import { StoreContext } from '../../context/StoreContext';
import { isAuthenticated } from '../../utils/auth';

const Cart = () => {
  const { cartItems, book_list, removeFromCart } = useContext(StoreContext);
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/');
      return;
    }
  }, [navigate]);

  const calculateTotal = () => {
    return book_list.reduce((total, item) => {
      if (cartItems[item._id]) {
        return total + (item.price * cartItems[item._id]);
      }
      return total;
    }, 0);
  };

  if (!isAuthenticated()) {
    return null;
  }

  const hasItems = Object.keys(cartItems).length > 0;

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
            {book_list.map((item) => {
              if (cartItems[item._id] > 0) {
                return (
                  <div key={item._id}>
                    <div className="cart-items-title cart-items-item">
                      <img src={item.image} alt={item.name} />
                      <p>{item.name}</p>
                      <p>${item.price.toFixed(2)}</p>
                      <div>{cartItems[item._id]}</div>
                      <p>${(item.price * cartItems[item._id]).toFixed(2)}</p>
                      <p
                        className="cart-items-remove-icon"
                        onClick={() => removeFromCart(item._id)}
                      >
                        x
                      </p>
                    </div>
                    <hr />
                  </div>
                );
              }
              return null;
            })}
          </div>
          <div className="cart-total">
            <h2>Cart Totals</h2>
            <div>
              <div className="cart-total-details">
                <p>Subtotal</p>
                <p>${calculateTotal().toFixed(2)}</p>
              </div>
              <hr />
              <div className="cart-total-details">
                <p>Delivery Fee</p>
                <p>${calculateTotal() === 0 ? '0.00' : '5.00'}</p>
              </div>
              <hr />
              <div className="cart-total-details">
                <b>Total</b>
                <b>${calculateTotal() === 0 ? '0.00' : (calculateTotal() + 5).toFixed(2)}</b>
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