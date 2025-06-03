package perudo_backend.perudo_backend.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import perudo_backend.exception.GameFullException;
import perudo_backend.exception.GameNotFoundException;
import perudo_backend.exception.NotEnoughPlayersException;
import perudo_backend.exception.NotYourTurnException;
import perudo_backend.exception.PlayerNotFoundException;
import perudo_backend.perudo_backend.Bid;
import perudo_backend.perudo_backend.Game;
import perudo_backend.perudo_backend.GameStatus;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.GameRecord;
import perudo_backend.perudo_backend.dto.GameStateDTO;
import perudo_backend.perudo_backend.dto.ChallengeResultDTO;
import perudo_backend.perudo_backend.dto.BidDTO;
import perudo_backend.perudo_backend.dto.GameEndResultDTO;
import perudo_backend.perudo_backend.repositories.PlayerRepository;
import perudo_backend.perudo_backend.repositories.GameRecordRepository;

import java.util.*;
import java.util.stream.Collectors;
import perudo_backend.perudo_backend.Dice;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {
    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private Map<String, Game> games = new ConcurrentHashMap<>();
    private static final int MAX_PLAYERS = 6;
    private static final int STARTING_DICE = 5;

    // Store challenge results temporarily for retrieval
    private Map<String, ChallengeResultDTO> challengeResults = new ConcurrentHashMap<>();
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private GameRecordRepository gameRecordRepository;

    @PostConstruct
    public void init() {
        games = new ConcurrentHashMap<>();
    }

    public GameStateDTO createGame() {
        Game game = new Game();
        String gameId = UUID.randomUUID().toString();
        game.setGameId(gameId);
        game.setStatus(GameStatus.WAITING);
        game.setPlayers(new ArrayList<>());
        game.setName("Game-" + gameId.substring(0, 8)); // Add a name for the game
        
        // Debug logging
        System.out.println("Creating new game with ID: " + gameId);
        
        // Store game in map
        games.put(gameId, game);
        
        // Create DTO with the game ID
        GameStateDTO dto = new GameStateDTO(game);
        
        // Verify DTO has correct ID
        if (dto.getId() == null || dto.getId().equals("null")) {
            throw new IllegalStateException("Failed to create game - DTO has invalid ID");
        }
        
        System.out.println("Successfully created game. DTO: " + dto);
        return dto;
    }

    public GameStateDTO joinGame(String gameId, String playerIdStr) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        
        if (game.getPlayers().size() >= MAX_PLAYERS) {
            throw new GameFullException();
        }

        Long pId;
        try {
            pId = Long.parseLong(playerIdStr);
        } catch (NumberFormatException e) {
            log.error("Invalid player ID format: {}", playerIdStr, e);
            throw new IllegalArgumentException("Invalid player ID format: " + playerIdStr);
        }

        // Fetch the persistent player object from the database
        Player persistentPlayer = playerRepository.findById(pId)
            .orElseThrow(() -> new PlayerNotFoundException(pId)); // Throws if player doesn't exist

        persistentPlayer.resetCurrentGameStats(); // Reset stats for the new game

        // Vérifier si un joueur avec cet ID est déjà dans la partie
        boolean alreadyJoined = game.getPlayers().stream()
                                    .anyMatch(p -> p.getId() != null && p.getId().equals(persistentPlayer.getId()));
        if (alreadyJoined) {
            log.warn("Player {} ({}) is already in game {}. Not adding again.", 
                persistentPlayer.getId(), persistentPlayer.getUsername(), gameId);
            // Renvoyer l'état actuel sans ajouter le joueur à nouveau
            return new GameStateDTO(game, playerIdStr);
        }
        
        log.info("Adding player {} ({}) to game: {}", persistentPlayer.getId(), persistentPlayer.getUsername(), gameId);
        
        // Ajouter le joueur (chargé depuis la DB) au jeu
        game.addPlayer(persistentPlayer);
        
        // Loguer l'état du jeu après ajout
        log.info("Game state after player {} joined: ID={}, Players={}, CurrentPlayer={}", 
            persistentPlayer.getUsername(),
            gameId, 
            game.getPlayers().size(),
            game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() : "none");
            
        return new GameStateDTO(game, playerIdStr);
    }

    public GameStateDTO getGameState(String gameId) {
        Game game = findById(gameId);
        return new GameStateDTO(game, null);
    }

    public GameStateDTO startGame(String gameId) {
        Game game = findById(gameId);

        if (game.getPlayers().size() < 2) {
            throw new NotEnoughPlayersException("Not enough players to start the game. Minimum required: 2");
        }

        // Store a copy of the players list at the start of the game for end-game stats
        game.setOriginalPlayers(new ArrayList<>(game.getPlayers()));
        // Ensure stats are reset for all players in originalPlayers list (and consequently in game.getPlayers() as they are same objects)
        for (Player player : game.getOriginalPlayers()) {
            player.resetCurrentGameStats();
        }

        game.initializeTurnSequence();
        log.info("Game {} started. Initial turn sequence: {}", gameId, 
            game.getTurnSequence().stream().map(p -> p.getId() + "(" + p.getUsername() + ")").collect(Collectors.toList()));
        log.info("Current player after init: {}", game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() + "(" + game.getCurrentPlayer().getUsername() + ")" : "null");

        game.setStatus(GameStatus.ROLLING);
        game.getPlayers().forEach(player -> player.setHasRolled(false));

        log.info("Game {} status set to ROLLING. Players reset for rolling.", gameId);
        
        GameStateDTO gameState = new GameStateDTO(game);
        log.info("startGame returning GameStateDTO with currentPlayerId: {}", gameState.getCurrentPlayerId());
        return gameState;
    }

    public GameStateDTO handleRoll(String gameId, String playerIdStr) {
        log.info("Handling roll for game: {} and player: {}", gameId, playerIdStr);
        Game game = findById(gameId);
        Long pId = Long.parseLong(playerIdStr);

        Player player = game.getPlayers().stream()
            .filter(p -> p.getId().equals(pId))
            .findFirst()
            .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + playerIdStr));

        // Simuler un lancer de dés (chaque joueur commence avec STARTING_DICE)
        List<Dice> newDice = new ArrayList<>();
        Random random = new Random();
        int numDiceForPlayer = player.getDiceCount() > 0 ? player.getDiceCount() : STARTING_DICE;
        for (int i = 0; i < numDiceForPlayer; i++) {
            Dice dice = new Dice(); // Assurez-vous que la classe Dice est bien disponible et instanciable
            dice.setValue(random.nextInt(6) + 1);
            // dice.setPlayer(player); // La relation est souvent gérée par JPA ou manuellement si besoin
            newDice.add(dice);
        }
        player.setDice(newDice);
        player.setHasRolled(true);
        log.info("Player {} in game {} rolled {} dice: {}", playerIdStr, gameId, numDiceForPlayer, newDice.stream().map(Dice::getValue).collect(Collectors.toList()));

        boolean allRolled = game.getPlayers().stream().allMatch(Player::getHasRolled);
        if (allRolled) {
            game.setStatus(GameStatus.PLAYING);
            log.info("All players in game {} have rolled. Game status set to PLAYING.", gameId);
            // Il est crucial que currentPlayer soit défini ici si ce n'est pas déjà fait par initializeTurnSequence.
            // Normalement, initializeTurnSequence dans startGame l'a déjà fait.
            if (game.getCurrentPlayer() == null && !game.getTurnSequence().isEmpty()) {
                game.setCurrentPlayer(game.getTurnSequence().get(0));
                 log.warn("Current player was null after all rolled, set to first in turn sequence: {}", game.getCurrentPlayer().getId());
            }
        }
        GameStateDTO gameState = new GameStateDTO(game, playerIdStr);
        log.info("handleRoll returning GameStateDTO with currentPlayerId: {}", gameState.getCurrentPlayerId());
        return gameState;
    }

    private List<Dice> createDiceForPlayer(int count, Player player) {
        List<Dice> diceList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            Dice die = new Dice();
            die.setValue(random.nextInt(6) + 1);
            die.setPlayer(player);
            diceList.add(die);
        }
        return diceList;
    }

    private void rollDiceForPlayer(Player player) {
        if (player == null) return;
        
        // Get current dice count or use STARTING_DICE for new players
        int diceCount = player.getDice() != null ? player.getDice().size() : STARTING_DICE;
        
        // Clear existing dice
        if (player.getDice() != null) {
            player.getDice().clear();
        }
        
        // Create and set new dice
        List<Dice> newDice = createDiceForPlayer(diceCount, player);
        player.setDice(newDice);
    }

    public GameStateDTO processBid(String gameId, String playerId, int quantity, int value) {
        Game game = findById(gameId);
        Player placingPlayer = game.getPlayers().stream()
                                 .filter(p -> String.valueOf(p.getId()).equals(playerId))
                                 .findFirst()
                                 .orElseThrow(() -> new PlayerNotFoundException("Player " + playerId + " not found in game " + gameId));

        log.info("processBid: GameId='{}'. Attempting bid by PlayerId='{}' ({}). Current Game Turn: PlayerId='{}' ({}). Current Game Bid: {}", 
            gameId, 
            placingPlayer.getId(), 
            placingPlayer.getUsername(),
            game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() : "null",
            game.getCurrentPlayer() != null ? game.getCurrentPlayer().getUsername() : "N/A",
            game.getCurrentBid() != null ? "Quantity: " + game.getCurrentBid().getQuantity() + ", Value: " + game.getCurrentBid().getValue() : "None");

        if (game.getCurrentPlayer() == null || !game.getCurrentPlayer().getId().equals(placingPlayer.getId())) {
            log.error("processBid: NOT PLAYER'S TURN. GameId='{}'. Expected Turn: PlayerId='{}'. Received Bid From: PlayerId='{}'.", 
                gameId, 
                game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() : "null",
                playerId);
            throw new NotYourTurnException();
        }

        Bid previousBid = game.getCurrentBid();
        if (!validateBid(previousBid, quantity, value)) {
            throw new IllegalArgumentException("Mise invalide : elle doit être strictement supérieure à la précédente (quantité ou valeur), ou valeur entre 1 et 6." );
        }

        Bid bid = new Bid(playerId, quantity, value);
        game.setCurrentBid(bid);
        log.info("Player {} ({}) placed bid: {}x{}. Current player before moveToNextPlayer: {} ({})", 
            placingPlayer.getId(), placingPlayer.getUsername(), quantity, value, 
            game.getCurrentPlayer().getId(), game.getCurrentPlayer().getUsername());

        game.moveToNextPlayer();
        log.info("After moveToNextPlayer. New current player: {} ({})", 
            game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() : "null",
            game.getCurrentPlayer() != null ? game.getCurrentPlayer().getUsername() : "null");
            
        GameStateDTO gameState = new GameStateDTO(game, playerId);
        log.info("processBid returning GameStateDTO with currentPlayerId: {}", gameState.getCurrentPlayerId());
        return gameState;
    }

    /**
     * Valide la mise selon les règles classiques du Perudo :
     * - Si pas de mise précédente, toute mise est valide
     * - Sinon, la nouvelle mise doit être strictement supérieure à la précédente
     *   (soit quantité supérieure, soit même quantité mais valeur supérieure)
     */
    private boolean validateBid(Bid previousBid, int newQuantity, int newValue) {
        if (previousBid == null) {
            return true; // Première mise toujours valide
        }
        int prevQuantity = previousBid.getQuantity();
        int prevValue = previousBid.getValue();
        // La nouvelle mise doit être strictement supérieure
        if (newQuantity > prevQuantity) {
            return true;
        } else if (newQuantity == prevQuantity && newValue > prevValue) {
            return true;
        }
        return false;
    }

    public GameStateDTO processChallenge(String gameId, String challengerId) {
        Game game = findById(gameId);
        
        if (game.getCurrentBid() == null) {
            throw new IllegalStateException("No bid to challenge");
        }
        
        // Get the challenger
        Player challenger = game.getPlayers().stream()
            .filter(p -> String.valueOf(p.getId()).equals(challengerId))
            .findFirst()
            .orElseThrow(() -> new PlayerNotFoundException("Challenger not found: " + challengerId));
            
        // Get the player who made the bid
        String bidPlayerId = game.getCurrentBid().getPlayerId();
        Player bidPlayer = game.getPlayers().stream()
            .filter(p -> String.valueOf(p.getId()).equals(bidPlayerId))
            .findFirst()
            .orElseThrow(() -> new PlayerNotFoundException("Bid player not found: " + bidPlayerId));

        log.info("Challenge initiated by {} against {}'s bid: {}x{}",
            challenger.getUsername(), bidPlayer.getUsername(),
            game.getCurrentBid().getQuantity(), game.getCurrentBid().getValue());

        // Reveal all dice and count BEFORE making any changes
        ChallengeResultDTO challengeResult = resolveBidChallenge(game, challenger, bidPlayer);
        
        // Store challenge result for retrieval
        challengeResults.put(gameId, challengeResult);
        
        // Update player dice counts based on challenge result
        updateDiceCountsAfterChallenge(game, challengeResult);

        // Do NOT start new round here - let the frontend handle it after showing results
        // The new round will be started by a separate call after the challenge modal is closed

        return new GameStateDTO(game, challengerId);
    }

    public ChallengeResultDTO getChallengeResult(String gameId) {
        return challengeResults.get(gameId);
    }

    public void clearChallengeResult(String gameId) {
        challengeResults.remove(gameId);
    }

    private ChallengeResultDTO resolveBidChallenge(Game game, Player challenger, Player bidPlayer) {
        Bid currentBid = game.getCurrentBid();
        
        // Track challenge attempt for challenger
        challenger.setCurrentGameChallenges(challenger.getCurrentGameChallenges() + 1);
        
        // Collect all dice from all players
        Map<String, List<Integer>> allPlayerDice = new HashMap<>();
        int totalDiceOfValue = 0;
        
        for (Player player : game.getPlayers()) {
            if (player.getDice() != null) {
                List<Integer> playerDiceValues = player.getDice().stream()
                    .map(Dice::getValue)
                    .collect(Collectors.toList());
                allPlayerDice.put(String.valueOf(player.getId()), playerDiceValues);
                
                // Count dice that match the bid (including 1s as wild cards, unless the bid is for 1s)
                for (Dice die : player.getDice()) {
                    if (currentBid.getValue() == 1) {
                        // If bidding on 1s, only 1s count (no wild cards)
                        if (die.getValue() == 1) {
                            totalDiceOfValue++;
                        }
                    } else {
                        // If bidding on any other value, count that value plus 1s (wild)
                        if (die.getValue() == currentBid.getValue() || die.getValue() == 1) {
                            totalDiceOfValue++;
                        }
                    }
                }
            }
        }

        // Challenge is successful if actual count is DIFFERENT from bid quantity
        boolean challengeSuccessful = totalDiceOfValue != currentBid.getQuantity();
        
        // Track successful challenge for challenger
        if (challengeSuccessful) {
            challenger.setCurrentGameSuccessfulChallenges(challenger.getCurrentGameSuccessfulChallenges() + 1);
        }
        
        // Determine who loses a die
        Player losingPlayer = challengeSuccessful ? bidPlayer : challenger;
        
        // Track elimination by the one who "won" the challenge round
        if (losingPlayer.getDice() != null && losingPlayer.getDice().size() <= 1) { // check if the losing player will be eliminated
            if (challengeSuccessful) { // Challenger won, bidPlayer lost and is eliminated
                challenger.setCurrentGameEliminatedPlayers(challenger.getCurrentGameEliminatedPlayers() + 1);
            } else { // Challenger lost (and is eliminated), bidPlayer won the challenge round
                bidPlayer.setCurrentGameEliminatedPlayers(bidPlayer.getCurrentGameEliminatedPlayers() + 1);
            }
        }
        
        log.info("Challenge result: Bid was {}x{}, actual count: {}, challenge {}",
            currentBid.getQuantity(), currentBid.getValue(), totalDiceOfValue,
            challengeSuccessful ? "SUCCESSFUL" : "FAILED");
        log.info("Player {} loses a die", losingPlayer.getUsername());

        // Check if game will be finished
        boolean gameFinished = false;
        String winnerId = null;
        String winnerName = null;
        
        // Simulate removing a die to check if player would be eliminated
        int losingPlayerDiceCount = losingPlayer.getDice() != null ? losingPlayer.getDice().size() : 0;
        if (losingPlayerDiceCount <= 1) {
            // This player will be eliminated, check if game ends
            long remainingPlayersWithDice = game.getPlayers().stream()
                .filter(p -> !p.equals(losingPlayer))
                .filter(p -> p.getDice() != null && p.getDice().size() > 0)
                .count();
            
            if (remainingPlayersWithDice <= 1) {
                gameFinished = true;
                // Find the winner
                Player winner = game.getPlayers().stream()
                    .filter(p -> !p.equals(losingPlayer))
                    .filter(p -> p.getDice() != null && p.getDice().size() > 0)
                    .findFirst()
                    .orElse(null);
                
                if (winner != null) {
                    winnerId = String.valueOf(winner.getId());
                    winnerName = winner.getUsername();
                }
            }
        }

        return new ChallengeResultDTO(
            String.valueOf(challenger.getId()),
            challenger.getUsername(),
            String.valueOf(bidPlayer.getId()),
            bidPlayer.getUsername(),
            new BidDTO(currentBid),
            totalDiceOfValue,
            challengeSuccessful,
            String.valueOf(losingPlayer.getId()),
            losingPlayer.getUsername(),
            allPlayerDice,
            gameFinished,
            winnerId,
            winnerName
        );
    }

    private void updateDiceCountsAfterChallenge(Game game, ChallengeResultDTO challengeResult) {
        Player losingPlayer = game.getPlayers().stream()
            .filter(p -> String.valueOf(p.getId()).equals(challengeResult.getLosingPlayerId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Losing player not found"));

        // Remove one die from the losing player
        List<Dice> currentDice = losingPlayer.getDice();
        if (currentDice != null && !currentDice.isEmpty()) {
            Dice dieToRemove = currentDice.get(currentDice.size() - 1);
            currentDice.remove(dieToRemove);
            losingPlayer.setDice(currentDice);
            log.info("Removed one die from {}. Remaining dice: {}", 
                losingPlayer.getUsername(), currentDice.size());
        }

        // Check if player is eliminated
        if (currentDice == null || currentDice.isEmpty()) {
            game.removePlayer(losingPlayer);
            log.info("Player {} has been eliminated from game {}.", 
                losingPlayer.getUsername(), game.getGameId());
        }

        // Check if game is over
        if (challengeResult.isGameFinished()) {
            game.setStatus(GameStatus.FINISHED);
            if (challengeResult.getWinnerId() != null) {
                Player winner = game.getPlayers().stream()
                    .filter(p -> String.valueOf(p.getId()).equals(challengeResult.getWinnerId()))
                    .findFirst()
                    .orElse(null);
                game.setWinner(winner);
                log.info("Game {} finished. Winner is {}.", 
                    game.getGameId(), challengeResult.getWinnerName());
            }
        }
    }

    // New method to start the next round after challenge results are shown
    public GameStateDTO startNextRoundAfterChallenge(String gameId, String losingPlayerId) {
        Game game = findById(gameId);
        
        if (game.getStatus() == GameStatus.FINISHED) {
            return new GameStateDTO(game);
        }

        game.setRound(game.getRound() + 1);
        game.setCurrentBid(null);

        // Roll new dice for all remaining players
        game.getPlayers().forEach(this::rollDiceForPlayer);

        // The losing player starts the next round (if they're still in the game)
        Player nextStartingPlayer = game.getPlayers().stream()
            .filter(p -> String.valueOf(p.getId()).equals(losingPlayerId))
            .findFirst()
            .orElse(null);
        
        // If the losing player was eliminated, find the next player in sequence
        if (nextStartingPlayer == null && !game.getTurnSequence().isEmpty()) {
            // Find the next player after the eliminated player
            nextStartingPlayer = game.getTurnSequence().get(0); // Fallback to first player
        }

        if (nextStartingPlayer != null) {
            game.setCurrentPlayer(nextStartingPlayer);
            log.info("New round started. {} begins the round.", nextStartingPlayer.getUsername());
        }

        return new GameStateDTO(game);
    }

    public Game findById(String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        return game;
    }

    public Game getGame(String gameId) {
        if (gameId == null || gameId.trim().isEmpty()) {
            throw new IllegalArgumentException("Game ID cannot be null or empty");
        }

        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }

        return game;
    }

    public Player getPlayer(String gameId, String playerId) {
        Game game = findById(gameId);
        return game.getPlayers().stream()
            .filter(p -> String.valueOf(p.getId()).equals(playerId))
            .findFirst()
            .orElseThrow(() -> new perudo_backend.exception.PlayerNotFoundException(playerId));
    }

    // Ajouter cette méthode de débogage 
    public String debugGameState(String gameId) {
        Game game = findById(gameId);
        StringBuilder sb = new StringBuilder();
        sb.append("Game Debug Info:\n");
        sb.append("Game ID: ").append(game.getGameId()).append("\n");
        sb.append("Status: ").append(game.getStatus()).append("\n");
        sb.append("Players (").append(game.getPlayers().size()).append("):\n");
        
        for (Player p : game.getPlayers()) {
            sb.append("  - ID: ").append(p.getId())
              .append(", Username: ").append(p.getUsername())
              .append(", CurrentTurn: ").append(p.isCurrentTurn())
              .append(", Dice: ").append(p.getDice().size())
              .append("\n");
        }
        
        sb.append("Current Player: ");
        if (game.getCurrentPlayer() != null) {
            sb.append("ID=").append(game.getCurrentPlayer().getId())
              .append(", Username=").append(game.getCurrentPlayer().getUsername())
              .append(", CurrentTurn=").append(game.getCurrentPlayer().isCurrentTurn());
        } else {
            sb.append("null");
        }
        sb.append("\n");
        
        sb.append("Turn Sequence (").append(game.getTurnSequence().size()).append("):\n");
        for (Player p : game.getTurnSequence()) {
            sb.append("  - ID: ").append(p.getId())
              .append(", Username: ").append(p.getUsername())
              .append(", CurrentTurn: ").append(p.isCurrentTurn())
              .append("\n");
        }
        
        return sb.toString();
    }
    
    // --- GAME END RESULTS AND SCORING SYSTEM ---
    
    @Transactional
    public GameEndResultDTO calculateGameEndResults(String gameId) {
        Game game = findById(gameId);
        
        if (game.getStatus() != GameStatus.FINISHED) {
            log.warn("Trying to calculate end results for unfinished game: {}", gameId);
            return null;
        }

        // Diagnostic: Log all players in DB at the start of this method
        try {
            List<Player> allPlayersInDB = playerRepository.findAll();
            log.info("[GameEnd] All players in DB at start of calculateGameEndResults for game {}: {}",
                gameId, allPlayersInDB.stream().map(p -> "ID=" + p.getId() + ",User=" + p.getUsername()).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("[GameEnd] Error trying to list all players from DB at start of calculateGameEndResults for game {}: {}", gameId, e.getMessage());
        }

        List<Player> playersAtGameStart = game.getOriginalPlayers();
        if (playersAtGameStart == null || playersAtGameStart.isEmpty()) {
            log.error("Original players list is empty for game {}. Cannot calculate results.", gameId);
            // Potentially return an error DTO or throw an exception
            return new GameEndResultDTO(gameId, true, new ArrayList<>()); // Or some other error indication
        }
        
        // Detect if this was a 2-player game
        boolean isTwoPlayerGame = playersAtGameStart.size() == 2;
        log.info("Game {} ended with {} starting players. Two-player special rules: {}", gameId, playersAtGameStart.size(), isTwoPlayerGame);
        
        // Create ranked list of players based on elimination order and current state in originalPlayers
        List<Player> rankedPlayers = createPlayerRanking(playersAtGameStart, game.getWinner(), game);
        
        // Calculate points and coins for each player
        List<GameEndResultDTO.PlayerGameResult> playerResults = new ArrayList<>();
        
        for (int i = 0; i < rankedPlayers.size(); i++) {
            Player originalPlayerInstance = rankedPlayers.get(i);
            
            // Fetch the managed entity from DB to persist changes
            log.info("[GameEnd] Processing player from originalPlayers list for ranking/stats: ID={}, Username={}. Attempting to fetch from DB.", 
                originalPlayerInstance.getId(), originalPlayerInstance.getUsername());

            Player managedPlayer = playerRepository.findById(originalPlayerInstance.getId())
                .orElseThrow(() -> {
                    log.error("[GameEnd] PlayerNotFoundException: Could not find player with id {} in repository while calculating game end results. Original instance from game.originalPlayers: ID={}, Username={}", 
                        originalPlayerInstance.getId(), originalPlayerInstance.getId(), originalPlayerInstance.getUsername());
                    return new PlayerNotFoundException(originalPlayerInstance.getId());
                });

            // Copy the accumulated transient stats from the originalPlayerInstance (from game.getOriginalPlayers())
            // to the managedPlayer entity that will be saved.
            managedPlayer.setCurrentGameChallenges(originalPlayerInstance.getCurrentGameChallenges());
            managedPlayer.setCurrentGameSuccessfulChallenges(originalPlayerInstance.getCurrentGameSuccessfulChallenges());
            managedPlayer.setCurrentGameEliminatedPlayers(originalPlayerInstance.getCurrentGameEliminatedPlayers());
            // finalPosition will be set by ranking logic, or can be set here
            managedPlayer.setFinalPosition(i + 1); 

            int position = i + 1;
            
            int pointsEarned, coinsEarned, trophiesEarned;
            
            if (isTwoPlayerGame) {
                // Special rules for 2-player games
                if (position == 1) {
                    pointsEarned = 50; 
                    coinsEarned = 100;
                    trophiesEarned = 1;
                } else {
                    pointsEarned = 0;
                    coinsEarned = 0;
                    trophiesEarned = 0;
                }
            } else {
                // Pass the managedPlayer (which now has the correct current game stats) to calculatePointsEarned
                pointsEarned = calculatePointsEarned(managedPlayer, position, rankedPlayers.size(), game);
                coinsEarned = calculateCoinsEarned(position);
                trophiesEarned = (position == 1) ? 1 : 0; 
            }
            
            String performanceMessage = generatePerformanceMessage(managedPlayer, position, isTwoPlayerGame, game);
            // getDiceCountFromGame should ideally use the player instance from the game object if still active,
            // or rely on a snapshot if player was eliminated. For simplicity, let's assume originalPlayerInstance's dice count is final.
            // However, dice are managed on Player objects within the Game's `players` list primarily.
            // The `getDiceCountFromGame` method already fetches from `game.getPlayers()` or `game.getOriginalPlayers()` effectively.
            int diceCount = getDiceCountFromGame(game, String.valueOf(managedPlayer.getId())); 
            
            GameEndResultDTO.PlayerGameResult result = new GameEndResultDTO.PlayerGameResult(
                managedPlayer.getId(),
                managedPlayer.getUsername(),
                position,
                pointsEarned,
                coinsEarned,
                managedPlayer.getCurrentGameChallenges(), // Use stats from managedPlayer
                managedPlayer.getCurrentGameSuccessfulChallenges(),
                managedPlayer.getCurrentGameEliminatedPlayers(),
                diceCount
            );
            result.setPerformanceMessage(performanceMessage);
            playerResults.add(result);
            
            // Update and save the managedPlayer's overall stats
            updatePlayerStatsAfterGame(managedPlayer, position, pointsEarned, coinsEarned, trophiesEarned);
            
            // Créer un GameRecord pour ce match (seulement s'il n'existe pas déjà)
            boolean won = (position == 1);
            try {
                // Vérifier si un GameRecord existe déjà pour ce joueur et ce match
                boolean recordExists = gameRecordRepository.existsByPlayerIdAndGameId(managedPlayer.getId(), gameId);
                
                if (!recordExists) {
                    GameRecord gameRecord = new GameRecord(managedPlayer, gameId, LocalDateTime.now(), won, pointsEarned);
                    gameRecordRepository.save(gameRecord);
                    
                    // Ajouter le GameRecord au joueur (pour maintenir la relation bidirectionnelle)
                    managedPlayer.addGameRecord(gameRecord);
                    
                    log.info("Created GameRecord for {} in game {}: Won={}, ScoreChange={}", 
                        managedPlayer.getUsername(), gameId, won, pointsEarned);
                } else {
                    log.info("GameRecord already exists for {} in game {}, skipping creation", 
                        managedPlayer.getUsername(), gameId);
                }
            } catch (Exception e) {
                log.error("Error creating GameRecord for {} in game {}: {}", 
                    managedPlayer.getUsername(), gameId, e.getMessage());
                // Ne pas faire échouer le traitement complet pour une erreur de GameRecord
            }
        }
        
        log.info("Game {} ended. Results calculated for {} players", gameId, playerResults.size());
        return new GameEndResultDTO(gameId, true, playerResults);
    }
    
    /**
     * Create player ranking based on elimination order and final dice count
     */
    private List<Player> createPlayerRanking(List<Player> playersToRank, Player winner, Game game) {
        Set<Long> processedPlayerIdsForRanking = new HashSet<>();
        List<Player> ranking = new ArrayList<>();

        // playersToRank are the originalPlayer instances with their final game stats
        Map<Long, Player> distinctPlayerMap = playersToRank.stream()
            .filter(p -> p != null && p.getId() != null)
            .collect(Collectors.toMap(
                Player::getId,
                p -> p,
                (existing, replacement) -> existing 
            ));

        // Winner is always first
        if (winner != null) {
            Player winnerInstance = distinctPlayerMap.get(winner.getId());
            if (winnerInstance != null && processedPlayerIdsForRanking.add(winnerInstance.getId())) { 
                ranking.add(winnerInstance);
            }
        }
        
        List<Player> remainingPlayers = playersToRank.stream()
            .filter(p -> winner == null || !p.getId().equals(winner.getId())) 
            .filter(p -> p != null && p.getId() != null && !processedPlayerIdsForRanking.contains(p.getId()))
            .sorted((p1, p2) -> {
                // p1 and p2 are instances from playersToRank (originalPlayers)
                int dice1 = getDiceCountFromGame(game, String.valueOf(p1.getId())); // Reflects final dice count
                int dice2 = getDiceCountFromGame(game, String.valueOf(p2.getId()));
                if (dice1 != dice2) {
                    return Integer.compare(dice2, dice1); // More dice = better rank
                }

                // Use the current game stats directly from p1 and p2 (which are from originalPlayers)
                int activity1 = p1.getCurrentGameSuccessfulChallenges() + p1.getCurrentGameEliminatedPlayers();
                int activity2 = p2.getCurrentGameSuccessfulChallenges() + p2.getCurrentGameEliminatedPlayers();
                if (activity1 != activity2) {
                    return Integer.compare(activity2, activity1); // More activity = better rank
                }
                return p1.getId().compareTo(p2.getId()); // Fallback for deterministic order
            })
            .collect(Collectors.toList());
            
        for (Player p : remainingPlayers) {
            if (processedPlayerIdsForRanking.add(p.getId())) {
                ranking.add(p);
            }
        }
        
        log.info("Player ranking created. Size: {}. Players: {}", 
            ranking.size(), 
            ranking.stream().map(p -> "ID: " + p.getId() + " User: " + p.getUsername() + " Dice: " + getDiceCountFromGame(game, String.valueOf(p.getId())) + " Act: "+ (p.getCurrentGameSuccessfulChallenges() + p.getCurrentGameEliminatedPlayers())).collect(Collectors.toList()));

        return ranking;
    }
    
    /**
     * Calculate points earned based on performance and position
     */
    private int calculatePointsEarned(Player player, int position, int totalPlayers, Game game) {
        int points = 0;
        
        // Base points for position
        if (position == 1) {
            points += 100; // Winner bonus
        } else if (position == 2) {
            points += 50;  // Second place bonus
        } else if (position == 3) {
            points += 25;  // Third place bonus
        }
        
        // Activity bonuses
        points += player.getCurrentGameSuccessfulChallenges() * 15; // 15 points per successful challenge
        points += player.getCurrentGameEliminatedPlayers() * 25;    // 25 points per player eliminated
        
        // Dice remaining bonus (only if not last place)
        if (position < totalPlayers) {
            int diceCount = getDiceCountFromGame(game, String.valueOf(player.getId()));
            points += diceCount * 5; // 5 points per remaining die
        }
        
        // Challenge accuracy bonus
        if (player.getCurrentGameChallenges() > 0) {
            double accuracy = (double) player.getCurrentGameSuccessfulChallenges() / player.getCurrentGameChallenges();
            if (accuracy >= 0.75) {
                points += 20; // High accuracy bonus
            } else if (accuracy >= 0.5) {
                points += 10; // Good accuracy bonus
            }
        }
        
        // Penalty for losing (but with compensation for active players)
        if (position > 1) {
            int penalty = Math.min(50, (position - 1) * 10); // Max 50 point penalty
            
            // Compensation for active players
            int activityScore = player.getCurrentGameSuccessfulChallenges() + player.getCurrentGameEliminatedPlayers();
            int compensation = Math.min(penalty, activityScore * 15); // Can offset penalty with activity
            
            points -= (penalty - compensation);
        }
        
        // Minimum points (players don't go negative unless they were completely inactive)
        if (player.getCurrentGameChallenges() > 0 || player.getCurrentGameEliminatedPlayers() > 0) {
            points = Math.max(5, points); // Active players get at least 5 points
        } else if (position == totalPlayers) {
            points = Math.max(-20, points); // Inactive last place can lose up to 20 points
        } else {
            points = Math.max(0, points); // Others don't go negative
        }
        
        return points;
    }
    
    /**
     * Calculate coins earned based on position
     */
    private int calculateCoinsEarned(int position) {
        switch (position) {
            case 1: return 200; // 1st place
            case 2: return 100; // 2nd place
            case 3: return 50;  // 3rd place
            default: return 0;  // No coins for 4th place and below
        }
    }
    
    /**
     * Generate performance message for player
     */
    private String generatePerformanceMessage(Player player, int position, boolean isTwoPlayerGame, Game game) {
        StringBuilder message = new StringBuilder();
        
        if (isTwoPlayerGame) {
            if (position == 1) {
                message.append("🏆 Victoire en duel ! ");
            } else {
                message.append("💔 Défaite en duel... ");
            }
        } else {
            if (position == 1) {
                message.append("🏆 Victoire ! ");
            } else if (position == 2) {
                message.append("🥈 Excellent ! ");
            } else if (position == 3) {
                message.append("🥉 Bien joué ! ");
            } else {
                message.append("Défaite... ");
            }
        }
        
        // Add activity highlights
        if (player.getCurrentGameSuccessfulChallenges() > 2) {
            message.append("Maître du challenge ! ");
        } else if (player.getCurrentGameSuccessfulChallenges() > 0) {
            message.append("Bon challengeur. ");
        }
        
        if (player.getCurrentGameEliminatedPlayers() > 1) {
            message.append("Éliminateur redoutable ! ");
        } else if (player.getCurrentGameEliminatedPlayers() > 0) {
            message.append("Un joueur éliminé. ");
        }
        
        int diceCount = getDiceCountFromGame(game, String.valueOf(player.getId()));
        if (diceCount >= 3 && position > 1) {
            message.append("Bonne survie !");
        }
        
        return message.toString().trim();
    }
    
    /**
     * Update player's persistent statistics after game
     */
    private void updatePlayerStatsAfterGame(Player player, int position, int pointsEarned, int coinsEarned, int trophiesEarned) {
        // Update game counts
        player.setGamesPlayed(player.getGamesPlayed() + 1);
        boolean won = (position == 1);
        if (won) {
            player.setGamesWon(player.getGamesWon() + 1);
        }
        
        player.setTrophies(player.getTrophies() + trophiesEarned);
        player.setTotalChallenges(player.getTotalChallenges() + player.getCurrentGameChallenges());
        player.setSuccessfulChallenges(player.getSuccessfulChallenges() + player.getCurrentGameSuccessfulChallenges());
        player.setPlayersEliminated(player.getPlayersEliminated() + player.getCurrentGameEliminatedPlayers());
        player.setPoints(player.getPoints() + pointsEarned);
        player.setPieces(player.getPieces() + coinsEarned);
        
        if (player.getGamesPlayed() > 0) {
            double winRate = (double) player.getGamesWon() / player.getGamesPlayed() * 100;
            player.setWinRate(Math.round(winRate * 100.0) / 100.0);
        }
        
        player.setCurrentGameChallenges(0);
        player.setCurrentGameSuccessfulChallenges(0);
        player.setCurrentGameEliminatedPlayers(0);
        player.setFinalPosition(0);

        // Correctly handle orphanRemoval for dice collection
        if (player.getDice() != null) { // Check if the collection exists (it should)
            // Hibernate.initialize(player.getDice()); // Optionnel: forcer le chargement si LAZY et non déjà chargé
            player.getDice().clear(); // Vide la collection, Hibernate s'occupera de supprimer les orphelins
        }
        player.setGame(null); 
        
        playerRepository.save(player);
        
        log.info("Updated stats for {}: Games {}/{}, Points: {}, Coins: {}, Trophies: {}", 
            player.getUsername(), player.getGamesWon(), player.getGamesPlayed(),
            player.getPoints(), player.getPieces(), player.getTrophies());
    }

    private int getDiceCountFromGame(Game game, String playerId) {
        Long pId = Long.parseLong(playerId);
        // First, check active players in the game
        Player playerInGame = game.getPlayers().stream()
                                .filter(p -> p.getId().equals(pId))
                                .findFirst().orElse(null);
        if (playerInGame != null) {
            return playerInGame.getDiceCount();
        }
        // If not in active players (eliminated), check original players list for their last state
        // This part is tricky if originalPlayers list isn't updated with dice counts upon elimination.
        // For now, assume if they are not in game.getPlayers(), their dice count is 0 for ranking purposes (they were eliminated).
        // The ChallengeResultDTO handles showing dice at the moment of challenge.
        // For final ranking, 0 dice if eliminated is usually correct.
        return 0; 
    }
}