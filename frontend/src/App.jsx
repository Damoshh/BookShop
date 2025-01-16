import React, { useEffect, useState } from "react";
import "./index.css";
import { BrowserRouter as Router, Route, Routes, useNavigate, useLocation, Navigate } from "react-router-dom";
import { isAuthenticated } from './utils/auth.js';

// Components
import Navbar from "./components/Navbar/Navbar";
import Footer from "./components/Footer/Footer";
import LoginPopup from "./components/LoginPopup/LoginPopup";
import ProtectedRoute from "./components/ProtectedRoute";

// Pages
import Home from "./pages/Home/Home";
import Cart from "./pages/Cart/Cart";
import Profile from "./pages/Profile/Profile";
import PlaceOrder from "./pages/PlaceOrder/PlaceOrder";
import AdminDashboard from "./pages/Admin/AdminDashboard";
import SearchPage from './pages/Search/SearchPage';

// Context
import StoreContextProvider from "./context/StoreContext";

const LoginPopupWrapper = (props) => {
  const navigate = useNavigate();
  return <LoginPopup {...props} navigate={navigate} />;
};

const LayoutWrapper = ({ 
  children, 
  showLogin, 
  setShowLogin, 
  setIsLoggedIn, 
  setUserEmail, 
  initialState, 
  theme, 
  setTheme, 
  isLoggedIn, 
  userEmail, 
  setInitialState 
}) => {
  const location = useLocation();
  const isAdminPage = location.pathname.startsWith('/admin');

  return (
    <>
      {showLogin && (
        <LoginPopupWrapper 
          setShowLogin={setShowLogin} 
          setIsLoggedIn={setIsLoggedIn}
          setUserEmail={setUserEmail}
          initialState={initialState}
          returnUrl={location.pathname}
        />
      )}
      
      {!isAdminPage && (
        <Navbar 
          theme={theme} 
          setTheme={setTheme} 
          setShowLogin={setShowLogin}
          isLoggedIn={isLoggedIn}
          setIsLoggedIn={setIsLoggedIn}
          userEmail={userEmail}
          setUserEmail={setUserEmail}
          setInitialState={setInitialState}
        />
      )}
      
      <main className={`min-h-screen ${theme}`}>
        {children}
      </main>
      
      {!isAdminPage && <Footer />}
    </>
  );
};

function App() {
  // Theme state
  const [theme, setTheme] = useState(() => 
    localStorage.getItem("current_theme") || "light"
  );

  // Auth states
  const [showLogin, setShowLogin] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(() => isAuthenticated());
  const [userEmail, setUserEmail] = useState(() => 
    localStorage.getItem('userEmail') || ''
  );
  const [initialState, setInitialState] = useState('Login');

  // Persist theme changes
  useEffect(() => {
    localStorage.setItem("current_theme", theme);
  }, [theme]);

  // Check authentication status on mount and when auth-related storage changes
  useEffect(() => {
    const checkAuth = () => {
      const authenticated = isAuthenticated();
      const storedEmail = localStorage.getItem('userEmail');
      
      setIsLoggedIn(authenticated);
      setUserEmail(authenticated ? storedEmail : '');
      
      if (!authenticated) {
        setShowLogin(false);
      }
    };

    window.addEventListener('storage', checkAuth);
    return () => window.removeEventListener('storage', checkAuth);
  }, []);

  return (
    <StoreContextProvider>
      <Router>
        <LayoutWrapper
          showLogin={showLogin}
          setShowLogin={setShowLogin}
          setIsLoggedIn={setIsLoggedIn}
          setUserEmail={setUserEmail}
          initialState={initialState}
          theme={theme}
          setTheme={setTheme}
          isLoggedIn={isLoggedIn}
          userEmail={userEmail}
          setInitialState={setInitialState}
        >
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={
              <Home 
                isLoggedIn={isLoggedIn} 
                setShowLogin={setShowLogin}
              />
            } />
            <Route path="/search" element={
              <SearchPage 
                isLoggedIn={isLoggedIn} 
                setShowLogin={setShowLogin}
              />
            } />

            {/* Protected User Routes */}
            <Route path="/cart" element={
              <ProtectedRoute requiresAdmin={false}>
                <Cart />
              </ProtectedRoute>
            } />
            <Route path="/order" element={
              <ProtectedRoute requiresAdmin={false}>
                <PlaceOrder />
              </ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute requiresAdmin={false}>
                <Profile userEmail={userEmail} />
              </ProtectedRoute>
            } />
            <Route path="/profile/orders" element={
              <ProtectedRoute requiresAdmin={false}>
                <Profile userEmail={userEmail} activeTab="orders" />
              </ProtectedRoute>
            } />
            <Route path="/profile/wishlist" element={
              <ProtectedRoute requiresAdmin={false}>
                <Profile userEmail={userEmail} activeTab="wishlist" />
              </ProtectedRoute>
            } />

            {/* Protected Admin Routes */}
            <Route path="/admin/*" element={
              <ProtectedRoute requiresAdmin={true}>
                <AdminDashboard />
              </ProtectedRoute>
            } />

            {/* 404 Route */}
            <Route path="*" element={
              <div className="flex items-center justify-center min-h-screen">
                <h2 className="text-2xl font-bold text-gray-800">404 - Page Not Found</h2>
              </div>
            } />
          </Routes>
        </LayoutWrapper>
      </Router>
    </StoreContextProvider>
  );
}

export default App;