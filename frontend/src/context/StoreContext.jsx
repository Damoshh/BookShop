import { createContext, useEffect, useState } from "react";
import { book_list } from "./BookList";
export const StoreContext = createContext(null);

const StoreContextProvider = (props) => {

    const [book_list, setBookList] = useState([]);
    const [cartItems, setCartItems] = useState(() => {
        const savedCart = localStorage.getItem('cart');
        return savedCart ? JSON.parse(savedCart) : {};
    });

    const [cartTotal, setCartTotal] = useState(0);


    useEffect(() => {
        fetch('http://localhost:8000/api/books')
            .then(response => response.json())
            .then(data => {
                setBookList(data);
            })
            .catch(error => console.error('Error fetching books:', error));
    }, []);


    const addToCart = (itemId) => {
        const book = book_list.find(book => book._id === itemId);
        if (!book) return;
      
        setCartItems(prev => ({
            ...prev,
            [String(itemId)]: (prev[String(itemId)] || 0) + 1
        }));
    };

    const removeFromCart = (itemId) => {
        setCartItems(prev => {
            const updatedCart = { ...prev };
            if (updatedCart[String(itemId)] === 1) {
                delete updatedCart[String(itemId)];
            } else {
                updatedCart[String(itemId)] -= 1;
            }
            return updatedCart;
        });
    };

    const clearCart = () => {
        setCartItems({});
    };

    const calculateTotal = () => {
        return Object.entries(cartItems).reduce((total, [itemId, quantity]) => {
            const book = book_list.find(book => book._id === itemId);
            return total + (book?.price || 0) * quantity;
        }, 0);
    };

    // Get cart item count
    const getCartItemCount = () => {
        return Object.values(cartItems).reduce((total, quantity) => total + quantity, 0);
    };

    useEffect(() => {
        localStorage.setItem('cart', JSON.stringify(cartItems));
        const total = calculateTotal();
        setCartTotal(total);
    }, [cartItems]); // `book_list` is not needed here since it doesn't change
    

    // Filter books by category
    const getBooksByCategory = (category) => {
        if (category === 'All') return book_list;
        return book_list.filter(book => book.category === category);
    };

    const contextValue = {
        book_list,
        cartItems,
        cartTotal,
        setCartItems,
        addToCart,
        removeFromCart,
        clearCart,
        getCartItemCount,
        getBooksByCategory,
        calculateTotal
    };

    return (
        <StoreContext.Provider value={contextValue}>
            {props.children}
        </StoreContext.Provider>
    );
};

export default StoreContextProvider;
