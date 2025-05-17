package perudo_backend.perudo_backend.services;

import org.springframework.stereotype.Service;

import perudo_backend.exception.GameFullException;
import perudo_backend.exception.GameNotFoundException;
import perudo_backend.exception.NotEnoughPlayersException;
import perudo_backend.exception.NotYourTurnException;
import perudo_backend.perudo_backend.Bid;
import perudo_backend.perudo_backend.Game;
import perudo_backend.perudo_backend.GameStatus;
import perudo_backend.perudo_backend.Player;
import perudo_backend.perudo_backend.dto.GameStateDTO;

import java.util.*;
import perudo_backend.perudo_backend.Dice;

@Service
public class GameService {
    private final Map<String, Game> games = new HashMap<>();
    private static final int MAX_PLAYERS = 6;
    private static final int STARTING_DICE = 5;

    public GameStateDTO createGame() {
        Game game = new Game();
        game.setGameId(UUID.randomUUID().toString());  // Use gameId instead of id
        game.setStatus(GameStatus.WAITING);
        games.put(game.getGameId(), game);  // Use gameId as map key
        return new GameStateDTO(game, null);
    }

    public GameStateDTO joinGame(String gameId, String playerId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        
        if (game.getPlayers().size() >= MAX_PLAYERS) {
            throw new GameFullException();
        }

        Player player = new Player();
        player.setId(Long.parseLong(playerId));
        game.addPlayer(player);
        return new GameStateDTO(game, playerId);
    }

    public GameStateDTO getGameState(String gameId) {
        Game game = findById(gameId);
        return new GameStateDTO(game, null);
    }

    public GameStateDTO startGame(String gameId) {
        Game game = findById(gameId);
        
        if (game.getPlayers().size() < 2) {
            throw new NotEnoughPlayersException();
        }

        // Initialize game
        game.setStatus(GameStatus.PLAYING);
        game.setRound(1);
        
        // Roll dice for all players
        game.getPlayers().forEach(this::rollDiceForPlayer);

        // Select random first player
        List<Player> playersList = new ArrayList<>(game.getPlayers());
        int randomIndex = new Random().nextInt(playersList.size());
        game.setCurrentPlayer(playersList.get(randomIndex));

        return new GameStateDTO(game, null);
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
        Player currentPlayer = game.getCurrentPlayer();
        
        if (currentPlayer == null) {
            throw new IllegalStateException("No current player set for game: " + gameId);
        }

        if (!String.valueOf(currentPlayer.getId()).equals(playerId)) {  // Use playerId instead of id
            throw new NotYourTurnException();
        }

        // Update game state with new bid
        Bid bid = new Bid(playerId, quantity, value);
        game.setCurrentBid(bid);
        game.moveToNextPlayer();

        return new GameStateDTO(game, playerId);
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
            game.getPlayers().remove(losingPlayer);
        }

        // Check if game is over (only one player left)
        if (game.getPlayers().size() == 1) {
            game.setStatus(GameStatus.FINISHED);
            // Safely set winner with null check
            List<Player> playersList = new ArrayList<>(game.getPlayers());
            Player winner = playersList.isEmpty() ? null : playersList.get(0);
            if (winner != null) {
                game.setWinner(winner);
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

    public Game getGame(String gameId) {
        // TODO: Implement logic to retrieve a Game by its ID
        // Example placeholder:
        throw new UnsupportedOperationException("getGame(String) not implemented yet");
    }
}