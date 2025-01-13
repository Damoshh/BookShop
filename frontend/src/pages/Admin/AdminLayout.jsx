
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './AdminLayout.css';

const AdminLayout = ({ children }) => {
  const navigate = useNavigate();
  
  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    navigate('/');
  };

  return (
    <div className="admin-layout">
      {/* Admin Header */}
      <header className="admin-header">
        <div className="admin-header-left">
          <h1 className="admin-logo">Readify Admin</h1>
        </div>
        <div className="admin-header-right">
          <span className="admin-email">{localStorage.getItem('userEmail')}</span>
          <button onClick={handleLogout} className="admin-logout-btn">Logout</button>
        </div>
      </header>

      {/* Admin Content */}
      <div className="admin-content">
        <nav className="admin-sidebar">
          <Link to="/admin" className="admin-nav-link">Dashboard</Link>
          <Link to="/admin/books" className="admin-nav-link">Manage Books</Link>
          <Link to="/admin/orders" className="admin-nav-link">Orders</Link>
        </nav>
        <main className="admin-main">
          {children}
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;