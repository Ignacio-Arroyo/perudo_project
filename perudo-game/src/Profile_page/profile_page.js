import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './profile_page.css';

const Profile_page = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [editMode, setEditMode] = useState(false);
    
    // État pour les champs modifiables
    const [formData, setFormData] = useState({
        username: '',
        nom: '',
        prenom: '',
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    
    // État pour les messages de feedback
    const [message, setMessage] = useState({ type: '', text: '' });
    
    const navigate = useNavigate();

    useEffect(() => {
        fetchUserData();
    }, []);

    const fetchUserData = async () => {
        try {
            const userStr = localStorage.getItem('user');
            if (!userStr) {
                setError('Veuillez vous connecter pour accéder à votre profil');
                setLoading(false);
                return;
            }

            const userData = JSON.parse(userStr);
            const userId = userData.player_id || userData.id;

            if (!userId) {
                setError('Information utilisateur incomplète. Essayez de vous reconnecter.');
                setLoading(false);
                return;
            }

            const response = await axios.get(`http://localhost:8080/api/players/${userId}`);
            
            if (response.status === 200) {
                setUser(response.data);
                setFormData({
                    username: response.data.username || '',
                    nom: response.data.nom || '',
                    prenom: response.data.prenom || '',
                    currentPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                });
            }
        } catch (err) {
            console.error('Erreur lors de la récupération du profil:', err);
            setError('Erreur lors de la récupération de votre profil');
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage({ type: '', text: '' });
        
        // Validation
        if (formData.newPassword && formData.newPassword !== formData.confirmPassword) {
            setMessage({ type: 'error', text: 'Les nouveaux mots de passe ne correspondent pas' });
            return;
        }
        
        try {
            const userId = user.player_id || user.id;
            
            // Créer l'objet à envoyer (sans les champs liés aux mots de passe)
            const updateData = {
                username: formData.username,
                nom: formData.nom,
                prenom: formData.prenom
            };
            
            // Ajouter les mots de passe si nécessaire
            if (formData.newPassword && formData.currentPassword) {
                // Vérifier d'abord le mot de passe actuel
                try {
                    await axios.post('http://localhost:8080/api/players/verify-password', {
                        playerId: userId,
                        password: formData.currentPassword
                    });
                    
                    // Si pas d'erreur, on peut ajouter le nouveau mot de passe
                    updateData.password = formData.newPassword;
                } catch (err) {
                    setMessage({ type: 'error', text: 'Le mot de passe actuel est incorrect' });
                    return;
                }
            }
            
            const response = await axios.put(`http://localhost:8080/api/players/${userId}`, updateData);
            
            if (response.status === 200) {
                // Mettre à jour les informations dans le localStorage
                const userStr = localStorage.getItem('user');
                if (userStr) {
                    const userData = JSON.parse(userStr);
                    userData.username = formData.username;
                    userData.nom = formData.nom;
                    userData.prenom = formData.prenom;
                    localStorage.setItem('user', JSON.stringify(userData));
                }
                
                setUser(response.data);
                setMessage({ type: 'success', text: 'Profil mis à jour avec succès' });
                setEditMode(false);
            }
        } catch (err) {
            console.error('Erreur lors de la mise à jour du profil:', err);
            setMessage({ 
                type: 'error', 
                text: err.response?.data || 'Erreur lors de la mise à jour du profil'
            });
        }
    };

    if (loading) {
        return <div className="profile-container loading">Chargement...</div>;
    }

    if (error) {
        return (
            <div className="profile-container">
                <div className="error-message">{error}</div>
                <button 
                    className="primary-button" 
                    onClick={() => navigate('/connexion')}
                >
                    Se connecter
                </button>
            </div>
        );
    }

    if (!user) {
        return (
            <div className="profile-container">
                <div className="error-message">Aucune information utilisateur trouvée</div>
                <button 
                    className="primary-button" 
                    onClick={() => navigate('/connexion')}
                >
                    Se connecter
                </button>
            </div>
        );
    }

    return (
        <div className="profile-container">
            <h1>Mon Profil</h1>
            
            {message.text && (
                <div className={`message ${message.type}`}>
                    {message.text}
                </div>
            )}
            
            {editMode ? (
                <form onSubmit={handleSubmit} className="profile-form">
                    <div className="form-group">
                        <label htmlFor="username">Nom d'utilisateur</label>
                        <input 
                            type="text" 
                            id="username" 
                            name="username" 
                            value={formData.username}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="nom">Nom</label>
                        <input 
                            type="text" 
                            id="nom" 
                            name="nom" 
                            value={formData.nom}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="prenom">Prénom</label>
                        <input 
                            type="text" 
                            id="prenom" 
                            name="prenom" 
                            value={formData.prenom}
                            onChange={handleInputChange}
                            required
                        />
                    </div>
                    
                    <div className="form-divider">
                        <h3>Changer de mot de passe</h3>
                        <p className="form-help">Laissez vide pour conserver votre mot de passe actuel</p>
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="currentPassword">Mot de passe actuel</label>
                        <input 
                            type="password" 
                            id="currentPassword" 
                            name="currentPassword" 
                            value={formData.currentPassword}
                            onChange={handleInputChange}
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="newPassword">Nouveau mot de passe</label>
                        <input 
                            type="password" 
                            id="newPassword" 
                            name="newPassword" 
                            value={formData.newPassword}
                            onChange={handleInputChange}
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="confirmPassword">Confirmer le mot de passe</label>
                        <input 
                            type="password" 
                            id="confirmPassword" 
                            name="confirmPassword" 
                            value={formData.confirmPassword}
                            onChange={handleInputChange}
                        />
                    </div>
                    
                    <div className="button-group">
                        <button type="submit" className="primary-button">
                            Sauvegarder
                        </button>
                        <button 
                            type="button" 
                            className="secondary-button"
                            onClick={() => setEditMode(false)}
                        >
                            Annuler
                        </button>
                    </div>
                </form>
            ) : (
                <div className="profile-info">
                    <div className="info-section">
                        <div className="info-item">
                            <h3>Informations personnelles</h3>
                            <p><span>Nom d'utilisateur:</span> {user.username}</p>
                            <p><span>Nom:</span> {user.nom}</p>
                            <p><span>Prénom:</span> {user.prenom}</p>
                            <p><span>Code Ami:</span> {user.friendCode}</p>
                            <button 
                                className="edit-button"
                                onClick={() => setEditMode(true)}
                            >
                                Modifier mon profil
                            </button>
                        </div>
                        
                        <div className="info-item">
                            <h3>Statistiques</h3>
                            <p><span>Pièces:</span> {user.pieces}</p>
                            <p><span>Trophées:</span> {user.trophies}</p>
                            <p><span>Taux de victoire:</span> {user.winRate}%</p>
                        </div>
                    </div>
                    
                    <div className="info-section">
                        <div className="info-item">
                            <h3>Inventaire</h3>
                            <div className="inventory-preview">
                                {user.inventory && user.inventory.length > 0 ? (
                                    <p>{user.inventory.length} objets - <a href="/inventory">Voir l'inventaire</a></p>
                                ) : (
                                    <p>Aucun objet - <a href="/shop">Visiter la boutique</a></p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Profile_page;

