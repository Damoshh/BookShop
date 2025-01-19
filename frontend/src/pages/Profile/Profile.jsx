import React, { useState, useEffect } from 'react';
import './Profile.css';

const Profile = ({ userEmail }) => {
    const [activeTab, setActiveTab] = useState('profile');
    const [userData, setUserData] = useState({
        name: '',
        email: userEmail,
        phone: '',
        street: '',
        city: '',
        state: '',
        zipcode: '',
        country: ''
    });
    const [orders, setOrders] = useState([]);
    const [hasDeliveryItems, setHasDeliveryItems] = useState(false);

    useEffect(() => {
        fetchUserData();
        fetchOrders();
    }, [userEmail]);

    useEffect(() => {
        // Check if there are any orders with "Delivery" status
        const deliveryExists = orders.some(order => order.status === 'Delivery');
        setHasDeliveryItems(deliveryExists);
    }, [orders]);

    const fetchUserData = async () => {
        try {
            const response = await fetch(`http://localhost:8000/api/users/profile?email=${userEmail}`);
            if (response.ok) {
                const data = await response.json();
                setUserData(data);
            } else {
                console.error('Failed to fetch user data');
            }
        } catch (error) {
            console.error('Error fetching user data:', error);
        }
    };

    const fetchOrders = async () => {
        try {
            const response = await fetch(`http://localhost:8000/api/orders/user/${localStorage.getItem('userId')}`);
            if (response.ok) {
                const data = await response.json();
                // Sort orders: Delivery first, then Pending, then Delivered
                const sortedOrders = data.sort((a, b) => {
                    const statusOrder = {
                        'Delivery': 0,
                        'Pending': 1,
                        'Delivered': 2
                    };
                    return statusOrder[a.status] - statusOrder[b.status] || 
                           new Date(b.orderDate) - new Date(a.orderDate);
                });
                setOrders(sortedOrders);
            }
        } catch (error) {
            console.error('Error fetching orders:', error);
        }
    };

    const handleReceived = async (orderId) => {
        try {
            const response = await fetch('http://localhost:8000/api/orders/user/update-status', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('sessionToken')}`
                },
                body: JSON.stringify({
                    orderId,
                    status: 'Delivered'
                })
            });
    
            if (!response.ok) {
                throw new Error('Failed to update order status');
            }
    
            // Refresh orders after successful update
            await fetchOrders();
            
            // Add success feedback
            alert("Order has been successfully marked as delivered!");
    
        } catch (error) {
            console.error('Error updating order status:', error);
            alert('Failed to update order status. Please try again.');
        }
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const parseItems = (itemsString) => {
        try {
            const cleanedString = itemsString
                .replace(/'/g, '"')
                .replace(/;/g, ',')
                .replace(/\[|\]/g, '');
            return JSON.parse(`[${cleanedString}]`);
        } catch (error) {
            console.error('Error parsing items:', error);
            return [];
        }
    };

    return (
        <div className="profile-container">
            <div className="profile-sidebar">
                <button 
                    className={activeTab === 'profile' ? 'active' : ''} 
                    onClick={() => setActiveTab('profile')}
                >
                    Profile Details
                </button>
                <button 
                    className={`order-history-btn ${activeTab === 'orders' ? 'active' : ''}`}
                    onClick={() => setActiveTab('orders')}
                >
                    Order History
                    {hasDeliveryItems && <span className="notification-dot"></span>}
                </button>
            </div>

            <div className="profile-content">
                {activeTab === 'profile' && (
                    <div className="profile-details">
                        <h2>Profile Details</h2>
                        <div className="form-group">
                            <label>Full Name</label>
                            <div className="readonly-field">{userData.name}</div>
                        </div>

                        <div className="form-group">
                            <label>Email</label>
                            <div className="readonly-field">{userData.email}</div>
                        </div>

                        <div className="form-group">
                            <label>Phone Number</label>
                            <div className="readonly-field">{userData.phone}</div>
                        </div>

                        <div className="address-section">
                            <h3>Address Information</h3>
                            
                            <div className="form-group">
                                <label>Street Address</label>
                                <div className="readonly-field">{userData.street}</div>
                            </div>

                            <div className="address-grid">
                                <div className="form-group">
                                    <label>City</label>
                                    <div className="readonly-field">{userData.city}</div>
                                </div>

                                <div className="form-group">
                                    <label>State</label>
                                    <div className="readonly-field">{userData.state}</div>
                                </div>
                            </div>

                            <div className="address-grid">
                                <div className="form-group">
                                    <label>ZIP Code</label>
                                    <div className="readonly-field">{userData.zipcode}</div>
                                </div>

                                <div className="form-group">
                                    <label>Country</label>
                                    <div className="readonly-field">{userData.country}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'orders' && (
                    <div className="order-history">
                        <h2>Order History</h2>
                        {orders.length === 0 ? (
                            <div className="empty-orders">
                                <p>No orders yet</p>
                            </div>
                        ) : (
                            <div className="orders-list">
                                {orders.map((order) => (
                                    <div key={order.orderId} className={`order-card ${order.status.toLowerCase()}`}>
                                        <div className="order-header">
                                            <div className="order-info">
                                                <h3>Order #{order.orderId}</h3>
                                                <p className="order-date">{formatDate(order.orderDate)}</p>
                                            </div>
                                            <div className={`order-status ${order.status.toLowerCase()}`}>
                                                {order.status}
                                                {order.status === 'Delivery' && (
                                                    <button 
                                                        onClick={() => handleReceived(order.orderId)}
                                                        className="received-btn"
                                                    >
                                                        Mark as Received
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                        <div className="order-items">
                                            {parseItems(order.items).map((item, index) => (
                                                <div key={index} className="order-item">
                                                    <p>Book ID: {item.bookId}</p>
                                                    <p>Quantity: {item.quantity}</p>
                                                    <p>Price: RM {item.price.toFixed(2)}</p>
                                                </div>
                                            ))}
                                        </div>
                                        <div className="order-summary">
                                            <p>Total Amount: RM {order.totalAmount.toFixed(2)}</p>
                                            <p>Delivery Fee: RM {order.deliveryFee.toFixed(2)}</p>
                                            <p>Total: RM {(parseFloat(order.totalAmount) + parseFloat(order.deliveryFee)).toFixed(2)}</p>
                                        </div>
                                        <div className="delivery-info">
                                            <h4>Delivery Address:</h4>
                                            <p>{order.deliveryAddress.replace(/;/g, ', ')}</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Profile;