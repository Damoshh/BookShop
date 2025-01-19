import React, { useState, useEffect } from 'react';
import './Order.css';

const Orders = () => {
    const [orders, setOrders] = useState([]);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState("");

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            const response = await fetch('http://localhost:8000/api/orders', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('sessionToken')}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to fetch orders');
            }
            
            const data = await response.json();
            setOrders(data);
            setLoading(false);
        } catch (err) {
            setError('Error loading orders');
            console.error('Error:', err);
            setLoading(false);
        }
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleString('en-US', {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    };

    const parseItems = (itemsString) => {
        if (!itemsString) return [];
        try {
            return JSON.parse(itemsString.replace(/'/g, '"').replace(/;/g, ','));
        } catch (err) {
            console.error('Error parsing items:', err);
            return [];
        }
    };

    const updateOrderStatus = async (orderId) => {
        try {
            const response = await fetch('http://localhost:8000/api/orders/admin/update-status', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('sessionToken')}`
                },
                body: JSON.stringify({
                    orderId,
                    status: 'Delivery'
                })
            });

            if (!response.ok) {
                throw new Error('Failed to update order status');
            }

            await fetchOrders();
            setSelectedOrder(null);
            alert("Order has been successfully marked as delivered!");
        } catch (err) {
            console.error('Error updating order:', err);
            alert('Failed to update order status');
        }
    };

    const OrderDetailsModal = ({ order, onClose }) => {
        if (!order) return null;

        const deliveryLines = order.deliveryAddress ? order.deliveryAddress.split(';') : [];
        const items = parseItems(order.items);
        const subtotal = parseFloat(order.totalAmount) || 0;
        const deliveryFee = parseFloat(order.deliveryFee) || 0;
        const total = subtotal + deliveryFee;

        return (
            <div className="modal-overlay" onClick={(e) => {
                if (e.target === e.currentTarget) onClose();
            }}>
                <div className="modal-content">
                    <div className="modal-header">
                        <h3>Order Details</h3>
                        <button className="modal-close" onClick={onClose}>
                            <i className="fas fa-times"></i>
                        </button>
                    </div>
                    
                    <div className="modal-body">
                        <div className="section-title-container">
                            <i className="fas fa-truck text-success"></i>
                            <h4 className="section-title">Delivery Information</h4>
                        </div>
                        <div className="delivery-info">
                            {deliveryLines.map((line, index) => (
                                <div key={index} className="delivery-line">
                                    {line.trim()}
                                </div>
                            ))}
                        </div>

                        <div className="section-title-container">
                            <i className="fas fa-shopping-cart text-success"></i>
                            <h4 className="section-title">Order Items</h4>
                        </div>
                        <table className="items-table">
                            <thead>
                                <tr>
                                    <th>Book ID</th>
                                    <th>Quantity</th>
                                    <th>Price (RM)</th>
                                    <th>Subtotal (RM)</th>
                                </tr>
                            </thead>
                            <tbody>
                                {items.map((item, index) => (
                                    <tr key={index}>
                                        <td>{item.bookId}</td>
                                        <td>{item.quantity}</td>
                                        <td>{parseFloat(item.price).toFixed(2)}</td>
                                        <td>{(item.quantity * parseFloat(item.price)).toFixed(2)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>

                        <div className="order-totals">
                            <div className="total-row">
                                <span>Subtotal:</span>
                                <span>RM {subtotal.toFixed(2)}</span>
                            </div>
                            <div className="total-row">
                                <span>Delivery Fee:</span>
                                <span>RM {deliveryFee.toFixed(2)}</span>
                            </div>
                            <div className="total-row final">
                                <span>Total Amount:</span>
                                <span>RM {total.toFixed(2)}</span>
                            </div>
                        </div>

                        <button 
                            className="delivery-btn"
                            onClick={() => updateOrderStatus(order.orderId)}
                        >
                            <i className="fas fa-truck mr-2"></i>
                            Mark as Delivery
                        </button>
                    </div>
                </div>
            </div>
        );
    };

    if (loading) {
        return (
            <div className="orders-loading">
                <i className="fas fa-spinner fa-spin"></i> Loading orders...
            </div>
        );
    }

    if (error) {
        return (
            <div className="orders-error">
                <i className="fas fa-exclamation-circle"></i> {error}
            </div>
        );
    }

    const pendingOrders = orders.filter(order => order.status === 'Pending');

    return (
        <div className="orders-page">
            <h2>
                <i className="fas fa-clipboard-list mr-2"></i>
                Orders Management
            </h2>
            
            <div className="orders-section">
                <div className="pending-count">
                    <i className="fas fa-clock mr-2"></i>
                    Pending Orders: {pendingOrders.length}
                </div>

                <table className="orders-table">
                    <thead>
                        <tr>
                            <th>Order ID</th>
                            <th>Customer Details</th>
                            <th>Date</th>
                            <th>Total Amount</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {pendingOrders.map((order) => {
                            const addressLines = order.deliveryAddress ? order.deliveryAddress.split(';') : [];
                            const customerName = addressLines[0] || 'N/A';
                            const phoneNumber = addressLines[5]?.replace('Phone:', '').trim() || 'N/A';

                            return (
                                <tr key={order.orderId}>
                                    <td>{order.orderId}</td>
                                    <td>
                                        <div className="customer-name">
                                            <i className="fas fa-user mr-2"></i>
                                            {customerName}
                                        </div>
                                        <div className="customer-phone">
                                            <i className="fas fa-phone mr-2"></i>
                                            {phoneNumber}
                                        </div>
                                    </td>
                                    <td>{formatDate(order.orderDate)}</td>
                                    <td>RM {(parseFloat(order.totalAmount) + parseFloat(order.deliveryFee)).toFixed(2)}</td>
                                    <td>
                                        <span className="status-pending">
                                            <i className="fas fa-clock mr-2"></i>
                                            Pending
                                        </span>
                                    </td>
                                    <td>
                                        <button 
                                            className="view-details-btn"
                                            onClick={() => setSelectedOrder(order)}
                                        >
                                            <i className="fas fa-eye mr-2"></i>
                                            View Details
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            {selectedOrder && (
                <OrderDetailsModal 
                    order={selectedOrder}
                    onClose={() => setSelectedOrder(null)}
                />
            )}
        </div>
    );
};

export default Orders;