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

    useEffect(() => {
        fetchUserData();
    }, [userEmail]);

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
                    className={activeTab === 'orders' ? 'active' : ''} 
                    onClick={() => setActiveTab('orders')}
                >
                    Order History
                </button>
            </div>

            <div className="profile-content">
                {activeTab === 'profile' && (
                    <div className="profile-details">
                        <h2>Profile Details</h2>

                        <div className="form-group">
                            <label>Full Name</label>
                            <div className="readonly-field">{userData.name || 'Not provided'}</div>
                        </div>

                        <div className="form-group">
                            <label>Email</label>
                            <div className="readonly-field">{userData.email}</div>
                        </div>

                        <div className="form-group">
                            <label>Phone Number</label>
                            <div className="readonly-field">{userData.phone || 'Not provided'}</div>
                        </div>

                        <div className="address-section">
                            <h3>Address Information</h3>
                            
                            <div className="form-group">
                                <label>Street Address</label>
                                <div className="readonly-field">{userData.street || 'Not provided'}</div>
                            </div>

                            <div className="address-grid">
                                <div className="form-group">
                                    <label>City</label>
                                    <div className="readonly-field">{userData.city || 'Not provided'}</div>
                                </div>

                                <div className="form-group">
                                    <label>State</label>
                                    <div className="readonly-field">{userData.state || 'Not provided'}</div>
                                </div>
                            </div>

                            <div className="address-grid">
                                <div className="form-group">
                                    <label>ZIP Code</label>
                                    <div className="readonly-field">{userData.zipcode || 'Not provided'}</div>
                                </div>

                                <div className="form-group">
                                    <label>Country</label>
                                    <div className="readonly-field">{userData.country || 'Not provided'}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'orders' && (
                    <div className="order-history">
                        <h2>Order History</h2>
                        <div className="empty-orders">
                            <p>No orders yet</p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Profile;