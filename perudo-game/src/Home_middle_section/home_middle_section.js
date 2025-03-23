import React from 'react';
import './home_middle_section.css';
import perudo_image from '../assets/perudo.jpg';

const Home_middle_section = () => {
  return (
    <div className="home-middle-section">
      <div className="main-block">
        <h1 className="title">Perudo</h1>
        <p className="description">
          Perudo is a thrilling dice game where each player gets five dice and a cup to roll and hide them. Players make increasingly higher declarations about the results of all the dice in the game, such as "there are ten sixes." However, any player can challenge the last bid. When a challenge occurs, all dice are revealed, and either the bidder or the challenger loses dice, depending on who was correct. The last player with dice remaining is the winner!
        </p>
        
        <div className="game-details" style={{display: "flex", flexDirection: "row"}}>
          <div>
            <h2>Game Details</h2>
            <ul>
              <li>Number of Players: 2 to 6</li>
              <li>Age: 8 and up</li>
              <li>Play Time: 20-30 minutes</li>
              <li>Skills: Bluffing, Probability, Strategy</li>
            </ul>
          </div>
          <div style={{margin: "auto"}}>
            <img src={perudo_image} alt="Perudo Dice" className="game-image"/>
          </div>
        </div>
        <div className="fun-facts">
          <h2>Fun Facts</h2>
          <ul>
            <li>Perudo is also known as Liar's Dice and has many variations around the world.</li>
            <li>The game is believed to have originated in South America.</li>
            <li>Perudo was popularized in the movie "Pirates of the Caribbean."</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Home_middle_section;
