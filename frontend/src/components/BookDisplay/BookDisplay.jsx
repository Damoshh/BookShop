import React, { useContext } from 'react'
import './BookDisplay.css'
import { StoreContext } from '../../context/StoreContext'
import BookItem from '../BookItem/BookItem'

const BookDisplay = ({category}) => {
    const { book_list, getBooksByCategory } = useContext(StoreContext)
    
    
    const displayedBooks = category ? getBooksByCategory(category) : book_list;

    return (
        <div className='book-display' id='book-display'>
            <h2>Top Book Seller</h2>
            <div className="book-display-list">
                {displayedBooks.map((item) => {
                    return (
                        <BookItem 
                            key={item._id}
                            _id={item._id}  
                            name={item.name}
                            description={item.description}
                            price={item.price}
                            image={item.image}
                        /> 
                    )
                })}
            </div>
        </div>
    )
}

export default BookDisplay