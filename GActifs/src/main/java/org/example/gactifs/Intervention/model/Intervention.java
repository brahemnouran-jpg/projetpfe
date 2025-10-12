package org.example.gactifs.Intervention.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.gactifs.asset.models.Asset;
import org.example.gactifs.asset.models.ServiceDirection;
import org.example.gactifs.auth.Model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "interventions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TypeIntervention typeIntervention = TypeIntervention.AUTRE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PrioriteIntervention priorite = PrioriteIntervention.MOYENNE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutIntervention statut = StatutIntervention.PLANIFIEE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateEcheance;

    private Double dureeEstimee;
    private Double dureeReelle;
    private Double cout;

    private String creePar;
    private String validateur;
    private LocalDateTime dateValidation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_assigne_id")
    private User technicienAssigne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_direction_id")
    private ServiceDirection serviceDirection;

    // Méthode appelée automatiquement avant la persistance
    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutIntervention.PLANIFIEE;
        }
        if (priorite == null) {
            priorite = PrioriteIntervention.MOYENNE;
        }
        if (typeIntervention == null) {
            typeIntervention = TypeIntervention.AUTRE;
        }
    }

    // Enums
    public enum TypeIntervention {
        MAINTENANCE_PREVENTIVE,
        MAINTENANCE_CORRECTIVE,
        REPARATION,
        INSTALLATION,
        CONFIGURATION,
        MISE_A_JOUR,
        DIAGNOSTIC,
        AUTRE
    }

    public enum PrioriteIntervention {
        CRITIQUE,
        HAUTE,
        MOYENNE,
        BASSE
    }

    public enum StatutIntervention {
        PLANIFIEE,
        EN_ATTENTE,
        EN_COURS,
        SUSPENDUE,
        TERMINEE,
        ANNULEE,
        VALIDEE
    }
}