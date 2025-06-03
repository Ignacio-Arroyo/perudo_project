package perudo_backend.perudo_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import perudo_backend.perudo_backend.GameRecord;
import perudo_backend.perudo_backend.dto.GameRecordDTO;
import perudo_backend.perudo_backend.repositories.GameRecordRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/match-history")
@CrossOrigin(origins = "http://localhost:3000")
public class MatchHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(MatchHistoryController.class);

    @Autowired
    private GameRecordRepository gameRecordRepository;

    /**
     * Récupérer l'historique complet des matchs pour un joueur
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<List<GameRecordDTO>> getPlayerMatchHistory(@PathVariable Long playerId) {
        logger.info("Getting match history for player {}", playerId);
        
        try {
            List<GameRecord> gameRecords = gameRecordRepository.findByPlayerIdOrderByPlayedAtDesc(playerId);
            
            List<GameRecordDTO> gameRecordDTOs = gameRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            logger.info("Found {} match records for player {}", gameRecordDTOs.size(), playerId);
            return ResponseEntity.ok(gameRecordDTOs);
            
        } catch (Exception e) {
            logger.error("Error retrieving match history for player {}: {}", playerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupérer les X derniers matchs pour un joueur
     */
    @GetMapping("/{playerId}/recent")
    public ResponseEntity<List<GameRecordDTO>> getRecentMatches(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Getting {} recent matches for player {}", limit, playerId);
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<GameRecord> gameRecords = gameRecordRepository.findTopNByPlayerIdOrderByPlayedAtDesc(playerId, pageable);
            
            List<GameRecordDTO> gameRecordDTOs = gameRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            logger.info("Found {} recent match records for player {}", gameRecordDTOs.size(), playerId);
            return ResponseEntity.ok(gameRecordDTOs);
            
        } catch (Exception e) {
            logger.error("Error retrieving recent matches for player {}: {}", playerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtenir des statistiques sur l'historique des matchs
     */
    @GetMapping("/{playerId}/stats")
    public ResponseEntity<MatchStatsDTO> getMatchStats(@PathVariable Long playerId) {
        logger.info("Getting match statistics for player {}", playerId);
        
        try {
            long totalGames = gameRecordRepository.countByPlayerId(playerId);
            long gamesWon = gameRecordRepository.countByPlayerIdAndWonTrue(playerId);
            double winRate = totalGames > 0 ? (double) gamesWon / totalGames * 100 : 0;
            
            MatchStatsDTO stats = new MatchStatsDTO(totalGames, gamesWon, winRate);
            
            logger.info("Match stats for player {}: {} total, {} won, {:.2f}% win rate", 
                playerId, totalGames, gamesWon, winRate);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error retrieving match stats for player {}: {}", playerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour nettoyer les doublons de GameRecord (à utiliser uniquement pour le débogage)
     */
    @PostMapping("/cleanup-duplicates/{playerId}")
    public ResponseEntity<String> cleanupDuplicates(@PathVariable Long playerId) {
        logger.info("Starting cleanup of duplicate GameRecords for player {}", playerId);
        
        try {
            List<GameRecord> allRecords = gameRecordRepository.findByPlayerIdOrderByPlayedAtDesc(playerId);
            
            if (allRecords.isEmpty()) {
                return ResponseEntity.ok("No records found for player " + playerId);
            }
            
            // Grouper les records par gameId (null pour les anciens records)
            java.util.Map<String, List<GameRecord>> recordsByGameId = allRecords.stream()
                .collect(Collectors.groupingBy(
                    record -> record.getGameId() != null ? record.getGameId() : "unknown-" + record.getId()
                ));
            
            int duplicatesRemoved = 0;
            
            for (java.util.Map.Entry<String, List<GameRecord>> entry : recordsByGameId.entrySet()) {
                List<GameRecord> records = entry.getValue();
                
                if (records.size() > 1) {
                    // Garder le plus ancien record (premier dans la liste triée par date DESC -> le dernier)
                    GameRecord recordToKeep = records.get(records.size() - 1);
                    
                    // Supprimer les autres
                    for (int i = 0; i < records.size() - 1; i++) {
                        GameRecord recordToDelete = records.get(i);
                        gameRecordRepository.delete(recordToDelete);
                        duplicatesRemoved++;
                        logger.info("Deleted duplicate GameRecord with ID {} for player {}", 
                            recordToDelete.getId(), playerId);
                    }
                    
                    logger.info("Kept GameRecord with ID {} for gameId {} for player {}", 
                        recordToKeep.getId(), entry.getKey(), playerId);
                }
            }
            
            String message = String.format("Cleanup completed for player %d. Removed %d duplicate records.", 
                playerId, duplicatesRemoved);
            logger.info(message);
            
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            logger.error("Error during cleanup for player {}: {}", playerId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Convertir GameRecord en GameRecordDTO
     */
    private GameRecordDTO convertToDTO(GameRecord gameRecord) {
        return new GameRecordDTO(
            gameRecord.getId(),
            gameRecord.getPlayedAt(),
            gameRecord.isWon(),
            gameRecord.getScoreChange(),
            gameRecord.getGameId()
        );
    }

    /**
     * DTO pour les statistiques de match
     */
    public static class MatchStatsDTO {
        private long totalGames;
        private long gamesWon;
        private double winRate;

        public MatchStatsDTO(long totalGames, long gamesWon, double winRate) {
            this.totalGames = totalGames;
            this.gamesWon = gamesWon;
            this.winRate = winRate;
        }

        // Getters
        public long getTotalGames() { return totalGames; }
        public long getGamesWon() { return gamesWon; }
        public double getWinRate() { return winRate; }
    }
} 