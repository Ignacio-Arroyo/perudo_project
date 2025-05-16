import axios from 'axios';

const updatePlayerACoins = async () => {
  try {
    // Chercher le joueur par son nom d'utilisateur
    const searchResponse = await axios.get('http://localhost:8080/api/players/search?username=a');
    
    if (searchResponse.status === 200) {
      const player = searchResponse.data;
      console.log('Joueur trouvé:', player);
      
      // Mettre à jour les pièces du joueur
      const updateResponse = await axios.put(
        `http://localhost:8080/api/players/update-coins/a`, 
        { coins: 2000 },
        { headers: { 'Content-Type': 'application/json' } }
      );
      
      if (updateResponse.status === 200) {
        console.log('Pièces mises à jour avec succès:', updateResponse.data);
        // Si l'utilisateur est déjà connecté, mettre à jour son localStorage
        const userStr = localStorage.getItem('user');
        if (userStr) {
          const user = JSON.parse(userStr);
          if (user.username === 'a') {
            user.pieces = 2000;
            localStorage.setItem('user', JSON.stringify(user));
            console.log('LocalStorage mis à jour pour le joueur a');
          }
        }
        alert('Le joueur "a" dispose maintenant de 2000 pièces!');
      }
    }
  } catch (error) {
    console.error('Erreur lors de la mise à jour des pièces:', error);
    alert('Erreur: ' + (error.response?.data || error.message));
  }
};

// Exécuter la fonction immédiatement
updatePlayerACoins();

export default updatePlayerACoins;
