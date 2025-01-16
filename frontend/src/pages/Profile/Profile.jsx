// Profile.jsx
import React, { useState, useEffect, useContext } from 'react';
import './Profile.css';
import { StoreContext } from '../../context/StoreContext';
import BookItem from '../../components/BookItem/BookItem';

const Profile = ({ userEmail, isLoggedIn, setShowLogin, setInitialState }) => {
    const { book_list, wishlistItems, toggleWishlistItem } = useContext(StoreContext);
    const [activeTab, setActiveTab] = useState('profile');
    const [userData, setUserData] = useState({
        name: '',
        email: userEmail,
        phone: '',
        address: '',
    });

    useEffect(() => {
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

    // Get wishlist items
    const wishlistBooks = book_list.filter(book => wishlistItems.has(book._id));

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
                    className={activeTab === 'wishlist' ? 'active' : 'wishlist-btn'} 
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
                        <div className="empty-orders">
                            <p>No orders yet</p>
                        </div>
                    </div>
                )}

                {activeTab === 'wishlist' && (
                    <div className="wishlist-section">
                        <h2 className="wishlist-title">My Wishlist</h2>
                        {wishlistBooks.length > 0 ? (
                            <div className="wishlist-books">
                                {wishlistBooks.map(book => (
                                    <div key={book._id} className="wishlist-book-item">
                                        <img 
                                            src={book.image} 
                                            alt={book.name}
                                            className="wishlist-book-image"
                                        />
                                        <div className="wishlist-book-info">
                                            <h3>{book.name}</h3>
                                            <p className="author">{book.author}</p>
                                            <p className="category">{book.category}</p>
                                            <p className="description">{book.description}</p>
                                            <p className="price">${book.price.toFixed(2)}</p>
                                        </div>
                                        <div className="wishlist-book-actions">
                                            <span 
                                                className="star-icon active"
                                                onClick={() => toggleWishlistItem(book._id)}
                                            >⭐</span>
                                            <button className="remove-btn">-</button>
                                            <span className="quantity">2</span>
                                            <button className="add-btn">+</button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="empty-wishlist">
                                <h3>Your wishlist is empty</h3>
                                <p>Browse our collection and start adding your favorite books!</p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Profile;