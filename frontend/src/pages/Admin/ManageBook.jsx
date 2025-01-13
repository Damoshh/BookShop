import React, { useState, useContext } from 'react';
import { StoreContext } from '../../context/StoreContext';
import './ManageBook.css';

const ManageBooks = () => {
    const [newBook, setNewBook] = useState({
        title: '',
        author: '',
        price: '',
        category: '',
        description: '',
        coverImg: ''
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewBook(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8000/api/books', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
                },
                body: JSON.stringify(newBook)
            });

            if (response.ok) {
                setSuccess('Book added successfully!');
                setNewBook({
                    title: '',
                    author: '',
                    price: '',
                    category: '',
                    description: '',
                    coverImg: ''
                });
            } else {
                setError('Failed to add book');
            }
        } catch (err) {
            setError('Error adding book');
        }
    };

    return (
        <div className="manage-books">
            <h2>Manage Books</h2>
            
            {/* Add New Book Form */}
            <div className="add-book-form">
                <h3>Add New Book</h3>
                {error && <div className="error-message">{error}</div>}
                {success && <div className="success-message">{success}</div>}
                
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Title:</label>
                        <input
                            type="text"
                            name="title"
                            value={newBook.title}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>Author:</label>
                        <input
                            type="text"
                            name="author"
                            value={newBook.author}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>Price:</label>
                        <input
                            type="number"
                            name="price"
                            value={newBook.price}
                            onChange={handleInputChange}
                            required
                            step="0.01"
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>Category:</label>
                        <select
                            name="category"
                            value={newBook.category}
                            onChange={handleInputChange}
                            required
                        >
                            <option value="">Select Category</option>
                            <option value="Fiction">Fiction</option>
                            <option value="Non-Fiction">Non-Fiction</option>
                            <option value="Romance">Romance</option>
                            <option value="Mystery">Mystery</option>
                            <option value="Sci-Fi">Sci-Fi</option>
                        </select>
                    </div>
                    
                    <div className="form-group">
                        <label>Description:</label>
                        <textarea
                            name="description"
                            value={newBook.description}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>Cover Image URL:</label>
                        <input
                            type="text"
                            name="coverImg"
                            value={newBook.coverImg}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    
                    <button type="submit" className="submit-btn">Add Book</button>
                </form>
            </div>
        </div>
    );
};

export default ManageBooks;