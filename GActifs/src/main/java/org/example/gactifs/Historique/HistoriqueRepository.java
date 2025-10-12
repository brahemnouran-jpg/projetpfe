package org.example.gactifs.Historique;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<HistoriqueEntry, Long> {

    Page<HistoriqueEntry> findByReclamationId(Long reclamationId, Pageable pageable);
    List<HistoriqueEntry> findByReclamationIdOrderByDateActionDesc(Long reclamationId);

    @Query("SELECT h FROM HistoriqueEntry h WHERE " +
            "(:reclamationId IS NULL OR h.reclamation.id = :reclamationId) AND " +
            "(:utilisateur IS NULL OR LOWER(h.utilisateur) LIKE LOWER(CONCAT('%', :utilisateur, '%'))) AND " +
            "(:action IS NULL OR h.action = :action) AND " +
            "(:roleUtilisateur IS NULL OR h.roleUtilisateur = :roleUtilisateur) AND " +
            "(:dateDebut IS NULL OR h.dateAction >= :dateDebut) AND " +
            "(:dateFin IS NULL OR h.dateAction <= :dateFin)")
    Page<HistoriqueEntry> findByFilters(
            @Param("reclamationId") Long reclamationId,
            @Param("utilisateur") String utilisateur,
            @Param("action") String action,
            @Param("roleUtilisateur") String roleUtilisateur,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin,
            Pageable pageable);

    @Query("SELECT COUNT(h) FROM HistoriqueEntry h WHERE " +
            "(:dateDebut IS NULL OR h.dateAction >= :dateDebut) AND " +
            "(:dateFin IS NULL OR h.dateAction <= :dateFin)")
    long countByPeriod(@Param("dateDebut") LocalDateTime dateDebut,
                       @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT h.action, COUNT(h) FROM HistoriqueEntry h WHERE " +
            "(:dateDebut IS NULL OR h.dateAction >= :dateDebut) AND " +
            "(:dateFin IS NULL OR h.dateAction <= :dateFin) " +
            "GROUP BY h.action")
    List<Object[]> countActionsByType(@Param("dateDebut") LocalDateTime dateDebut,
                                      @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT h.utilisateur, COUNT(h) FROM HistoriqueEntry h WHERE " +
            "(:dateDebut IS NULL OR h.dateAction >= :dateDebut) AND " +
            "(:dateFin IS NULL OR h.dateAction <= :dateFin) " +
            "GROUP BY h.utilisateur")
    List<Object[]> countActionsByUtilisateur(@Param("dateDebut") LocalDateTime dateDebut,
                                             @Param("dateFin") LocalDateTime dateFin);

    @Query(value = "SELECT DATE_TRUNC('day', h.date_action) as jour, COUNT(*) as count " +
            "FROM historique_actions h WHERE " +
            "(:dateDebut IS NULL OR h.date_action >= :dateDebut) AND " +
            "(:dateFin IS NULL OR h.date_action <= :dateFin) " +
            "GROUP BY jour " +
            "ORDER BY jour DESC",
            nativeQuery = true)
    List<Object[]> countActionsByJour(@Param("dateDebut") LocalDateTime dateDebut,
                                      @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT COUNT(DISTINCT h.utilisateur) FROM HistoriqueEntry h WHERE " +
            "(:dateDebut IS NULL OR h.dateAction >= :dateDebut) AND " +
            "(:dateFin IS NULL OR h.dateAction <= :dateFin)")
    long countDistinctUtilisateurs(@Param("dateDebut") LocalDateTime dateDebut,
                                   @Param("dateFin") LocalDateTime dateFin);

}