package org.example.gactifs.Intervention.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.gactifs.Intervention.model.Intervention;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterventionDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    @NotNull(message = "Le type d'intervention est obligatoire")
    @Builder.Default
    private Intervention.TypeIntervention typeIntervention = Intervention.TypeIntervention.AUTRE;

    @NotNull(message = "La priorité est obligatoire")
    @Builder.Default
    private Intervention.PrioriteIntervention priorite = Intervention.PrioriteIntervention.MOYENNE;

    @NotNull(message = "Le statut est obligatoire")
    @Builder.Default
    private Intervention.StatutIntervention statut = Intervention.StatutIntervention.PLANIFIEE;

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

    // Relations - Asset
    private Long assetId;
    private String assetNom;

    // Relations - Technicien
    private UUID technicienId;
    private TechnicienDTO technicienAssigne;

    // Relations - Service Direction
    private Long serviceDirectionId;
    private String serviceDirectionNom;

    // DTO interne pour technicien
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TechnicienDTO {
        private UUID id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String specialite;
        private String nomComplet;

        public String getNomComplet() {
            if (nomComplet != null) return nomComplet;
            return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
        }
    }
}