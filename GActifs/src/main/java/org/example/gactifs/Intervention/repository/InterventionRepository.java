package org.example.gactifs.Intervention.repository;

import org.example.gactifs.Intervention.model.Intervention;
import org.example.gactifs.auth.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Long> {
    @Query("SELECT i FROM Intervention i WHERE " +
            "(i.dateDebut BETWEEN :start AND :end) OR " +
            "(i.dateFin BETWEEN :start AND :end) OR " +
            "(i.dateDebut <= :start AND i.dateFin >= :end)")
    List<Intervention> findByDateDebutBetweenOrDateFinBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // ✅ NOUVEAU - Retourne TOUTES les interventions (pas de limite de date)
    @Query("SELECT i FROM Intervention i ORDER BY i.dateDebut ASC")
    List<Intervention> findAllInterventionsWithoutDateLimit();

    // ✅ Pour technicien - Retourne TOUTES ses interventions
    @Query("SELECT i FROM Intervention i WHERE i.technicienAssigne.id = :technicienId " +
            "ORDER BY i.dateDebut ASC")
    List<Intervention> findAllByTechnicienWithoutDateLimit(
            @Param("technicienId") UUID technicienId
    );

    // ✅ ORIGINAL - Pour technicien entre deux dates
    @Query("SELECT i FROM Intervention i WHERE i.technicienAssigne.id = :technicienId AND " +
            "((i.dateDebut BETWEEN :start AND :end) OR " +
            "(i.dateFin BETWEEN :start AND :end) OR " +
            "(i.dateDebut <= :start AND i.dateFin >= :end)) " +
            "ORDER BY i.dateDebut ASC")
    List<Intervention> findByTechnicienAndDateRange(
            @Param("technicienId") UUID technicienId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    // Méthode pour récupérer toutes les interventions dans une période
    List<Intervention> findByDateDebutBetweenOrDateFinBetween(
            LocalDateTime start1, LocalDateTime end1,
            LocalDateTime start2, LocalDateTime end2
    );

    // Nouvelle méthode pour récupérer les interventions d'un technicien spécifique


    // Méthodes pour les statistiques
    long countByStatut(Intervention.StatutIntervention statut);

    long countByPriorite(Intervention.PrioriteIntervention priorite);

    long countByDateEcheanceBeforeAndStatutNotIn(
            LocalDateTime date,
            List<Intervention.StatutIntervention> statuts
    );

    // Méthode pour la recherche avec filtres
    @Query("SELECT i FROM Intervention i WHERE " +
            "(:titre IS NULL OR LOWER(i.titre) LIKE LOWER(CONCAT('%', :titre, '%'))) AND " +
            "(:statut IS NULL OR i.statut = :statut) AND " +
            "(:priorite IS NULL OR i.priorite = :priorite) AND " +
            "(:typeIntervention IS NULL OR i.typeIntervention = :typeIntervention)")
    Page<Intervention> findByFilters(
            @Param("titre") String titre,
            @Param("statut") Intervention.StatutIntervention statut,
            @Param("priorite") Intervention.PrioriteIntervention priorite,
            @Param("typeIntervention") Intervention.TypeIntervention typeIntervention,
            Pageable pageable
    );

    // Récupérer les interventions par technicien
    Page<Intervention> findByTechnicienAssigne(User technicien, Pageable pageable);

    // Récupérer les interventions en retard
    @Query("SELECT i FROM Intervention i WHERE i.dateEcheance < :now " +
            "AND i.statut NOT IN :excludedStatuts")
    List<Intervention> findOverdueInterventions(
            @Param("now") LocalDateTime now,
            @Param("excludedStatuts") List<Intervention.StatutIntervention> excludedStatuts
    );
}