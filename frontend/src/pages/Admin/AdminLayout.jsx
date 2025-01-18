import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { handleLogout } from '../../utils/auth';
import './AdminLayout.css';

const AdminLayout = ({ children }) => {
    const navigate = useNavigate();
    const userEmail = localStorage.getItem('userEmail');

    const onLogout = () => {
        handleLogout(navigate);
    };

    return (
        <div className="admin-layout">
            <header className="admin-header">
                <div className="admin-header-left">
                    <h1 className="admin-logo">Readify Admin</h1>
                </div>
                <div className="admin-header-right">
                    <div className="admin-user">
                        <i className="fa-solid fa-user"></i>
                        <span>{userEmail}</span>
                    </div>
                    <button onClick={onLogout} className="admin-logout-btn">
                        Logout
                    </button>
                </div>
            </header>

            <div className="admin-content">
                <nav className="admin-sidebar">
                    <Link to="/admin" className="admin-nav-link">Dashboard</Link>
                    <Link to="/admin/profile" className="admin-nav-link">Profile</Link> 
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