import React, { useState } from 'react';
import './inventory.css';

const Inventory = () => {
    const [inventory, setInventory] = useState([]);
    const [equippedDice, setEquippedDice] = useState(null);
    

    useEffect(() => {
        fetch(`/api/players/${Player.getId()}/inventory`)
            .then(response => response.json())
            .then(data => setInventory(data))
            .catch(error => console.error('Error fetching inventory:', error));
    }, []);
    
    return (
        <div className="inventory">
            <h1>Inventory</h1>
            <ul>
                {inventory.map(item => (
                    <li key={item.id}>{item.name}</li>
                ))}
            </ul>
        </div>
    );
};

export default Inventory;
    
