import React, { useState, useEffect } from 'react';
import './Inventory.css';
import axios from 'axios';

const Inventory = () => {
    const [inventory, setInventory] = useState([]);
    const [equippedProduct, setEquippedProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [message, setMessage] = useState({ type: '', text: '' });
    const [userId, setUserId] = useState(null);

    // Récupérer l'ID du joueur depuis le localStorage
    useEffect(() => {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            const user = JSON.parse(userStr);
            setUserId(user.player_id || user.id);
            setEquippedProduct(user.equippedProduct || null);
        }
    }, []);

    // Récupérer l'inventaire du joueur
    useEffect(() => {
        if (!userId) return;

        const fetchInventory = async () => {
            try {
                setLoading(true);
                const response = await axios.get(`http://localhost:8080/api/players/${userId}/inventory`);
                
                if (response.status === 200) {
                    console.log("Inventaire récupéré:", response.data);
                    setInventory(response.data);
                }
            } catch (error) {
                console.error('Erreur lors de la récupération de l\'inventaire:', error);
                setError('Impossible de récupérer l\'inventaire.');
            } finally {
                setLoading(false);
            }
        };

        fetchInventory();
    }, [userId]);

    // Fonction pour équiper un produit
    const handleEquip = async (productId) => {
        if (!userId) {
            setMessage({ type: 'error', text: 'Vous devez être connecté pour équiper un produit.' });
            return;
        }

        try {
            const response = await axios.post(`http://localhost:8080/api/players/${userId}/equip`, 
                { diceId: productId },
                { headers: { 'Content-Type': 'application/json' }}
            );

            if (response.status === 200) {
                setEquippedProduct(productId);
                setMessage({ type: 'success', text: 'Produit équipé avec succès!' });
                
                // Mettre à jour le localStorage
                const userStr = localStorage.getItem('user');
                if (userStr) {
                    const user = JSON.parse(userStr);
                    user.equippedProduct = productId;
                    localStorage.setItem('user', JSON.stringify(user));
                }
            }
        } catch (error) {
            console.error('Erreur lors de l\'équipement du produit:', error);
            setMessage({ 
                type: 'error', 
                text: error.response?.data || 'Erreur lors de l\'équipement du produit.' 
            });
        }
    };

    if (loading) {
        return <div className="inventory">Chargement de l'inventaire...</div>;
    }

    if (error) {
        return <div className="inventory">
            <h1>Inventaire</h1>
            <div className="error-message">{error}</div>
        </div>;
    }

    // Liste des produits disponibles avec leurs images
    const productImages = {
        1: require('../assets/woodensetdice.png'),
        2: require('../assets/redsetdice.png'),
        3: require('../assets/orangesetdice.png'),
        4: require('../assets/multicolorsetdice.png'),
        5: require('../assets/grey-blacksetdice.jpg'),
        6: require('../assets/greensetdice.png'),
        7: require('../assets/clearbluesetdice.png'),
        8: require('../assets/bluesetdice.png'),
        9: require('../assets/blacksetdice.png'),
    };

    return (
        <div className="inventory-container">
            <h1>Inventaire</h1>
            
            {message.text && (
                <div className={`message ${message.type}`}>
                    {message.text}
                </div>
            )}
            
            {inventory.length === 0 ? (
                <p>Votre inventaire est vide. Achetez des dés dans le Shop!</p>
            ) : (
                <div className="inventory-grid">
                    {inventory.map(item => (
                        <div key={item.id} className={`inventory-item ${equippedProduct === item.id ? 'equipped' : ''}`}>
                            {productImages[item.id] && (
                                <img src={productImages[item.id]} alt={item.name} />
                            )}
                            <h3>{item.name}</h3>
                            <button 
                                onClick={() => handleEquip(item.id)}
                                disabled={equippedProduct === item.id}
                                className={equippedProduct === item.id ? 'equipped-button' : 'equip-button'}
                            >
                                {equippedProduct === item.id ? 'Équipé' : 'Équiper'}
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Inventory;
    
