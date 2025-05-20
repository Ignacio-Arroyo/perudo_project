package perudo_backend.perudo_backend.services;

import org.springframework.stereotype.Service;

import perudo_backend.exception.GameFullException;
import perudo_backend.exception.GameNotFoundException;
import perudo_backend.exception.NotEnoughPlayersException;
import perudo_backend.exception.NotYourTurnException;
import perudo_backend.exception.PlayerNotFoundException;
import perudo_backend.perudo_backend.Bid;
import perudo_backend.perudo_backend.Game;
import perudo_backend.perudo_backend.GameStatus;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.dto.GameStateDTO;

import java.util.*;
import java.util.stream.Collectors;
import perudo_backend.perudo_backend.Dice;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GameService {
    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private Map<String, Game> games = new ConcurrentHashMap<>();
    private static final int MAX_PLAYERS = 6;
    private static final int STARTING_DICE = 5;

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

        // Vérifier si un joueur avec cet ID est déjà dans la partie
        boolean alreadyJoined = game.getPlayers().stream()
                                    .anyMatch(p -> p.getId() != null && p.getId().equals(pId));
        if (alreadyJoined) {
            log.warn("Player with ID {} is already in game {}. Not adding again.", pId, gameId);
            // Renvoyer l'état actuel sans ajouter le joueur à nouveau
            return new GameStateDTO(game, playerIdStr);
        }

        // Créer un nouveau joueur avec un nom d'utilisateur défini
        Player player = new Player();
        player.setId(pId);
        player.setUsername("Player " + pId); // Définir un nom d'utilisateur par défaut

        log.info("Adding player to game: ID={}, Username={}", pId, player.getUsername());
        
        // Ajouter le joueur au jeu
        game.addPlayer(player);
        
        // Loguer l'état du jeu après ajout
        log.info("Game state after player joined: ID={}, Players={}, CurrentPlayer={}", 
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

        log.info("processBid: Game: {}, Player making bid: {} ({}), Current game player: {} ({})", 
            gameId, placingPlayer.getId(), placingPlayer.getUsername(),
            game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() : "null",
            game.getCurrentPlayer() != null ? game.getCurrentPlayer().getUsername() : "null");

        if (game.getCurrentPlayer() == null || !game.getCurrentPlayer().getId().equals(placingPlayer.getId())) {
            log.error("Not player's turn. Current: {}, Attempted: {}", 
                game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() : "null", playerId);
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

    public GameStateDTO processChallenge(String gameId, String playerId) {
        Game game = findById(gameId);

        // Resolve challenge
        boolean challengeSuccessful = resolveBidChallenge(game);
        
        // Update player dice counts based on challenge result
        updateDiceCounts(game, challengeSuccessful);

        // Start new round
        startNewRound(game);

        return new GameStateDTO(game, playerId);
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

    private boolean resolveBidChallenge(Game game) {
        Bid currentBid = game.getCurrentBid();
        if (currentBid == null) {
            throw new IllegalStateException("No bid to challenge");
        }

        int totalDiceOfValue = 0;
        // Count all dice of the bid value and ones (wild)
        for (Player player : game.getPlayers()) {
            if (player.getDice() != null) {
                for (Dice die : player.getDice()) {
                    if (die.getValue() == currentBid.getValue() || die.getValue() == 1) {
                        totalDiceOfValue++;
                    }
                }
            }
        }

        return totalDiceOfValue < currentBid.getQuantity();
    }

    private void updateDiceCounts(Game game, boolean challengeSuccessful) {
        // Add null checks for currentBid
        Bid currentBid = game.getCurrentBid();
        if (currentBid == null) {
            throw new IllegalStateException("No current bid found for game");
        }

        String bidPlayerId = currentBid.getPlayerId();
        if (bidPlayerId == null) {
            throw new IllegalStateException("No player ID found for current bid");
        }

        Player challengedPlayer = game.getPlayers().stream()
            .filter(p -> p != null && String.valueOf(p.getId()).equals(bidPlayerId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Challenged player not found"));

        Player challengingPlayer = game.getCurrentPlayer();

        // Determine who loses a die
        Player losingPlayer = challengeSuccessful ? challengedPlayer : challengingPlayer;
        
        // Remove one die from the losing player
        List<Dice> currentDice = losingPlayer.getDice();
        if (currentDice != null && !currentDice.isEmpty()) {
            Dice dieToRemove = currentDice.get(currentDice.size() - 1);
            currentDice.remove(dieToRemove);
            losingPlayer.setDice(currentDice);
        }

        // Check if player is eliminated
        if (currentDice == null || currentDice.isEmpty()) {
            // Utiliser la méthode removePlayer de l'objet Game pour assurer la cohérence de la turnSequence
            game.removePlayer(losingPlayer);
            log.info("Player {} has been eliminated from game {}.", losingPlayer.getId(), game.getGameId());
        }

        // Check if game is over (only one player left or no active players in turn sequence)
        List<Player> activePlayers = game.getPlayers().stream()
                                        .filter(p -> p.getDiceCount() > 0)
                                        .collect(Collectors.toList());

        if (activePlayers.size() <= 1) {
            game.setStatus(GameStatus.FINISHED);
            Player winner = activePlayers.isEmpty() ? null : activePlayers.get(0);
            if (winner != null) {
                game.setWinner(winner);
                log.info("Game {} finished. Winner is Player {}.", game.getGameId(), winner.getId());
            } else {
                // Aucun gagnant si tous les joueurs sont éliminés en même temps (cas rare)
                // ou si la partie se termine sans joueurs actifs.
                game.setWinner(null); // Assure que winner est null
                log.info("Game {} finished. No winner, all players eliminated or no active players left.", game.getGameId());
            }
        }
    }

    private void startNewRound(Game game) {
        if (game.getStatus() == GameStatus.FINISHED) {
            return;
        }

        game.setRound(game.getRound() + 1);
        game.setCurrentBid(null);

        // Roll new dice for all players
        game.getPlayers().forEach(this::rollDiceForPlayer);

        // Safely move to next player with null checks
        if (game.getStatus() != GameStatus.FINISHED && !game.getPlayers().isEmpty()) {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null) {
                List<Player> playersList = new ArrayList<>(game.getPlayers());
                int currentIndex = playersList.indexOf(currentPlayer);
                if (currentIndex != -1) {
                    int nextIndex = (currentIndex + 1) % playersList.size();
                    Player nextPlayer = playersList.get(nextIndex);
                    if (nextPlayer != null) {
                        game.setCurrentPlayer(nextPlayer);
                    }
                }
            }
        }
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
}