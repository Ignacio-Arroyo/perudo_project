import React, { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [friends, setFriends] = useState([]);
  const [mailbox, setMailbox] = useState([]);

  // Vérifier si l'utilisateur est déjà connecté au chargement de la page
  useEffect(() => {
    try {
      const storedUser = localStorage.getItem('user');
      if (storedUser && storedUser !== "undefined" && storedUser !== "null") {
        const userData = JSON.parse(storedUser);
        if (userData) {
          setUser(userData);
          setIsAuthenticated(true);
          
          // Charger les amis si disponibles
          if (userData.friends) {
            setFriends(userData.friends);
          }
          
          // Charger la boîte mail si disponible
          if (userData.mailbox) {
            setMailbox(userData.mailbox);
          }
        }
      }
    } catch (error) {
      console.error('Error parsing user data from localStorage:', error);
      localStorage.removeItem('user'); // Clear the invalid data
    }
  }, []);

  const login = (userData) => {
    if (!userData) {
      console.error('Login attempted with undefined or null userData');
      return;
    }
    
    try {
      // Normalize user data - ensure both id and player_id are available
      const normalizedUserData = {
        ...userData,
        // Make sure both ID formats are available
        id: userData.id || userData.player_id,
        player_id: userData.player_id || userData.id
      };
      
      console.log('Normalized user data for login:', normalizedUserData);
      
      localStorage.setItem('user', JSON.stringify(normalizedUserData));
      setUser(normalizedUserData);
      setIsAuthenticated(true);
      
      // Initialiser les amis si disponibles
      if (normalizedUserData.friends) {
        setFriends(normalizedUserData.friends);
      }
      
      // Initialiser la boîte mail si disponible
      if (normalizedUserData.mailbox) {
        setMailbox(normalizedUserData.mailbox);
      }
      
      console.log('User logged in:', normalizedUserData);
    } catch (error) {
      console.error('Error while logging in:', error);
    }
  };

  const logout = () => {
    try {
      localStorage.removeItem('user');
      setUser(null);
      setIsAuthenticated(false);
      setFriends([]);
      setMailbox([]);
      console.log('User logged out');
    } catch (error) {
      console.error('Error while logging out:', error);
    }
  };

  // Méthodes de gestion des amis
  const addFriend = (friend) => {
    const updatedFriends = [...friends, friend];
    setFriends(updatedFriends);
    
    // Mettre à jour dans localStorage
    const userData = { ...user, friends: updatedFriends };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const removeFriend = (friendId) => {
    const updatedFriends = friends.filter(friend => friend.id !== friendId);
    setFriends(updatedFriends);
    
    // Mettre à jour dans localStorage
    const userData = { ...user, friends: updatedFriends };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  // Méthodes de gestion de la boîte mail
  const addMessage = (message) => {
    const updatedMailbox = [...mailbox, message];
    setMailbox(updatedMailbox);
    
    // Mettre à jour dans localStorage
    const userData = { ...user, mailbox: updatedMailbox };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const removeMessage = (messageId) => {
    const updatedMailbox = mailbox.filter(msg => msg.id !== messageId);
    setMailbox(updatedMailbox);
    
    // Mettre à jour dans localStorage
    const userData = { ...user, mailbox: updatedMailbox };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  // Méthode de mise à jour des informations utilisateur
  const updateUser = (updatedData) => {
    const updatedUser = { ...user, ...updatedData };
    localStorage.setItem('user', JSON.stringify(updatedUser));
    setUser(updatedUser);
  };

  return (
    <AuthContext.Provider value={{ 
      isAuthenticated, 
      user, 
      login, 
      logout,
      updateUser,
      friends,
      addFriend,
      removeFriend,
      mailbox,
      addMessage,
      removeMessage
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
