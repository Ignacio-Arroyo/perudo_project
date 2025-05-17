import React, { useState, useEffect } from 'react';
import './shop.css';
import axios from 'axios';
import { Link } from 'react-router-dom';

// Images pour les produits
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

const Shop = () => {
  const [playerId, setPlayerId] = useState(null);
  const [pieces, setPieces] = useState(0);
  const [username, setUsername] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [buyLoading, setBuyLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [products, setProducts] = useState([]);

  // Récupérer les produits depuis le backend
  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/products');
        console.log("Produits disponibles:", response.data);
        setProducts(response.data);
      } catch (err) {
        console.error("Erreur lors de la récupération des produits:", err);
        setError("Impossible de récupérer les produits du shop.");
      }
    };
    
    fetchProducts();
  }, []);

  useEffect(() => {
    // Récupérer les infos utilisateur du localStorage
    const getUserInfo = () => {
      try {
        const userStr = localStorage.getItem('user');
        if (userStr) {
          console.log("Informations utilisateur trouvées:", userStr);
          const user = JSON.parse(userStr);
          console.log("Informations utilisateur décodées:", user);
          
          if (user.player_id) {
            setPlayerId(user.player_id);
            setIsAuthenticated(true);
          } else if (user.id) {
            // Format alternatif possible
            setPlayerId(user.id);
            setIsAuthenticated(true);
          }
          
          // Si les pièces sont disponibles dans le localStorage, les utiliser
          if (user.pieces !== undefined) {
            setPieces(user.pieces);
          }
          if (user.username) {
            setUsername(user.username);
            setIsAuthenticated(true);
          }
        } else {
          console.log("Aucune information utilisateur trouvée dans localStorage");
        }
      } catch (err) {
        console.error('Erreur lors de la récupération des données utilisateur:', err);
        setError('Erreur lors de la récupération des données utilisateur');
      } finally {
        setLoading(false);
      }
    };

    getUserInfo();
  }, []);

  // Récupère le profil du joueur et met à jour le nombre de pièces
  useEffect(() => {
    const fetchPlayerProfile = async () => {
      if (!playerId) {
        console.log("Impossible de récupérer le profil: ID joueur manquant");
        return;
      }
      
      try {
        console.log(`Récupération du profil du joueur ${playerId}...`);
        // Utiliser axios au lieu de fetch pour être plus compatible
        const response = await axios.get(`http://localhost:8080/api/players/${playerId}`);
        if (response.status === 200) {
          console.log("Profil récupéré avec succès:", response.data);
          setPieces(response.data.pieces);
          setIsAuthenticated(true);
        }
      } catch (err) {
        console.error('Erreur lors de la récupération du profil:', err);
        setError('Erreur de connexion au serveur');
      }
    };

    if (playerId) {
      fetchPlayerProfile();
    }
  }, [playerId]);

  const handleBuy = async (productId) => {
    // Vérifier si l'utilisateur est connecté en vérifiant à la fois playerId et isAuthenticated
    if (!isAuthenticated || !playerId) {
      console.log("Tentative d'achat sans être connecté. ID:", playerId, "Auth:", isAuthenticated);
      setMessage({ type: 'error', text: "Vous devez être connecté !" });
      
      // Rechercher à nouveau les informations utilisateur au cas où
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          const user = JSON.parse(userStr);
          console.log("Informations utilisateur au moment de l'achat:", user);
          if (user.player_id || user.id) {
            window.location.reload(); // Recharger la page pour tenter de récupérer l'ID
          }
        } catch (e) {
          console.error("Erreur lors de la lecture des données utilisateur:", e);
        }
      }
      return;
    }
    
    setBuyLoading(true);
    setMessage({ type: '', text: '' });
    
    try {
      console.log(`Tentative d'achat du produit ${productId} pour le joueur ${playerId}`);
      const response = await axios.post(`http://localhost:8080/api/players/${playerId}/buy`, 
        { productId },
        { headers: { "Content-Type": "application/json" }}
      );
      
      if (response.status === 200) {
        console.log("Achat réussi:", response.data);
        // Mettre à jour le profil après l'achat
        const updatedProfile = await axios.get(`http://localhost:8080/api/players/${playerId}`);
        setPieces(updatedProfile.data.pieces);
        
        // Mettre à jour le localStorage également
        const userStr = localStorage.getItem('user');
        if (userStr) {
          const user = JSON.parse(userStr);
          user.pieces = updatedProfile.data.pieces;
          localStorage.setItem('user', JSON.stringify(user));
        }
        
        setMessage({ type: 'success', text: "Achat réussi ! Rendez-vous dans votre inventaire pour équiper votre nouveau dé." });
      }
    } catch (err) {
      console.error('Erreur lors de l\'achat:', err);
      let errorMessage = "Problème de connexion au serveur";
      
      if (err.response) {
        console.log("Réponse d'erreur:", err.response);
        if (err.response.data) {
          if (typeof err.response.data === 'string') {
            errorMessage = err.response.data;
          } else if (err.response.data.message) {
            errorMessage = err.response.data.message;
          }
        }
        
        if (err.response.status === 500) {
          errorMessage = "Erreur serveur. Le produit existe-t-il dans la base de données?";
        } else if (err.response.status === 404) {
          errorMessage = "Produit non trouvé dans la base de données";
        } else if (err.response.status === 400) {
          errorMessage = "Vous n'avez pas assez de pièces pour cet achat";
        }
      }
      
      setMessage({ type: 'error', text: `Erreur : ${errorMessage}` });
    } finally {
      setBuyLoading(false);
    }
  };

  if (loading) {
    return <div className="shop-container">Chargement...</div>;
  }

  return (
    <div className="shop-container">
      <h1>Shop des Skins de Dé</h1>
      
      {username && (
        <div style={{marginBottom: '10px', padding: '5px', backgroundColor: '#e8f4f8', borderRadius: '5px'}}>
          Connecté en tant que: <strong>{username}</strong> (ID: {playerId})
        </div>
      )}
      
      <div style={{marginBottom: '20px', fontWeight: 'bold', fontSize: '18px'}}>
        Vous avez : <span style={{color: '#4CAF50'}}>{pieces} pièces</span>
      </div>
      
      {error && (
        <div style={{color: 'red', marginBottom: '20px', padding: '10px', backgroundColor: '#ffeeee', borderRadius: '5px'}}>
          {error}
        </div>
      )}
      
      {message.text && (
        <div style={{
          marginBottom: '20px', 
          padding: '10px', 
          backgroundColor: message.type === 'error' ? '#ffeeee' : '#eeffee', 
          color: message.type === 'error' ? '#d32f2f' : '#388e3c',
          borderRadius: '5px'
        }}>
          {message.text}
        </div>
      )}
      
      {!isAuthenticated && (
        <div style={{color: 'red', marginBottom: '20px', padding: '10px', backgroundColor: '#ffeeee', borderRadius: '5px'}}>
          Vous n'êtes pas connecté. Veuillez vous connecter pour effectuer des achats.
        </div>
      )}
      
      {isAuthenticated && (
        <div style={{marginBottom: '20px'}}>
          <Link to="/inventory" className="inventory-link">
            Voir mon inventaire
          </Link>
        </div>
      )}
      
      <div className="products">
        {products.map((product) => (
          <div className="product-card" key={product.id}>
            <img src={productImages[product.id] || 'placeholder.png'} alt={product.name} style={{ width: '120px', height: '120px', objectFit: 'contain' }} />
            <h2>{product.name}</h2>
            <p className="price">{product.price} pièces</p>
            <button 
              onClick={() => handleBuy(product.id)}
              disabled={!isAuthenticated || pieces < product.price || buyLoading}
              style={{
                backgroundColor: !isAuthenticated ? '#aaaaaa' : 
                               pieces < product.price ? '#ffaaaa' : 
                               buyLoading ? '#cccccc' : '#4CAF50',
                cursor: !isAuthenticated || pieces < product.price || buyLoading ? 'not-allowed' : 'pointer',
                opacity: buyLoading ? 0.7 : 1
              }}
            >
              {!isAuthenticated ? 'Connectez-vous' : 
               pieces < product.price ? 'Pièces insuffisantes' : 
               buyLoading ? 'Achat en cours...' : 'Acheter'}
            </button>
          </div>
        ))}
      </div>
      
      {/* Pour le débogage en développement */}
      {process.env.NODE_ENV !== 'production' && (
        <div style={{marginTop: '30px', padding: '10px', backgroundColor: '#f5f5f5', borderRadius: '5px'}}>
          <h3>Informations de débogage</h3>
          <p>ID Joueur: {playerId || 'Non défini'}</p>
          <p>Authentifié: {isAuthenticated ? 'Oui' : 'Non'}</p>
          <p>Nom d'utilisateur: {username || 'Non défini'}</p>
          <p>Pièces: {pieces}</p>
        </div>
      )}
    </div>
  );
};

export default Shop;
