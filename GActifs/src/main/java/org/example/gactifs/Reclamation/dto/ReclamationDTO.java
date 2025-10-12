package org.example.gactifs.Reclamation.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReclamationDTO {
    private Long id;
    private String numero;
    private String objet;
    private String description;

    private String typeReclamation;   // Enum sous forme de String
    private String priorite;          // Enum sous forme de String
    private String statut;            // Enum sous forme de String

    private LocalDateTime dateCreation;
    private LocalDateTime dateEcheance;
    private LocalDateTime dateResolution;

    private String reclamantNom;
    private String reclamantEmail;
    private String reclamantTelephone;

    private String resolution;
    private Integer satisfactionClient;
    private Double cout;
    private Integer tempsResolution;

    private UUID technicienAssigneId;   // relation User
    private Long serviceDirectionId;    // relation ServiceDirection
    private Long assetId;               // relation Asset
}
