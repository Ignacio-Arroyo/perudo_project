import React, { useState, useEffect } from 'react';
import './shop.css';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { useAuth } from '../Auth/authcontext';

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
  const { user: authUser, updateUser, isAuthenticated: authIsAuthenticated } = useAuth();
  const [playerId, setPlayerId] = useState(null);
  const [pieces, setPieces] = useState(authUser?.pieces || 0);
  const [username, setUsername] = useState(authUser?.username || '');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAuthenticated, setIsAuthenticated] = useState(authIsAuthenticated);
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
    if (authUser) {
      setPlayerId(authUser.id || authUser.player_id);
      setUsername(authUser.username || '');
      setPieces(authUser.pieces !== undefined ? authUser.pieces : 0);
      setIsAuthenticated(true);
      setLoading(false);
    } else {
        const userStr = localStorage.getItem('user');
        if (userStr) {
          try {
            const user = JSON.parse(userStr);
            setPlayerId(user.id || user.player_id);
            setUsername(user.username || '');
            setPieces(user.pieces !== undefined ? user.pieces : 0);
            setIsAuthenticated(true);
          } catch (e) {
            console.error("Failed to parse user from localStorage in Shop:", e);
            setIsAuthenticated(false);
          }
        } else {
            setIsAuthenticated(false);
        }
      setLoading(false);
    }
  }, [authUser]);

  // Récupère le profil du joueur et met à jour le nombre de pièces
  useEffect(() => {
    const fetchPlayerProfile = async () => {
      if (!playerId) {
        console.log("Impossible de récupérer le profil: ID joueur manquant");
        return;
      }
      if (!isAuthenticated) {
          return;
      }
      
      try {
        console.log(`Récupération du profil du joueur ${playerId} pour le Shop...`);
        const response = await axios.get(`http://localhost:8080/api/players/${playerId}`);
        if (response.status === 200) {
          console.log("Profil récupéré avec succès pour le Shop:", response.data);
          setPieces(response.data.pieces);
          if (updateUser && authUser && response.data.pieces !== authUser.pieces) {
            updateUser({ pieces: response.data.pieces });
          }
        }
      } catch (err) {
        console.error('Erreur lors de la récupération du profil pour le Shop:', err);
      }
    };

    if (playerId && isAuthenticated) {
      fetchPlayerProfile();
    }
  }, [playerId, isAuthenticated, updateUser, authUser]);

  const handleBuy = async (productId) => {
    if (!isAuthenticated || !playerId) {
      console.log("Tentative d'achat sans être connecté. ID:", playerId, "Auth:", isAuthenticated);
      setMessage({ type: 'error', text: "Vous devez être connecté !" });
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
        const productPrice = products.find(p => p.id === productId)?.price || 0;
        const currentPieces = authUser?.pieces !== undefined ? authUser.pieces : pieces;
        const newPieces = response.data.newPieceCount !== undefined 
            ? response.data.newPieceCount 
            : currentPieces - productPrice;
        
        setPieces(newPieces);

        if (updateUser) {
          updateUser({ pieces: newPieces });
        }
        
        setMessage({ type: 'success', text: "Achat réussi ! Rendez-vous dans votre inventaire pour équiper votre nouveau dé." });
      }
    } catch (err) {
      console.error('Erreur lors de l\'achat:', err);
      let specificMessage = "Une erreur est survenue lors de l'achat.";
      if (err.response) {
        if (err.response.status === 400) {
            specificMessage = err.response.data?.message || err.response.data || "Vous n'avez peut-être pas assez de pièces.";
            if (typeof specificMessage === 'string' && specificMessage.toLowerCase().includes('pièces')) {
                specificMessage = "Vous n'avez pas assez de pièces pour cet achat.";
            }
        } else if (err.response.status === 404) {
            specificMessage = "Le produit demandé n'a pas été trouvé.";
        } else if (err.response.status === 500) {
            specificMessage = "Erreur du serveur lors du traitement de votre achat.";
        } else if (err.response.data?.message) {
            specificMessage = err.response.data.message;
        } else if (typeof err.response.data === 'string') {
            specificMessage = err.response.data;
        }
      } else if (err.request) {
        specificMessage = "Aucune réponse du serveur. Vérifiez votre connexion.";
      } else {
        specificMessage = err.message || "Erreur inconnue lors de la configuration de la requête.";
      }
      setMessage({ type: 'error', text: specificMessage });
    } finally {
      setBuyLoading(false);
    }
  };

  if (loading) {
    return <div className="shop-container loading-container">Chargement...</div>;
  }

  return (
    <div className="shop-container">
      <h1>Shop des Skins de Dé</h1>
      
      <div className="user-info">
        {isAuthenticated && username && (
          <div className="connection-status connected">
            Connecté en tant que: <strong>{username}</strong> (ID: {playerId})
          </div>
        )}
        
        <div className="pieces-display">
          Vous avez : {pieces} pièces 💰
        </div>
        
        {!isAuthenticated && (
          <div className="connection-status disconnected">
            Vous n'êtes pas connecté. Veuillez vous connecter pour effectuer des achats.
          </div>
        )}
        
        {isAuthenticated && (
          <Link to="/inventory" className="inventory-link">
            📦 Voir mon inventaire
          </Link>
        )}
      </div>
      
      {error && (
        <div className="message error">
          {error}
        </div>
      )}
      
      {message.text && (
        <div className={`message ${message.type}`}>
          {message.text}
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
        <div style={{marginTop: '30px', padding: '10px', color: '#333', backgroundColor: 'rgba(255,255,255,0.8)', borderRadius: '5px'}}>
          <h3>Informations de débogage (Shop)</h3>
          <p>Auth User ID: {authUser?.id || 'N/A'}</p>
          <p>Auth User Pieces: {authUser?.pieces ?? 'N/A'}</p>
          <p>Auth User Trophies: {authUser?.trophies ?? 'N/A'}</p>
          <p>Local Player ID: {playerId || 'Non défini'}</p>
          <p>Local IsAuthenticated: {isAuthenticated ? 'Oui' : 'Non'}</p>
          <p>Local Username: {username || 'Non défini'}</p>
          <p>Local Pieces: {pieces}</p>
        </div>
      )}
    </div>
  );
};

export default Shop;
