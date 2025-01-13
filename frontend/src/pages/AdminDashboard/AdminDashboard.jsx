import React, { useState, useEffect } from 'react';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('books');
  const [orders, setOrders] = useState([]);
  const [books, setBooks] = useState([]);
  const [newBook, setNewBook] = useState({
    title: '',
    author: '',
    price: '',
    category: '',
    description: '',
    coverImg: ''
  });
  const [isLoading, setIsLoading] = useState(false);

  // Get admin token
  const getAuthHeaders = () => {
    const sessionToken = localStorage.getItem('sessionToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${sessionToken}`
    };
};

  // Fetch orders
  const fetchOrders = async () => {
    try {
      const response = await fetch('http://localhost:8000/api/admin/orders', {
        headers: getAuthHeaders()
      });
      if (response.ok) {
        const data = await response.json();
        setOrders(data);
      }
    } catch (error) {
      console.error('Error fetching orders:', error);
      alert('Failed to load orders');
    }
  };

  // Fetch books
  const fetchBooks = async () => {
    try {
      const response = await fetch('http://localhost:8000/api/books');
      if (response.ok) {
        const data = await response.json();
        setBooks(data);
      }
    } catch (error) {
      console.error('Error fetching books:', error);
      alert('Failed to load books');
    }
  };

  useEffect(() => {
    fetchOrders();
    fetchBooks();
  }, []);

  const handleAddBook = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const response = await fetch('http://localhost:8000/api/admin/books', {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(newBook)
      });
      
      if (response.ok) {
        setNewBook({
          title: '',
          author: '',
          price: '',
          category: '',
          description: '',
          coverImg: ''
        });
        await fetchBooks(); // Refresh book list
        alert('Book added successfully!');
      } else {
        const error = await response.json();
        throw new Error(error.message || 'Failed to add book');
      }
    } catch (error) {
      console.error('Error adding book:', error);
      alert(error.message || 'Failed to add book');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteBook = async (bookId) => {
    if (window.confirm('Are you sure you want to delete this book?')) {
        try {
            const response = await fetch(`http://localhost:8000/api/admin/books/${bookId}`, {
                method: 'DELETE',
                headers: getAuthHeaders()  // Use it here
            });
        
        if (response.ok) {
          await fetchBooks(); // Refresh book list
          alert('Book deleted successfully!');
        } else {
          const error = await response.json();
          throw new Error(error.message || 'Failed to delete book');
        }
        } catch (error) {
            console.error('Error deleting book:', error);
            alert('Failed to delete book');
        }
    }
};

  return (
    <div className="admin-dashboard">
      <div className="admin-header">
        <h1>Admin Dashboard</h1>
      </div>
      
      <div className="admin-tabs">
        <button 
          className={`tab-button ${activeTab === 'orders' ? 'active' : ''}`}
          onClick={() => setActiveTab('orders')}
        >
          Orders
        </button>
        <button 
          className={`tab-button ${activeTab === 'books' ? 'active' : ''}`}
          onClick={() => setActiveTab('books')}
        >
          Manage Books
        </button>
      </div>

      {/* Orders Tab */}
      {activeTab === 'orders' && (
        <div className="orders-section">
          <h2>Orders in Processing</h2>
          <div className="orders-list">
            {orders.map(order => (
              <div key={order._id} className="order-card">
                <div className="order-header">
                  <h3>Order #{order._id}</h3>
                  <span className={`status ${order.status.toLowerCase()}`}>
                    {order.status}
                  </span>
                </div>
                <div className="order-details">
                  <p><strong>Customer:</strong> {order.customerEmail}</p>
                  <p><strong>Total:</strong> ${order.total.toFixed(2)}</p>
                  <p><strong>Date:</strong> {new Date(order.date).toLocaleDateString()}</p>
                </div>
              </div>
            ))}
            {orders.length === 0 && (
              <p className="no-data">No orders in processing</p>
            )}
          </div>
        </div>
      )}

      {/* Books Tab */}
      {activeTab === 'books' && (
        <div className="books-section">
          <div className="add-book-form">
            <h2>Add New Book</h2>
            <form onSubmit={handleAddBook}>
              <input
                type="text"
                placeholder="Title"
                value={newBook.title}
                onChange={(e) => setNewBook({...newBook, title: e.target.value})}
                required
                disabled={isLoading}
              />
              <input
                type="text"
                placeholder="Author"
                value={newBook.author}
                onChange={(e) => setNewBook({...newBook, author: e.target.value})}
                required
                disabled={isLoading}
              />
              <input
                type="number"
                placeholder="Price"
                value={newBook.price}
                onChange={(e) => setNewBook({...newBook, price: e.target.value})}
                required
                disabled={isLoading}
                step="0.01"
                min="0"
              />
              <select
                value={newBook.category}
                onChange={(e) => setNewBook({...newBook, category: e.target.value})}
                required
                disabled={isLoading}
              >
                <option value="">Select Category</option>
                <option value="Fiction">Fiction</option>
                <option value="Non-Fiction">Non-Fiction</option>
                <option value="Science">Science</option>
                <option value="Technology">Technology</option>
                <option value="Business">Business</option>
              </select>
              <textarea
                placeholder="Description"
                value={newBook.description}
                onChange={(e) => setNewBook({...newBook, description: e.target.value})}
                required
                disabled={isLoading}
              />
              <input
                type="url"
                placeholder="Cover Image URL"
                value={newBook.coverImg}
                onChange={(e) => setNewBook({...newBook, coverImg: e.target.value})}
                required
                disabled={isLoading}
              />
              <button 
                type="submit" 
                disabled={isLoading}
                className={isLoading ? 'loading' : ''}
              >
                {isLoading ? 'Adding...' : 'Add Book'}
              </button>
            </form>
          </div>

          <div className="books-list">
            <h2>Current Books</h2>
            <div className="books-grid">
              {books.map(book => (
                <div key={book._id} className="book-card">
                  <img 
                    src={book.coverImg} 
                    alt={book.title} 
                    onError={(e) => {
                      e.target.src = '/placeholder-book.jpg'; // Add a placeholder image
                    }}
                  />
                  <div className="book-info">
                    <h3>{book.title}</h3>
                    <p className="author">{book.author}</p>
                    <p className="price">${Number(book.price).toFixed(2)}</p>
                    <p className="category">{book.category}</p>
                    <button 
                      onClick={() => handleDeleteBook(book._id)}
                      className="delete-btn"
                      disabled={isLoading}
                    >
                      {isLoading ? 'Deleting...' : 'Delete'}
                    </button>
                  </div>
                </div>
              ))}
              {books.length === 0 && (
                <p className="no-data">No books available</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;