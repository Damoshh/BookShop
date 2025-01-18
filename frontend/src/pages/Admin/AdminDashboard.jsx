import React from "react";
import { Routes, Route } from "react-router-dom";
import AdminLayout from "./AdminLayout";
import ManageBooks from "./ManageBook";
import Orders from "./Order";
import AdminProfile from './AdminProfile'; 

const AdminDashboard = () => {
  return (
    <AdminLayout>
      <Routes>
        <Route path="/" element={<AdminOverview />} />
        <Route path="/books" element={<ManageBooks />} />
        <Route path="/orders" element={<Orders />} />
        <Route path="/profile" element={<AdminProfile />} />
      </Routes>
    </AdminLayout>
  );
};

// Simple overview component
const AdminOverview = () => (
  <div>
    <h1>Admin Dashboard</h1>
    <div className="dashboard-stats">
      {/* Add your dashboard stats/widgets here */}
    </div>
  </div>
);

export default AdminDashboard;