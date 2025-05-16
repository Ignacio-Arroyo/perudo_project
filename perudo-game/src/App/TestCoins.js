import React, { useState } from 'react';
import { updatePlayerCoins } from '../services/PlayerService';
import Button from 'react-bootstrap/Button';

const TestCoins = () => {
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);

  const handleAddCoins = async () => {
    try {
      setLoading(true);
      const response = await updatePlayerCoins('a', 2000);
      setStatus(`Succès: Joueur "a" a maintenant 2000 pièces`);
      console.log('Résultat:', response);
    } catch (error) {
      console.error('Erreur lors de la mise à jour des pièces:', error);
      setStatus(`Erreur: ${error.response?.data || error.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ margin: '20px', padding: '20px', border: '1px solid #ccc', borderRadius: '5px' }}>
      <h2>Test: Ajouter 2000 pièces au joueur "a"</h2>
      <Button 
        variant="primary" 
        onClick={handleAddCoins} 
        disabled={loading}
      >
        {loading ? 'Mise à jour...' : 'Ajouter 2000 pièces au joueur "a"'}
      </Button>
      {status && (
        <div style={{ marginTop: '10px', padding: '10px', backgroundColor: status.includes('Erreur') ? '#ffdddd' : '#ddffdd' }}>
          {status}
        </div>
      )}
    </div>
  );
};

export default TestCoins; 