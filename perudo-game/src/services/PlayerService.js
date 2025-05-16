import axios from 'axios';

const API_URL = 'http://localhost:8080/api/players';

const getPlayers = async () => {
  const response = await axios.get(API_URL);
  return response.data;
};

const getPlayerByUsername = async (username) => {
  const response = await axios.get(`${API_URL}/search?username=${username}`);
  return response.data;
};

const updatePlayerCoins = async (username, coins) => {
  const response = await axios.put(`${API_URL}/update-coins/${username}`, { coins });
  return response.data;
};

export { getPlayers, getPlayerByUsername, updatePlayerCoins }; 