import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [friends, setFriends] = useState([]);
  const [mailbox, setMailbox] = useState([]);

  // Function to fetch full player profile and update context
  const fetchAndUpdateUserProfile = async (userId) => {
    if (!userId) return;
    try {
      console.log(`[AuthContext] Fetching full profile for user ID: ${userId}`);
      // Temporarily bypass the proxy by using the full URL
      const response = await axios.get(`http://localhost:8080/api/players/${userId}`);
      if (response.data) {
        console.log("[AuthContext] Full profile fetched:", response.data);
        // Ensure all existing user data is preserved, only update fetched fields
        // and normalize IDs again just in case.
        const fetchedData = response.data;
        setUser(currentUser => {
          const updatedFullUser = {
            ...(currentUser || {}), // Keep existing context data if any
            ...fetchedData,       // Override with new data from fetch
            id: fetchedData.id || fetchedData.player_id || userId,
            player_id: fetchedData.player_id || fetchedData.id || userId,
            pieces: fetchedData.pieces ?? fetchedData.coins ?? 0,
            trophies: fetchedData.trophies ?? 0,
          };
          localStorage.setItem('user', JSON.stringify(updatedFullUser));
          console.log("[AuthContext] User context updated with full profile:", updatedFullUser);
          return updatedFullUser;
        });
      }
    } catch (error) {
      console.error('[AuthContext] Error fetching full user profile:', error);
      // Don't log out or clear user, just means profile refresh failed
    }
  };

  // Vérifier si l'utilisateur est déjà connecté au chargement de la page
  useEffect(() => {
    try {
      const storedUser = localStorage.getItem('user');
      if (storedUser && storedUser !== "undefined" && storedUser !== "null") {
        const userData = JSON.parse(storedUser);
        if (userData && userData.id) { // Ensure userData and its ID is valid
          setUser(userData);
          setIsAuthenticated(true);
          if (userData.friends) setFriends(userData.friends);
          if (userData.mailbox) setMailbox(userData.mailbox);
          
          // Check if critical data like pieces/trophies might be missing or stale
          // and fetch profile if necessary. This helps on initial load if localStorage data is minimal.
          if (userData.pieces === undefined || userData.trophies === undefined) {
            console.log("[AuthContext] Initial load: pieces or trophies missing. Fetching full profile.");
            fetchAndUpdateUserProfile(userData.id);
          }
        }
      }
    } catch (error) {
      console.error('Error parsing user data from localStorage:', error);
      localStorage.removeItem('user');
    }
  }, []);

  const login = async (loginData) => {
    if (!loginData) {
      console.error('Login attempted with undefined or null loginData');
      return;
    }
    
    try {
      const normalizedUserData = {
        ...loginData,
        id: loginData.id || loginData.player_id,
        player_id: loginData.player_id || loginData.id
      };
      
      console.log('[AuthContext] Normalized user data from login API:', normalizedUserData);
      
      // Set basic user data first
      localStorage.setItem('user', JSON.stringify(normalizedUserData));
      setUser(normalizedUserData);
      setIsAuthenticated(true);
      if (normalizedUserData.friends) setFriends(normalizedUserData.friends);
      if (normalizedUserData.mailbox) setMailbox(normalizedUserData.mailbox);
      
      console.log('[AuthContext] User logged in with initial data:', normalizedUserData);

      // If pieces or trophies are not part of loginData, fetch them
      if (normalizedUserData.id && (normalizedUserData.pieces === undefined || normalizedUserData.trophies === undefined)) {
        console.log("[AuthContext] Post-login: pieces or trophies missing. Fetching full profile.");
        await fetchAndUpdateUserProfile(normalizedUserData.id);
      } else {
        console.log("[AuthContext] Post-login: pieces and trophies seem present.");
      }

    } catch (error) {
      console.error('[AuthContext] Error while logging in:', error);
      // Potentially clear stored data if login process fails badly
      localStorage.removeItem('user');
      setUser(null);
      setIsAuthenticated(false);
    }
  };

  const logout = () => {
    try {
      localStorage.removeItem('user');
      setUser(null);
      setIsAuthenticated(false);
      setFriends([]);
      setMailbox([]);
      console.log('[AuthContext] User logged out');
    } catch (error) {
      console.error('[AuthContext] Error while logging out:', error);
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

  const updateUser = (updatedData) => {
    console.log("[AuthContext] updateUser called with:", updatedData);
    setUser(currentUser => {
      if (!currentUser) {
        // This case should ideally not happen if user is authenticated
        // But as a fallback, if there's no current user, initialize with updatedData.
        // Ensure ID is present if we're creating a new user object here.
        const newUserState = { id: updatedData.id || updatedData.player_id, ...updatedData };
        localStorage.setItem('user', JSON.stringify(newUserState));
        console.log("[AuthContext] updateUser: no current user, set to:", newUserState);
        return newUserState;
      }
      const newMergedUser = { ...currentUser, ...updatedData };
      localStorage.setItem('user', JSON.stringify(newMergedUser));
      console.log("[AuthContext] updateUser: merged to:", newMergedUser);
      return newMergedUser;
    });
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
      removeMessage,
      fetchAndUpdateUserProfile
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
