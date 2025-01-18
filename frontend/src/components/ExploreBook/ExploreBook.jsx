import React from 'react';
import './ExploreBook.css';
import { category_list } from './CategoryList';

const ExploreBook = ({category, setCategory}) => {
    return (
        <div className='explore-book' id='explore-book'>
            <h1>Find Your Perfect Book</h1>
            <p className='explore-book-text'>
                Discover amazing books across various genres. Whether you're looking for 
                thrilling adventures, romantic tales, or educational content, we have 
                something for everyone.
            </p>
            <div className='explore-book-list'>
                {category_list
                    .filter(item => item.category_name !== 'Wishlist')  // Filter out wishlist
                    .map((item, index) => {
                        return (
                            <div 
                                onClick={() => setCategory(prev => 
                                    prev === item.category_name ? 'All' : item.category_name
                                )} 
                                key={index} 
                                className="explore-book-list-item"
                                title={`Browse ${item.category_name} books`}
                            >
                                <img 
                                    className={category === item.category_name ? 'active' : ''} 
                                    src={item.category_image} 
                                    alt={`${item.category_name} category`}
                                    loading="lazy"
                                />
                                <p>{item.category_name}</p>
                            </div>
                        );
                    })}
            </div>
            <hr />
        </div>
    );
};

export default ExploreBook;