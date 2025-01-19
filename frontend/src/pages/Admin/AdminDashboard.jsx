import React, { useState, useEffect } from "react";
import { Routes, Route } from "react-router-dom";
import AdminLayout from "./AdminLayout";
import ManageBooks from "./ManageBook";
import Orders from "./Order";
import './AdminOverview.css';

const AdminDashboard = () => {
  return (
    <AdminLayout>
      <Routes>
        <Route path="/dashboard" element={<AdminOverview />} />
        <Route path="/books" element={<ManageBooks />} />
        <Route path="/orders" element={<Orders />} />
      </Routes>
    </AdminLayout>
  );
};

const AdminOverview = () => {
  const [profile, setProfile] = useState({
    fullName: '',
    email: '',
    phoneNumber: '',
    address: ''
  });
  
  const [stats, setStats] = useState({
    totalBooks: 0,
    activeOrders: 0,
    totalUsers: 0,
    totalSales: 0
  });
  
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchProfile();
    fetchDashboardStats();
  }, []);

  const fetchProfile = async () => {
    try {
      const token = localStorage.getItem('sessionToken');
      const email = localStorage.getItem('userEmail');

      if (!token || !email) {
        throw new Error('Authentication information missing');
      }

      const response = await fetch('http://localhost:8000/api/admin/profile', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'x-user-email': email,
          'Content-Type': 'application/json'
        }
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setProfile(data);
      setError('');
    } catch (err) {
      setError('Failed to load profile: ' + err.message);
      console.error('Profile fetch error:', err);
    }
  };

  const fetchDashboardStats = async () => {
    try {
      const token = localStorage.getItem('sessionToken');
      if (!token) {
        throw new Error('Authentication token missing');
      }

      const response = await fetch('http://localhost:8000/api/admin/dashboard', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setStats(data);
    } catch (err) {
      setError('Failed to load statistics: ' + err.message);
      console.error('Statistics fetch error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return <div className="loading-state">Loading dashboard...</div>;
  }

  return (
    <div className="admin-overview">
      <h1 className="dashboard-title">Admin Dashboard</h1>
      
      <div className="dashboard-section">
        <h2 className="section-header">Admin Profile</h2>
        
        <div className="profile-info">
          <div>
            <label>Full Name</label>
            <p>{profile.fullName}</p>
          </div>
          
          <div>
            <label>Email</label>
            <p>{profile.email}</p>
          </div>
          
          <div>
            <label>Phone</label>
            <p>{profile.phoneNumber}</p>
          </div>
          
          <div>
            <label>Address</label>
            <p>{profile.address}</p>
          </div>
        </div>
      </div>

      <div className="dashboard-section">
        <h2 className="section-header">Dashboard Statistics</h2>
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-number">{stats.totalBooks}</div>
            <div className="stat-text">Total Books</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">{stats.activeOrders}</div>
            <div className="stat-text">Active Orders</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">{stats.totalUsers}</div>
            <div className="stat-text">Total Users</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">RM {stats.totalSales.toFixed(2)}</div>
            <div className="stat-text">Total Sales</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;