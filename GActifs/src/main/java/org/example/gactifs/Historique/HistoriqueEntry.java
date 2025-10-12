package org.example.gactifs.Historique;

import jakarta.persistence.*;
import lombok.*;
import org.example.gactifs.Reclamation.model.Reclamation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "historique_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // TypeAction

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String utilisateur;

    @Column(nullable = false)
    private String roleUtilisateur; // RoleUtilisateur

    @Column(nullable = false)
    private LocalDateTime dateAction;

    private String adresseIP;

    // Pour stocker les changements de valeurs
    private String ancienneValeur;
    private String nouvelleValeur;

    // Détails supplémentaires en JSON
    @Column(columnDefinition = "TEXT")
    private String detailsJson;

    // Relation avec réclamation (optionnelle)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reclamation_id")
    private Reclamation reclamation;

    private String reclamationNumero;

    // Méthodes utilitaires pour les détails
    public Map<String, String> getDetails() {
        if (detailsJson == null || detailsJson.isEmpty()) {
            return new HashMap<>();
        }
        // Implémentez la désérialisation JSON
        return new HashMap<>(); // À compléter avec Jackson
    }

    public void setDetails(Map<String, String> details) {
        if (details == null || details.isEmpty()) {
            this.detailsJson = null;
        } else {
            // Implémentez la sérialisation JSON
            this.detailsJson = "{}"; // À compléter avec Jackson
        }
    }
}