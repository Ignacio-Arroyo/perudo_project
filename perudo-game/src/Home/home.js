import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../Auth/authcontext';
import './home.css';

const Home = () => {
  const navigate = useNavigate();
  const { user, fetchAndUpdateUserProfile } = useAuth();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user && (user.pieces === undefined || user.trophies === undefined)) {
      setLoading(true);
      fetchAndUpdateUserProfile(user.id).finally(() => setLoading(false));
    }
  }, [user, fetchAndUpdateUserProfile]);

  // Si l'utilisateur est là mais que les infos sont manquantes, on affiche un chargement
  if (user && (user.pieces === undefined || user.trophies === undefined || loading)) {
    return (
      <div className="home-container">
        <div className="hero-section">
          <div className="hero-content">
            <h1 className="hero-title">
              Bienvenue sur <span className="gradient-text">Perudo</span>
            </h1>
            <p className="hero-subtitle">
              Le jeu de dés le plus passionnant en ligne
            </p>
            <div className="welcome-message">
              <p>Chargement des informations joueur...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const handleNavigation = (path) => {
    navigate(path);
  };

  const menuItems = [
    {
      id: 'game',
      title: 'Jouer',
      subtitle: 'Lancer une partie',
      path: '/game/board',
      icon: '🎲',
      color: 'from-red-500 to-red-700',
      description: 'Rejoindre ou créer une partie de Perudo'
    },
    {
      id: 'shop',
      title: 'Boutique',
      subtitle: 'Acheter des skins',
      path: '/shop',
      icon: '🛒',
      color: 'from-purple-500 to-purple-700',
      description: 'Découvrir les nouveaux dés colorés'
    },
    {
      id: 'inventory',
      title: 'Inventaire',
      subtitle: 'Gérer vos objets',
      path: '/inventory',
      icon: '🎒',
      color: 'from-blue-500 to-blue-700',
      description: 'Équiper vos dés et accessoires'
    },
    {
      id: 'leaderboard',
      title: 'Classement',
      subtitle: 'Top joueurs',
      path: '/leaderboard',
      icon: '🏆',
      color: 'from-yellow-500 to-yellow-700',
      description: 'Voir les meilleurs joueurs'
    },
    {
      id: 'friends',
      title: 'Amis',
      subtitle: 'Réseau social',
      path: '/friends',
      icon: '👥',
      color: 'from-green-500 to-green-700',
      description: 'Gérer votre liste d\'amis'
    },
    {
      id: 'mail',
      title: 'Messages',
      subtitle: 'Boîte de réception',
      path: '/mail',
      icon: '📧',
      color: 'from-indigo-500 to-indigo-700',
      description: 'Consulter vos messages'
    },
    {
      id: 'statistics',
      title: 'Statistiques',
      subtitle: 'Vos performances',
      path: '/statistics',
      icon: '📊',
      color: 'from-teal-500 to-teal-700',
      description: 'Analyser vos parties'
    },
    {
      id: 'history',
      title: 'Historique',
      subtitle: 'Parties jouées',
      path: '/match-history',
      icon: '📝',
      color: 'from-orange-500 to-orange-700',
      description: 'Revoir vos anciennes parties'
    }
  ];

  return (
    <div className="home-container">
      {/* Hero Section */}
      <div className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">
            Bienvenue sur <span className="gradient-text">Perudo</span>
          </h1>
          <p className="hero-subtitle">
            Le jeu de dés le plus passionnant en ligne
          </p>
          {user && (
            <div className="welcome-message">
              <p>Content de vous revoir, <strong>{user.username}</strong>!</p>
              <div className="user-stats">
                <span className="stat">
                  💰 {user.pieces || 0} pièces
                </span>
                <span className="stat">
                  🏆 {user.trophies || 0} trophées
                </span>
              </div>
            </div>
          )}
        </div>
        <div className="hero-decoration">
          <div className="floating-dice">🎲</div>
          <div className="floating-dice delay-1">🎲</div>
          <div className="floating-dice delay-2">🎲</div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="quick-actions">
        <button 
          className="quick-action-btn primary"
          onClick={() => handleNavigation('/game/board')}
        >
          <span className="btn-icon">🎮</span>
          Jouer Maintenant
        </button>
        <button 
          className="quick-action-btn secondary"
          onClick={() => handleNavigation('/shop')}
        >
          <span className="btn-icon">🛍️</span>
          Boutique
        </button>
        <button 
          className="quick-action-btn secondary"
          onClick={() => handleNavigation('/profile')}
        >
          <span className="btn-icon">👤</span>
          Profil
        </button>
      </div>

      {/* Menu Grid */}
      <div className="menu-grid">
        {menuItems.map((item, index) => (
          <div
            key={item.id}
            className={`menu-card ${item.color}`}
            style={{ animationDelay: `${index * 0.1}s` }}
            onClick={() => handleNavigation(item.path)}
          >
            <div className="card-background"></div>
            <div className="card-content">
              <div className="card-icon">{item.icon}</div>
              <h3 className="card-title">{item.title}</h3>
              <p className="card-subtitle">{item.subtitle}</p>
              <p className="card-description">{item.description}</p>
            </div>
            <div className="card-hover-effect"></div>
          </div>
        ))}
      </div>

      {/* Footer Info */}
      <div className="home-footer">
        <div className="footer-content">
          <h3>🎯 Comment jouer ?</h3>
          <p>
            Perudo est un jeu de bluff avec des dés. Enchérissez sur le nombre total de dés
            d'une certaine valeur sur la table, ou défiez l'enchère précédente !
          </p>
        </div>
      </div>
    </div>
  );
};

export default Home;
