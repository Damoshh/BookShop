import React, { useContext } from 'react'
import './ExploreBook.css'
import { category_list } from './CategoryList'
import { StoreContext } from '../../context/StoreContext'

const ExploreBook = ({category, setCategory}) => {
    const { wishlistItems } = useContext(StoreContext);
    const wishlistCount = wishlistItems ? wishlistItems.size : 0;

    return (
        <div className='explore-book' id='explore-book'>
            <h1>mari cari</h1>
            <p className='explore-book-text'>sdfsfadsfaf</p>
            <div className='explore-book-list'>
                {category_list.map((item, index) => {
                    return (
                        <div 
                            onClick={() => setCategory(prev => prev === item.category_name ? 'All' : item.category_name)} 
                            key={index} 
                            className="explore-book-list-item"
                        >
                            {item.category_name === 'Wishlist' ? (
                                <div className={`wishlist-icon-container ${category === item.category_name ? 'active' : ''}`}>
                                    <item.icon className="wishlist-icon" />
                                    {wishlistCount > 0 && (
                                        <span className="wishlist-count">{wishlistCount}</span>
                                    )}
                                </div>
                            ) : (
                                <img 
                                    className={category === item.category_name ? 'active' : ''} 
                                    src={item.category_image} 
                                    alt={item.category_name}
                                />
                            )}
                            <p>{item.category_name}</p>
                        </div>
                    )
                })}
            </div>
            <hr />
        </div>
    )
}

export default ExploreBook;