import React from 'react'
import './ExploreBook.css'
import { category_list } from './CategoryList'

const ExploreBook = ({category,setCategory}) => {
  return (
    <div className='explore-book' id = 'explore-book'>
        <h1>mari cari</h1>
        <p className='explore-book-text'>sdfsfadsfaf</p>
        <div className='explore-book-list'>
            {category_list.map((item,index) => {
                return (
                    <div onClick={()=>setCategory(prev=>prev===item.category_name? 'All' :item.category_name)} 
                        key={index} className="explore-book-list-item"
                    >
                        <img 
                        className = {category === item.category_name ? 'active':''} 
                        src={item.category_image} 
                        alt=''
                        />
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
