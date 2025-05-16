import React, { useState } from 'react';
import axios from 'axios';
import Button from 'react-bootstrap/Button';
import '../Home_middle_section/home_middle_section.css';

const UpdatePlayerCoinsPage = () => {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState('');
  const [error, setError] = useState('');

  const updatePlayerACoins = async () => {
    setLoading(true);
    setResult('');
    setError('');
    
    try {
      // Mettre à jour les pièces du joueur directement
      const updateResponse = await axios.put(
        `http://localhost:8080/api/players/update-coins/a`, 
        { coins: 2000 },
        { headers: { 'Content-Type': 'application/json' } }
      );
      
      if (updateResponse.status === 200) {
        setResult('Le joueur "a" dispose maintenant de 2000 pièces!');
        
        // Si l'utilisateur est déjà connecté, mettre à jour son localStorage
        const userStr = localStorage.getItem('user');
        if (userStr) {
          const user = JSON.parse(userStr);
          if (user.username === 'a') {
            user.pieces = 2000;
            localStorage.setItem('user', JSON.stringify(user));
            setResult(prev => prev + ' LocalStorage mis à jour.');
          }
        }
      }
    } catch (err) {
      console.error('Erreur lors de la mise à jour des pièces:', err);
      setError('Erreur: ' + (err.response?.data || err.message));
    } finally {
      setLoading(false);
    }
  };

  const updatePlayerInDatabase = async () => {
    setLoading(true);
    setResult('');
    setError('');
    
    try {
      // Exécuter une requête SQL directe pour mettre à jour les pièces
      // Note: Cette fonctionnalité nécessite une API côté serveur qui n'existe pas encore
      const response = await axios.post(
        'http://localhost:8080/api/admin/execute-sql',
        { query: "UPDATE player SET pieces = 2000 WHERE username = 'a';" },
        { headers: { 'Content-Type': 'application/json' } }
      );
      
      if (response.status === 200) {
        setResult('SQL exécuté avec succès. Le joueur "a" dispose maintenant de 2000 pièces dans la base de données!');
      }
    } catch (err) {
      console.error('Erreur lors de l\'exécution SQL:', err);
      setError('Cette fonctionnalité n\'est pas encore implémentée. Veuillez utiliser l\'autre méthode.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="home-middle-section">
      <div style={{ 
        maxWidth: '600px', 
        margin: '0 auto', 
        padding: '20px', 
        backgroundColor: '#f5f5f5', 
        borderRadius: '10px',
        boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
      }}>
        <h1>Mise à jour des pièces du joueur "a"</h1>
        <p>Cet outil vous permet de mettre à jour le nombre de pièces du joueur "a" à 2000 pour les tests.</p>
        
        <div style={{ marginTop: '20px' }}>
          <Button 
            variant="primary" 
            onClick={updatePlayerACoins} 
            disabled={loading}
            style={{ marginRight: '10px' }}
          >
            {loading ? 'Mise à jour en cours...' : 'Mettre à jour via API REST'}
          </Button>
          
          <Button 
            variant="secondary" 
            onClick={updatePlayerInDatabase} 
            disabled={loading}
          >
            Mettre à jour directement en base de données
          </Button>
        </div>
        
        {result && (
          <div style={{ 
            marginTop: '20px', 
            padding: '10px', 
            backgroundColor: '#d4edda', 
            color: '#155724',
            borderRadius: '5px'
          }}>
            {result}
          </div>
        )}
        
        {error && (
          <div style={{ 
            marginTop: '20px', 
            padding: '10px', 
            backgroundColor: '#f8d7da', 
            color: '#721c24',
            borderRadius: '5px'
          }}>
            {error}
          </div>
        )}
        
        <div style={{ marginTop: '20px' }}>
          <h3>Instructions pour la mise à jour manuelle</h3>
          <p>Si l'API ne fonctionne pas, vous pouvez mettre à jour manuellement les pièces en exécutant cette commande SQL dans PostgreSQL:</p>
          <pre style={{ 
            backgroundColor: '#f8f9fa', 
            padding: '10px', 
            borderRadius: '5px',
            overflowX: 'auto'
          }}>
            UPDATE player SET pieces = 2000 WHERE username = 'a';
          </pre>
        </div>
      </div>
    </div>
  );
};

export default UpdatePlayerCoinsPage; 