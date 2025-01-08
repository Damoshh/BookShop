import React, { useState } from 'react'
import './Home.css'
import Header from '../../components/Header/Header'
import ExploreBook from '../../components/ExploreBook/ExploreBook'
import BookDisplay from '../../components/BookDisplay/BookDisplay'

const Home = () => {

  const [category,setCategory] = useState('All');

  return (
    <div>
      <Header/>
      <ExploreBook category={category} setCategory={setCategory}/>
      <BookDisplay category={category} />
    </div>
  )
}

export default Home
