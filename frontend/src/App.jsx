import React, { useEffect, useState } from "react";
import "./index.css";
import Navbar from "./components/Navbar/Navbar";
import Home from "./pages/Home/Home";
import Cart from "./pages/Cart/Cart";
import PlaceOrder from "./pages/PlaceOrder/PlaceOrder";
import AdminDashboard from "./pages/AdminDashboard/AdminDashboard";
import { BrowserRouter as Router, Route, Routes, useNavigate } from "react-router-dom";
import StoreContextProvider from "./context/StoreContext";
import Footer from "./components/Footer/Footer";
import LoginPopup from "./components/LoginPopup/LoginPopup";
import ProtectedRoute from "./components/ProtectedRoute";

const LoginPopupWrapper = (props) => {
  const navigate = useNavigate();
  return <LoginPopup {...props} navigate={navigate} />;
};

function App() {
  const current_theme = localStorage.getItem("current_theme");
  const [theme, setTheme] = useState(current_theme ? current_theme : "light");
  const [showLogin, setShowLogin] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userEmail, setUserEmail] = useState('');
  const [initialState, setInitialState] = useState('Login');

  useEffect(() => {
    localStorage.setItem("current_theme", theme);
    // Check initial login state
    const email = localStorage.getItem('userEmail');
    if (email) {
      setIsLoggedIn(true);
      setUserEmail(email);
    }
  }, [theme]);

  return (
    <StoreContextProvider>
      <Router>
        <div className={`App ${theme}`}>
          {showLogin ? (
            <LoginPopupWrapper 
              setShowLogin={setShowLogin} 
              setIsLoggedIn={setIsLoggedIn}
              setUserEmail={setUserEmail}
              initialState={initialState}
            />
          ) : null}
          
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
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/order" element={<PlaceOrder />} />
            <Route path="/admin" element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />
            <Route path="*" element={<h2>404 - Page Not Found</h2>} />
          </Routes>
          <Footer />
        </div>
      </Router>
    </StoreContextProvider>
  );
}

export default App;