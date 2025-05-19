/**
 * @typedef {Object} Player
 * @property {string} id - Player's unique identifier
 * @property {string} name - Player's display name
 * @property {number} diceCount - Number of dice the player has
 * @property {Array<number>} dice - Array of dice values
 * @property {boolean} isCurrentTurn - Whether it's this player's turn
 */

/**
 * @typedef {Object} Bid
 * @property {string} playerId - ID of the player making the bid
 * @property {number} quantity - Number of dice bid
 * @property {number} value - Value of the dice (1-6, where 1 is wild/Paco)
 */

/**
 * @typedef {Object} GameState
 * @property {string} gameId - Unique game identifier
 * @property {Array<Player>} players - List of players in the game
 * @property {Bid|null} currentBid - Current bid on the table
 * @property {string} currentPlayerId - ID of the player whose turn it is
 * @property {('WAITING'|'PLAYING'|'FINISHED')} gameStatus - Current game status
 * @property {number} round - Current round number
 * @property {string|null} winner - ID of the winning player
 */

export const GAME_STATUS = {
    WAITING: 'WAITING',
    PLAYING: 'PLAYING',
    FINISHED: 'FINISHED'
};

export const MAX_PLAYERS = 6;
export const MIN_PLAYERS = 2;
export const STARTING_DICE = 5;
export const PACO_VALUE = 1; // Wild dice value