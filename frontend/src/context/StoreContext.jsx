import { createContext, useEffect, useState } from "react";

export const StoreContext = createContext(null);

const StoreContextProvider = (props) => {
    const [book_list, setBookList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [cartItems, setCartItems] = useState(() => {
        const savedCart = localStorage.getItem('cart');
        return savedCart ? JSON.parse(savedCart) : {};
    });

    useEffect(() => {
        fetchBooks();
    }, []);

    const fetchBooks = async () => {
        try {
            setLoading(true);
            const response = await fetch('http://localhost:8000/api/books');
            if (!response.ok) {
                throw new Error('Failed to fetch books');
            }
            const data = await response.json();
            console.log('Fetched books:', data);
            setBookList(data);
        } catch (error) {
            console.error('Error fetching books:', error);
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    const getBooksByCategory = (category) => {
        if (!category || category === 'All') return book_list;
        return book_list.filter(book => book.category === category);
    };

    const addToCart = (itemId) => {
        setCartItems(prev => ({
            ...prev,
            [itemId]: (prev[itemId] || 0) + 1
        }));
    };

    const removeFromCart = (itemId) => {
        setCartItems(prev => {
            const updated = { ...prev };
            if (updated[itemId] === 1) {
                delete updated[itemId];
            } else {
                updated[itemId] = updated[itemId] - 1;
            }
            return updated;
        });
    };

    // Add this new function
    const getCartItemCount = () => {
        return Object.values(cartItems).reduce((total, quantity) => total + quantity, 0);
    };

    useEffect(() => {
        localStorage.setItem('cart', JSON.stringify(cartItems));
    }, [cartItems]);

    const contextValue = {
        book_list,
        loading,
        error,
        cartItems,
        addToCart,
        removeFromCart,
        getBooksByCategory,
        refreshBooks: fetchBooks,
        getCartItemCount  // Add this to the context value
    };

    return (
        <StoreContext.Provider value={contextValue}>
            {props.children}
        </StoreContext.Provider>
    );
};

export default StoreContextProvider;