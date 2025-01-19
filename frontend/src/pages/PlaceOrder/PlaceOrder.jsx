import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './PlaceOrder.css';
import { StoreContext } from '../../context/StoreContext';

const PlaceOrder = () => {
    const navigate = useNavigate();
    const { cartItems, cartTotal, cartSubtotal, deliveryFee, clearCart } = useContext(StoreContext);
    const [isLoading, setIsLoading] = useState(false);
    const [orderError, setOrderError] = useState('');
    const [orderSuccess, setOrderSuccess] = useState(false);

    // Initialize form data
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: localStorage.getItem('userEmail') || '',
        street: '',
        city: '',
        state: '',
        zipcode: '',
        country: '',
        phone: ''
    });

    // Check authentication and fetch user data immediately
    useEffect(() => {
        const token = localStorage.getItem('sessionToken');
        if (!token) {
            navigate('/');
            return;
        }

        const fetchUserData = async () => {
            try {
                const userEmail = localStorage.getItem('userEmail');
                if (!userEmail) return;

                const response = await fetch(`http://localhost:8000/api/users/profile?email=${userEmail}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'x-user-email': userEmail
                    }
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch user data');
                }

                const userData = await response.json();
                
                // Split name into first and last name
                const nameParts = userData.name ? userData.name.split(' ') : ['', ''];
                const firstName = nameParts[0] || '';
                const lastName = nameParts.slice(1).join(' ') || '';

                // Update form with user data
                setFormData({
                    firstName: firstName,
                    lastName: lastName,
                    email: userData.email,
                    street: userData.street || '',
                    city: userData.city || '',
                    state: userData.state || '',
                    zipcode: userData.zipcode || '',
                    country: userData.country || '',
                    phone: userData.phone || ''
                });
            } catch (error) {
                console.error('Error fetching user data:', error);
                setOrderError('Error loading user data. Please refresh the page.');
            }
        };

        fetchUserData();
    }, [navigate]);

    // Redirect if cart is empty
    useEffect(() => {
        if (!cartItems || cartItems.length === 0) {
            navigate('/cart');
        }
    }, [cartItems, navigate]);

    // Handle order placement
    const handlePlaceOrder = async () => {
        // Validate if all required fields have data
        const requiredFields = ['firstName', 'lastName', 'email', 'street', 'city', 'state', 'zipcode', 'country', 'phone'];
        const missingFields = requiredFields.filter(field => !formData[field]);
        
        if (missingFields.length > 0) {
            setOrderError('Missing delivery information. Please complete your profile first.');
            return;
        }
        
        setIsLoading(true);
        setOrderError('');
        setOrderSuccess(false);

        try {
            const token = localStorage.getItem('sessionToken');
            if (!token) {
                throw new Error('Authentication required');
            }

            // Format delivery address
            const deliveryAddress = `${formData.firstName} ${formData.lastName}, ${formData.street}, ${formData.city}, ${formData.state} ${formData.zipcode}, ${formData.country}, Phone: ${formData.phone}`;

            // Prepare order data
            const orderData = {
                userId: localStorage.getItem('userId'),
                totalAmount: cartTotal,
                deliveryFee: deliveryFee,
                paymentMethod: 'COD',
                deliveryAddress: deliveryAddress,
                items: cartItems.map(item => ({
                    bookId: item.bookId,
                    quantity: item.quantity,
                    price: item.price
                }))
            };

            // Send order to backend
            const response = await fetch('http://localhost:8000/api/orders/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                    'x-user-email': localStorage.getItem('userEmail')
                },
                body: JSON.stringify(orderData)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to place order');
            }

            // Set success state
            setOrderSuccess(true);
            
            // Clear cart
            await clearCart();
            
            // Show success message and redirect to home
            setTimeout(() => {
                alert(
                    `Order Placed Successfully!\n\n` +
                    `Order ID: ${data.orderId}\n` +
                    `Total Amount: RM${cartTotal.toFixed(2)}\n\n` +
                    `Your order will be delivered to:\n` +
                    `${deliveryAddress}\n\n` +
                    `Payment of RM${cartTotal.toFixed(2)} will be collected upon delivery.`
                );
                navigate('/'); // Redirect to home page
            }, 1000);

        } catch (error) {
            console.error('Error placing order:', error);
            setOrderError(error.message || 'Error placing order. Please try again.');
            setOrderSuccess(false);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className='place-order'>
            <div className="place-order-left">
                <h2 className='title'>Delivery Information</h2>
                {orderError && (
                    <div className="error-message">
                        {orderError}
                    </div>
                )}
                {orderSuccess && (
                    <div className="success-message">
                        Order placed successfully! Redirecting to home page...
                    </div>
                )}
                <div className="multi-fields">
                    <div className="input-group">
                        <input 
                            type="text" 
                            name='firstName' 
                            value={formData.firstName}
                            placeholder='First name'
                            disabled
                        />
                        <span className="required">*</span>
                    </div>
                    <div className="input-group">
                        <input 
                            type="text" 
                            name='lastName' 
                            value={formData.lastName}
                            placeholder='Last name'
                            disabled
                        />
                        <span className="required">*</span>
                    </div>
                </div>
                <div className="input-group">
                    <input 
                        type="email" 
                        name='email' 
                        value={formData.email}
                        placeholder='Email address'
                        disabled
                    />
                    <span className="required">*</span>
                </div>
                <div className="input-group">
                    <input 
                        type="text" 
                        name='street' 
                        value={formData.street}
                        placeholder='Street address'
                        disabled
                    />
                    <span className="required">*</span>
                </div>
                <div className="multi-fields">
                    <div className="input-group">
                        <input 
                            type="text" 
                            name='city' 
                            value={formData.city}
                            placeholder='City'
                            disabled
                        />
                        <span className="required">*</span>
                    </div>
                    <div className="input-group">
                        <input 
                            type="text" 
                            name='state' 
                            value={formData.state}
                            placeholder='State'
                            disabled
                        />
                        <span className="required">*</span>
                    </div>
                </div>
                <div className="multi-fields">
                    <div className="input-group">
                        <input 
                            type="text" 
                            name='zipcode' 
                            value={formData.zipcode}
                            placeholder='Zip code'
                            disabled
                        />
                        <span className="required">*</span>
                    </div>
                    <div className="input-group">
                        <input 
                            type="text" 
                            name='country' 
                            value={formData.country}
                            placeholder='Country'
                            disabled
                        />
                        <span className="required">*</span>
                    </div>
                </div>
                <div className="input-group">
                    <input 
                        type="tel" 
                        name='phone' 
                        value={formData.phone}
                        placeholder='Phone number'
                        disabled
                    />
                    <span className="required">*</span>
                </div>
            </div>
            <div className="place-order-right">
                <div className="order-summary">
                    <h2>Order Summary</h2>
                    <div className="cart-items-summary">
                        {cartItems.map(item => (
                            <div key={item.bookId} className="cart-item-summary">
                                <span>{item.title} × {item.quantity}</span>
                                <span>RM {(item.price * item.quantity).toFixed(2)}</span>
                            </div>
                        ))}
                    </div>
                    <div className="cart-total">
                        <div className="cart-total-details">
                            <p>Subtotal</p>
                            <p>RM {cartSubtotal.toFixed(2)}</p>
                        </div>
                        <hr />
                        <div className="cart-total-details">
                            <p>Delivery Fee</p>
                            <p>RM {deliveryFee.toFixed(2)}</p>
                        </div>
                        <hr />
                        <div className="cart-total-details total">
                            <b>Total</b>
                            <b>RM {cartTotal.toFixed(2)}</b>
                        </div>
                    </div>
                </div>
                <div className="payment-options">
                    <h2>Payment Method</h2>
                    <div className="payment-option selected">
                        <input 
                            type="radio" 
                            id="cod" 
                            name="payment" 
                            value="cod" 
                            defaultChecked 
                        />
                        <label htmlFor="cod">
                            <div className="payment-label">
                                <strong>Cash on Delivery</strong>
                                <p>Pay with cash upon delivery</p>
                                <p className="cod-notice">
                                    You will pay RM {cartTotal.toFixed(2)} when your order arrives
                                </p>
                            </div>
                        </label>
                    </div>
                    <button 
                        onClick={handlePlaceOrder}
                        className={`place-order-btn ${isLoading ? 'loading' : ''}`}
                        disabled={isLoading || orderSuccess}
                    >
                        {isLoading ? 'Processing...' : orderSuccess ? 'Order Placed!' : 'PLACE ORDER'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PlaceOrder;