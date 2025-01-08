import React, { useEffect, useState } from "react";
import "./index.css";
import Navbar from "./components/Navbar/Navbar";
import Home from "./pages/Home/Home";
import Cart from "./pages/Cart/Cart";
import PlaceOrder from "./pages/PlaceOrder/PlaceOrder";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import StoreContextProvider from "./context/StoreContext";
import Footer from "./components/Footer/Footer";
import LoginPopup from "./components/LoginPopup/LoginPopup";

function App() {
  const current_theme = localStorage.getItem("current_theme");
  const [theme, setTheme] = useState(current_theme ? current_theme : "light");

  useEffect(() => {
    localStorage.setItem("current_theme", theme);
  }, [theme]);

  const [showLogin,setShowLogin] = useState (false);

  return (
  <StoreContextProvider>
    {showLogin ? <LoginPopup setShowLogin = {setShowLogin} /> : <></>}
    <div className={`App ${theme}`}>
      <Router>
          <Navbar theme={theme} setTheme={setTheme} setShowLogin = {setShowLogin}/>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/order" element={<PlaceOrder />} />
            <Route path="*" element={<h2>404 - Page Not Found</h2>} />
          </Routes>
      </Router>
      <Footer/>
    </div>
    </StoreContextProvider>
  );
}

export default App;
