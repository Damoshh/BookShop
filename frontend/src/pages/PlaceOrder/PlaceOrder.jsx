import React, { useContext, useEffect, useState } from 'react'
import './PlaceOrder.css'
import { StoreContext } from '../../context/StoreContext'
import { useNavigate } from 'react-router-dom';

const PlaceOrder = () => {
    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        street: "",
        city: "",
        state: "",
        zipcode: "",
        country: "",
        phone: ""
    });

    const { calculateTotal, clearCart } = useContext(StoreContext);
    const navigate = useNavigate();

    const handleInputChange = (event) => {
        const { name, value } = event.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handlePlaceOrder = () => {
        // Validate form data
        const isFormValid = Object.values(formData).every(value => value.trim() !== "");
        
        if (!isFormValid) {
            alert("Please fill in all fields");
            return;
        }

        // Process order
        try {
            // You might want to send this to an API
            console.log("Order placed:", {
                ...formData,
                total: calculateTotal() + 5,
                orderDate: new Date().toISOString()
            });

            // Clear cart and redirect
            clearCart();
            navigate('/');
            alert("Order placed successfully!");
        } catch (error) {
            console.error("Error placing order:", error);
            alert("Error placing order. Please try again.");
        }
    };

    useEffect(() => {
        const total = calculateTotal();
        if (total === 0) {
            navigate('/');
        }
    }, [calculateTotal, navigate]);

    const total = calculateTotal();

    return (
        <div className='place-order'>
            <div className="place-order-left">
                <p className='title'>Delivery Information</p>
                <div className="multi-fields">
                    <input 
                        type="text" 
                        name='firstName' 
                        onChange={handleInputChange} 
                        value={formData.firstName} 
                        placeholder='First name'
                    />
                    <input 
                        type="text" 
                        name='lastName' 
                        onChange={handleInputChange} 
                        value={formData.lastName} 
                        placeholder='Last name'
                    />
                </div>
                <input 
                    type="email" 
                    name='email' 
                    onChange={handleInputChange} 
                    value={formData.email} 
                    placeholder='Email address'
                />
                <input 
                    type="text" 
                    name='street' 
                    onChange={handleInputChange} 
                    value={formData.street} 
                    placeholder='Street'
                />
                <div className="multi-fields">
                    <input 
                        type="text" 
                        name='city' 
                        onChange={handleInputChange} 
                        value={formData.city} 
                        placeholder='City'
                    />
                    <input 
                        type="text" 
                        name='state' 
                        onChange={handleInputChange} 
                        value={formData.state} 
                        placeholder='State'
                    />
                </div>
                <div className="multi-fields">
                    <input 
                        type="text" 
                        name='zipcode' 
                        onChange={handleInputChange} 
                        value={formData.zipcode} 
                        placeholder='Zip code'
                    />
                    <input 
                        type="text" 
                        name='country' 
                        onChange={handleInputChange} 
                        value={formData.country} 
                        placeholder='Country'
                    />
                </div>
                <input 
                    type="tel" 
                    name='phone' 
                    onChange={handleInputChange} 
                    value={formData.phone} 
                    placeholder='Phone'
                />
            </div>
            <div className="place-order-right">
                <div className="cart-total">
                    <h2>Cart Totals</h2>
                    <div>
                        <div className="cart-total-details">
                            <p>Subtotal</p>
                            <p>${total.toFixed(2)}</p>
                        </div>
                        <hr />
                        <div className="cart-total-details">
                            <p>Delivery Fee</p>
                            <p>${total === 0 ? '0.00' : '5.00'}</p>
                        </div>
                        <hr />
                        <div className="cart-total-details">
                            <b>Total</b>
                            <b>${total === 0 ? '0.00' : (total + 5).toFixed(2)}</b>
                        </div>
                    </div>
                </div>
                <div className="payment-options">
                    <h2>Select Payment Method</h2>
                    <div className="payment-option">
                        <input 
                            type="radio" 
                            id="cod" 
                            name="payment" 
                            value="cod" 
                            defaultChecked 
                        />
                        <label htmlFor="cod">COD (Cash On Delivery)</label>
                    </div>
                    <button 
                        onClick={handlePlaceOrder}
                        className="place-order-btn"
                    >
                        PLACE ORDER
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PlaceOrder;