import { createContext, useEffect, useState } from "react";
import { isAuthenticated } from '../utils/auth';

export const StoreContext = createContext(null);

const API_URL = 'http://localhost:8000'; // Update this to match your backend URL

const StoreContextProvider = (props) => {
    const [book_list, setBookList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [cartItems, setCartItems] = useState({});

    // Load cart items on mount and auth change
    useEffect(() => {
        const loadCartItems = () => {
            try {
                if (isAuthenticated()) {
                    const savedCart = localStorage.getItem('cart');
                    if (savedCart) {
                        setCartItems(JSON.parse(savedCart));
                    }
                } else {
                    setCartItems({});
                }
            } catch (error) {
                console.error('Error loading cart:', error);
                setCartItems({});
            }
        };

        loadCartItems();

        // Listen for login state changes
        window.addEventListener('loginStateChange', loadCartItems);
        window.addEventListener('storage', loadCartItems);
        
        return () => {
            window.removeEventListener('loginStateChange', loadCartItems);
            window.removeEventListener('storage', loadCartItems);
        };
    }, []);

    const fetchBooks = async () => {
        try {
            setLoading(true);
            setError(null);
            
            const response = await fetch(`${API_URL}/api/books`);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('Server response:', errorText);
                throw new Error(`Failed to fetch books: ${response.statusText}`);
            }

            const data = await response.json();
            console.log('Fetched books:', data); // Debug log
            
            if (!Array.isArray(data)) {
                throw new Error('Invalid data format received');
            }

            const transformedData = data.map(book => ({
                _id: book._id || String(Math.random()),
                title: book.title || book.name || 'Untitled',
                name: book.name || book.title || 'Untitled',
                author: book.author || 'Unknown Author',
                price: parseFloat(book.price) || 0,
                category: book.category || 'Uncategorized',
                description: book.description || 'No description available',
                coverImg: book.coverImg || book.image || '/placeholder-book.jpg',
                image: book.image || book.coverImg || '/placeholder-book.jpg',
            }));

            setBookList(transformedData);
        } catch (error) {
            console.error('Error fetching books:', error);
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchBooks();
    }, []);

    const getBooksByCategory = (category) => {
        if (!category || category === 'All') return book_list;
        return book_list.filter(book => book.category === category);
    };

    const addToCart = (itemId) => {
        if (!isAuthenticated()) return;
        setCartItems(prev => ({
            ...prev,
            [itemId]: (prev[itemId] || 0) + 1
        }));
    };

    const removeFromCart = (itemId) => {
        if (!isAuthenticated()) return;
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

    const getCartItemCount = () => {
        return Object.values(cartItems).reduce((total, quantity) => total + quantity, 0);
    };

    // Save cart to localStorage whenever it changes
    useEffect(() => {
        if (isAuthenticated() && Object.keys(cartItems).length > 0) {
            localStorage.setItem('cart', JSON.stringify(cartItems));
        }
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
        getCartItemCount
    };

    return (
        <StoreContext.Provider value={contextValue}>
            {props.children}
        </StoreContext.Provider>
    );
};

export default StoreContextProvider;