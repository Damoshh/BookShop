import React, { useState, useEffect } from 'react';
import './AdminProfile.css';

const AdminProfile = () => {
    const [profile, setProfile] = useState({
        fullName: '',
        email: '',
        phoneNumber: '',
        address: ''
    });
    const [error, setError] = useState('');

    useEffect(() => {
        console.log('AdminProfile mounted');
        console.log('Local Storage Contents:', {
            token: localStorage.getItem('sessionToken'),
            email: localStorage.getItem('userEmail'),
            role: localStorage.getItem('userRole')
        });
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            const token = localStorage.getItem('sessionToken');
            const email = localStorage.getItem('userEmail');
            console.log('Fetching profile with:', { token, email });  
    
            if (!email || !token) {
                console.log('Missing auth info:', { email, token });
                setError('Authentication information missing');
                return;
            }
    
            console.log('Making fetch request to profile endpoint...');
            const response = await fetch('http://localhost:8000/api/admin/profile', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'x-user-email': email,
                    'Content-Type': 'application/json'
                }
            });
            
            console.log('Response status:', response.status);
            
            if (response.status === 401) {
                console.log('Unauthorized access');
                setError('Unauthorized access - Please log in again');
                return;
            }
            
            if (!response.ok) {
                console.log('Response not ok:', response);
                throw new Error(`HTTP error! status: ${response.status}`);
            }
    
            const data = await response.json();
            console.log('Profile data received:', data);
            setProfile(data);
            setError('');
        } catch (err) {
            console.error('Error details:', {
                message: err.message,
                stack: err.stack
            });
            setError('Failed to load profile');
        }
    };

    return (
        <div className="admin-profile">
            <h2>Admin Profile</h2>
            
            <div className="profile-container">
                {error && (
                    <div className="error-message">
                        {error}
                        <button 
                            onClick={fetchProfile} 
                            className="retry-button"
                            style={{ marginLeft: '10px', padding: '5px 10px' }}
                        >
                            Retry
                        </button>
                    </div>
                )}
                
                {!error && (
                    <div className="profile-info">
                        <div className="info-group">
                            <span className="info-label">Full Name:</span>
                            <span className="info-value">{profile.fullName}</span>
                        </div>

                        <div className="info-group">
                            <span className="info-label">Email:</span>
                            <span className="info-value">{profile.email}</span>
                        </div>

                        <div className="info-group">
                            <span className="info-label">Phone:</span>
                            <span className="info-value">{profile.phoneNumber}</span>
                        </div>

                        <div className="info-group">
                            <span className="info-label">Address:</span>
                            <span className="info-value">{profile.address}</span>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminProfile;