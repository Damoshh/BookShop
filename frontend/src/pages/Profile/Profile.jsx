import React, { useState, useEffect } from 'react';
import './Profile.css';

const Profile = ({ userEmail }) => {
    const [activeTab, setActiveTab] = useState('profile');
    const [userData, setUserData] = useState({
        name: '',
        email: userEmail,
        phone: '',
        address: '',
        password: ''
    });

    useEffect(() => {
        // Fetch user data when component mounts
        fetchUserData();
    }, [userEmail]);

    const fetchUserData = async () => {
        try {
            const response = await fetch('http://localhost:8000/api/users/profile', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: userEmail })
            });
            if (response.ok) {
                const data = await response.json();
                setUserData(data);
            }
        } catch (error) {
            console.error('Error fetching user data:', error);
        }
    };

    const handleUpdateProfile = async () => {
        try {
            const response = await fetch('http://localhost:8000/api/users/update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData)
            });
            if (response.ok) {
                alert('Profile updated successfully!');
            }
        } catch (error) {
            console.error('Error updating profile:', error);
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
                <button 
                    className={activeTab === 'wishlist' ? 'active' : ''} 
                    onClick={() => setActiveTab('wishlist')}
                >
                    Wishlist
                </button>
            </div>

            <div className="profile-content">
                {activeTab === 'profile' && (
                    <div className="profile-details">
                        <h2>Profile Details</h2>
                        <div className="form-group">
                            <label>Full Name</label>
                            <input
                                type="text"
                                value={userData.name}
                                onChange={(e) => setUserData({...userData, name: e.target.value})}
                            />
                        </div>
                        <div className="form-group">
                            <label>Email</label>
                            <input
                                type="email"
                                value={userData.email}
                                disabled
                            />
                        </div>
                        <div className="form-group">
                            <label>Phone Number</label>
                            <input
                                type="tel"
                                value={userData.phone}
                                onChange={(e) => setUserData({...userData, phone: e.target.value})}
                            />
                        </div>
                        <div className="form-group">
                            <label>Address</label>
                            <textarea
                                value={userData.address}
                                onChange={(e) => setUserData({...userData, address: e.target.value})}
                            />
                        </div>
                        <button className="update-btn" onClick={handleUpdateProfile}>
                            Update Profile
                        </button>
                    </div>
                )}

                {activeTab === 'orders' && (
                    <div className="order-history">
                        <h2>Order History</h2>
                        {/* Order history component will go here */}
                    </div>
                )}

                {activeTab === 'wishlist' && (
                    <div className="wishlist">
                        <h2>Wishlist</h2>
                        {/* Wishlist component will go here */}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Profile;