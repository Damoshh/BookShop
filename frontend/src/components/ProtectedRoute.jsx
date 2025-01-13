import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  const isAdmin = localStorage.getItem('isAdmin') === 'true';
  const token = localStorage.getItem('sessionToken');
  
  if (!isAdmin || !token) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;