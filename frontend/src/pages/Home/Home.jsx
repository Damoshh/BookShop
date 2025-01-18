import React, { useState } from 'react'
import './Home.css'
import Header from '../../components/Header/Header'
import ExploreBook from '../../components/ExploreBook/ExploreBook'
import BookDisplay from '../../components/BookDisplay/BookDisplay'

const Home = ({ isLoggedIn, setShowLogin }) => {
  const [category, setCategory] = useState('All');

  const handleBrowseBooks = () => {
    document.getElementById('books-section').scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div className='home'>
      <Header onBrowseBooks={handleBrowseBooks} />
      <section id='books-section' className='books-section'>
        <ExploreBook category={category} setCategory={setCategory} />
        <BookDisplay 
          category={category} 
          isLoggedIn={isLoggedIn} 
          setShowLogin={setShowLogin}
        />
      </section>
    </div>
  )
}

export default Home