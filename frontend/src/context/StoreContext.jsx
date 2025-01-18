import { createContext, useEffect, useState } from "react";
import { isAuthenticated, getCurrentUser } from '../utils/auth';

export const StoreContext = createContext(null);

const StoreContextProvider = (props) => {
    const [cartItems, setCartItems] = useState([]);
    const [cartTotal, setCartTotal] = useState(0);
    const [cartTotalItems, setCartTotalItems] = useState(0);

    useEffect(() => {
        if (isAuthenticated()) {
            fetchCart();
        } else {
            setCartItems([]);
            setCartTotal(0);
            setCartTotalItems(0);
        }
    }, []);

    const fetchCart = async () => {
        if (!isAuthenticated()) return;
        try {
            const userId = getCurrentUser()?.id;
            // Use relative URL with proxy
            const response = await fetch(`/api/cart/${userId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'Authorization': `Bearer ${getCurrentUser()?.token}`
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            setCartItems(data.items || []);
            setCartTotal(data.total || 0);
            setCartTotalItems(data.totalItems || 0);
        } catch (error) {
            console.error('Error fetching cart:', error);
            setCartItems([]);
            setCartTotal(0);
            setCartTotalItems(0);
        }
    };

    const addToCart = async (bookId) => {
        if (!isAuthenticated()) return;
        try {
            const userId = getCurrentUser()?.id;
            const response = await fetch(`/api/cart/add`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${getCurrentUser()?.token}`
                },
                body: JSON.stringify({ 
                    userId, 
                    bookId
                })
            });
            
            if (response.ok) {
                const updatedCart = await response.json();
                setCartItems(updatedCart.items || []);
                setCartTotal(updatedCart.total || 0);
                setCartTotalItems(updatedCart.totalItems || 0);
            } else {
                throw new Error('Failed to add item to cart');
            }
        } catch (error) {
            console.error('Error adding to cart:', error);
            alert('Failed to add item to cart. Please try again.');
        }
    };

    const removeFromCart = async (bookId) => {
        if (!isAuthenticated()) return;
        try {
            const userId = getCurrentUser()?.id;
            const response = await fetch(`/api/cart/remove`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${getCurrentUser()?.token}`
                },
                body: JSON.stringify({ userId, bookId })
            });
            
            if (response.ok) {
                const updatedCart = await response.json();
                setCartItems(updatedCart.items || []);
                setCartTotal(updatedCart.total || 0);
                setCartTotalItems(updatedCart.totalItems || 0);
            }
        } catch (error) {
            console.error('Error removing from cart:', error);
        }
    };

    const value = {
        cartItems,
        cartTotal,
        cartTotalItems,
        addToCart,
        removeFromCart,
        refreshCart: fetchCart
    };

    return (
        <StoreContext.Provider value={value}>
            {props.children}
        </StoreContext.Provider>
    );
};

export default StoreContextProvider;