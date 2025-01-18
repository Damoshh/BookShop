import { createContext, useEffect, useState } from "react";
import { isAuthenticated, getCurrentUser } from '../utils/auth';

export const StoreContext = createContext(null);

const StoreContextProvider = (props) => {
    const [cartItems, setCartItems] = useState([]);
    const [cartTotal, setCartTotal] = useState(0);
    const [cartTotalItems, setCartTotalItems] = useState(0);
    const [cartSubtotal, setCartSubtotal] = useState(0);
    const [deliveryFee, setDeliveryFee] = useState(0);

    // Save cart state with user ID
    const saveCartState = () => {
        const userId = getCurrentUser()?.id;
        if (!userId) return;

        const cartState = {
            items: cartItems,
            total: cartTotal,
            totalItems: cartTotalItems,
            timestamp: new Date().getTime()
        };
        localStorage.setItem(`cart_${userId}`, JSON.stringify(cartState));
    };

    // Effect to save cart whenever it changes
    useEffect(() => {
        if (isAuthenticated() && (cartItems.length > 0 || cartTotal > 0)) {
            saveCartState();
        }
    }, [cartItems, cartTotal, cartTotalItems]);

    // Load cart when component mounts or user logs in
    const loadUserCart = async () => {
        if (!isAuthenticated()) return;

        const userId = getCurrentUser()?.id;
        if (!userId) return;

        try {
            // First try to get cart from backend
            const response = await fetch(`/api/cart/${userId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${getCurrentUser()?.token}`
                }
            });

            if (response.ok) {
                const backendCart = await response.json();
                
                // Get saved local cart
                const savedCartJSON = localStorage.getItem(`cart_${userId}`);
                const savedCart = savedCartJSON ? JSON.parse(savedCartJSON) : null;

                // Use local cart if it's newer than backend data
                if (savedCart && savedCart.items && savedCart.items.length > 0) {
                    setCartItems(backendCart.items || []);
                    setCartSubtotal(backendCart.subtotal || 0);
                    setDeliveryFee(backendCart.deliveryFee || 0);
                    setCartTotal(backendCart.total || 0);
                    setCartTotalItems(backendCart.totalItems || 0);

                    // Sync local cart to backend
                    try {
                        await fetch(`/api/cart/${userId}/sync`, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'Authorization': `Bearer ${getCurrentUser()?.token}`
                            },
                            body: JSON.stringify(savedCart.items)
                        });
                    } catch (error) {
                        console.error('Error syncing cart to backend:', error);
                    }
                } else {
                    // Use backend cart if no local cart or local cart is empty
                    setCartItems(backendCart.items || []);
                    setCartTotal(backendCart.total || 0);
                    setCartTotalItems(backendCart.totalItems || 0);
                }
            }
        } catch (error) {
            console.error('Error loading cart:', error);
            // Try to load from local storage if backend fails
            const savedCartJSON = localStorage.getItem(`cart_${userId}`);
            if (savedCartJSON) {
                const savedCart = JSON.parse(savedCartJSON);
                setCartItems(savedCart.items || []);
                setCartTotal(savedCart.total || 0);
                setCartTotalItems(savedCart.totalItems || 0);
            }
        }
    };

    // Initialize cart on mount
    useEffect(() => {
        if (isAuthenticated()) {
            loadUserCart();
        }
    }, []);

    // Listen for login
    useEffect(() => {
        const handleLoginStateChange = () => {
            if (isAuthenticated()) {
                loadUserCart();
            }
        };

        window.addEventListener('loginStateChange', handleLoginStateChange);
        return () => window.removeEventListener('loginStateChange', handleLoginStateChange);
    }, []);

    // Listen for logout
    useEffect(() => {
        const handleLogoutEvent = () => {
            saveCartState(); // Save current cart state before clearing
            setCartItems([]);
            setCartTotal(0);
            setCartTotalItems(0);
        };

        window.addEventListener('logout', handleLogoutEvent);
        return () => window.removeEventListener('logout', handleLogoutEvent);
    }, [cartItems, cartTotal, cartTotalItems]);

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
                body: JSON.stringify({ userId, bookId })
            });
            
            if (response.ok) {
                const updatedCart = await response.json();
                setCartItems(updatedCart.items || []);
                setCartSubtotal(updatedCart.subtotal || 0);
                setDeliveryFee(updatedCart.deliveryFee || 0);
                setCartTotal(updatedCart.total || 0);
                setCartTotalItems(updatedCart.totalItems || 0);
            }
        } catch (error) {
            console.error('Error adding to cart:', error);
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
                setCartSubtotal(updatedCart.subtotal || 0);
                setDeliveryFee(updatedCart.deliveryFee || 0);
                setCartTotal(updatedCart.total || 0);
                setCartTotalItems(updatedCart.totalItems || 0);
            }
        } catch (error) {
            console.error('Error removing from cart:', error);
        }
    };

    const clearCart = async () => {
        const userId = getCurrentUser()?.id;
        if (userId) {
            localStorage.removeItem(`cart_${userId}`);
        }
        
        if (isAuthenticated()) {
            try {
                await fetch(`/api/cart/${userId}/clear`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${getCurrentUser()?.token}`
                    }
                });
            } catch (error) {
                console.error('Error clearing cart in backend:', error);
            }
        }
        
        setCartItems([]);
        setCartSubtotal(0);
        setDeliveryFee(0);
        setCartTotal(0);
        setCartTotalItems(0);
    };

    const value = {
        cartItems,
        cartSubtotal,
        deliveryFee,
        cartTotal,
        cartTotalItems,
        addToCart,
        removeFromCart,
        refreshCart: loadUserCart,
        clearCart
    };

    return (
        <StoreContext.Provider value={value}>
            {props.children}
        </StoreContext.Provider>
    );
};

export default StoreContextProvider;