import React, { useState, useEffect } from 'react';
import { category_list } from '../../components/ExploreBook/CategoryList';
import './ManageBook.css';

const ManageBooks = () => {
    const [books, setBooks] = useState([]);
    const [editingBook, setEditingBook] = useState(null);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        fetchBooks();
    }, []);

    const fetchBooks = async () => {
        try {
            const response = await fetch('http://localhost:8000/api/books');
            if (response.ok) {
                const data = await response.json();
                setBooks(data);
            } else {
                throw new Error('Failed to fetch books');
            }
        } catch (err) {
            setError('Failed to fetch books');
            console.error('Error:', err);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setEditingBook(prev => ({
            ...prev,
            [name]: value,
            coverImg: prev.image, // Keep image path consistent
            image: prev.image // Keep image path consistent
        }));
    };

    const handleDelete = async (bookId) => {
        if (!window.confirm('Are you sure you want to delete this book?')) return;
        
        try {
            const response = await fetch(`http://localhost:8000/api/books/manage/${bookId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('sessionToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok) {
                setSuccess(data.message || 'Book deleted successfully!');
                fetchBooks();
            } else {
                throw new Error(data.error || 'Failed to delete book');
            }
        } catch (err) {
            setError(err.message || 'Error deleting book');
            console.error('Error:', err);
        }
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        try {
            // Ensure we have all required fields
            if (!editingBook.title || !editingBook.author || !editingBook.category || 
                !editingBook.price || !editingBook.description || !editingBook.image) {
                setError('All fields are required');
                return;
            }

            const response = await fetch(`http://localhost:8000/api/books/manage/${editingBook._id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('sessionToken')}`
                },
                body: JSON.stringify({
                    title: editingBook.title,
                    author: editingBook.author,
                    category: editingBook.category,
                    description: editingBook.description,
                    price: parseFloat(editingBook.price),
                    image: editingBook.image // Use the original image path
                })
            });

            const data = await response.json();

            if (response.ok) {
                setSuccess(data.message || 'Book updated successfully!');
                setEditingBook(null);
                fetchBooks();
            } else {
                throw new Error(data.error || 'Failed to update book');
            }
        } catch (err) {
            setError(err.message || 'Error updating book');
            console.error('Error:', err);
        }
    };

    return (
        <div className="manage-books">
            <h2>Manage Books</h2>
            
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}
            
            <div className="books-table">
                <table>
                    <thead>
                        <tr>
                            <th>Title</th>
                            <th>Author</th>
                            <th>Category</th>
                            <th>Price (RM)</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {books.map(book => (
                            <tr key={book._id}>
                                <td>{book.title}</td>
                                <td>{book.author}</td>
                                <td>{book.category}</td>
                                <td>{book.price.toFixed(2)}</td>
                                <td className="actions">
                                    <button 
                                        onClick={() => setEditingBook({...book})}
                                        className="action-btn edit-btn"
                                    >
                                        <i className="fa-solid fa-pen"></i>
                                    </button>
                                    <button 
                                        onClick={() => handleDelete(book._id)}
                                        className="action-btn delete-btn"
                                    >
                                        <i className="fa-solid fa-trash"></i>
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Edit Modal */}
            {editingBook && (
                <div className="modal-overlay">
                    <div className="edit-modal">
                        <h3>Edit Book</h3>
                        <form onSubmit={handleUpdate}>
                            <div className="form-group">
                                <label>Title:</label>
                                <input
                                    type="text"
                                    name="title"
                                    value={editingBook.title}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Author:</label>
                                <input
                                    type="text"
                                    name="author"
                                    value={editingBook.author}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Price:</label>
                                <input
                                    type="number"
                                    name="price"
                                    value={editingBook.price}
                                    onChange={handleInputChange}
                                    required
                                    step="0.01"
                                />
                            </div>
                            
                            <div className="form-group">
                                <label>Category:</label>
                                <select
                                    name="category"
                                    value={editingBook.category}
                                    onChange={handleInputChange}
                                    required
                                >
                                    {category_list.map(cat => (
                                        <option 
                                            key={cat.category_name} 
                                            value={cat.category_name}
                                            selected={cat.category_name === editingBook.category}
                                        >
                                            {cat.category_name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            
                            <div className="form-group">
                                <label>Description:</label>
                                <textarea
                                    name="description"
                                    value={editingBook.description}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>
                            
                            <div className="modal-actions">
                                <button type="submit" className="submit-btn">Save Changes</button>
                                <button 
                                    type="button" 
                                    className="cancel-btn"
                                    onClick={() => setEditingBook(null)}
                                >
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ManageBooks;