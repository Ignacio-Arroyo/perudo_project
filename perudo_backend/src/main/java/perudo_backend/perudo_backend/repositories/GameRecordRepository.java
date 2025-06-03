package perudo_backend.perudo_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import perudo_backend.perudo_backend.GameRecord;

import java.util.List;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    
    // Récupérer tous les enregistrements de jeu pour un joueur spécifique, triés par date (plus récent en premier)
    @Query("SELECT gr FROM GameRecord gr WHERE gr.player.id = :playerId ORDER BY gr.playedAt DESC")
    List<GameRecord> findByPlayerIdOrderByPlayedAtDesc(@Param("playerId") Long playerId);
    
    // Récupérer les X derniers jeux d'un joueur
    @Query("SELECT gr FROM GameRecord gr WHERE gr.player.id = :playerId ORDER BY gr.playedAt DESC")
    List<GameRecord> findTopNByPlayerIdOrderByPlayedAtDesc(@Param("playerId") Long playerId, org.springframework.data.domain.Pageable pageable);
    
    // Compter le nombre total de jeux joués par un joueur
    long countByPlayerId(Long playerId);
    
    // Compter le nombre de jeux gagnés par un joueur
    long countByPlayerIdAndWonTrue(Long playerId);
    
    // Vérifier si un GameRecord existe déjà pour ce joueur et ce match
    boolean existsByPlayerIdAndGameId(Long playerId, String gameId);
} 