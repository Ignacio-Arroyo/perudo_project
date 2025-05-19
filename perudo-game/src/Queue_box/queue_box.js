import React from 'react';
import '../Home_middle_section/home_middle_section.css';
import './queue_box.css'
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';

const Queue_box = () => {
  return (
    <div className="home-middle-section">
        <div className='main-block'>
            <h1>Search for a public game</h1>

            <div className='lobby-form'>
                <Button 
                    id='search-public-game'
                    className='queue-button public-match-button'
                    size="lg"
                >
                    Search for Public Match
                </Button>
            </div>

            {/* need to see if we will add public lobbies or only private --level of complexity for the matchmaking */}

            {/* <div className='lobby-form'>  
                <p>Join a public lobby</p>  
                <Button id='join-public-game'>Join</Button>
            </div> */}
            
            <div className='lobby-form'>
                <p className='queue-text'>Join a private lobby</p>
                <Form.Control className='queue-code' type="game-code" placeholder="12345" />
                <Button id='join-private-game'
                className='queue-button'>Join</Button>
            </div>
            
            <div className='lobby-form'>
                <p className='queue-text'>Create new game lobby</p>
                <Form.Control className='queue-code' type="new-game-code" placeholder="12345" />
                <Button id='create-new-game' className='queue-button'>Create</Button>
            </div>

        </div>
    </div>
  );
};

export default  Queue_box ;
