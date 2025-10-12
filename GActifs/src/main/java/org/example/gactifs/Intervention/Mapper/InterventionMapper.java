package org.example.gactifs.Intervention.Mapper;

import org.example.gactifs.Intervention.dto.InterventionDTO;
import org.example.gactifs.Intervention.model.Intervention;
import org.example.gactifs.asset.models.Asset;
import org.example.gactifs.asset.models.ServiceDirection;
import org.example.gactifs.auth.Model.User;
import org.springframework.stereotype.Component;

@Component
public class InterventionMapper {

    public InterventionDTO toDTO(Intervention intervention) {
        if (intervention == null) {
            return null;
        }

        InterventionDTO.InterventionDTOBuilder builder = InterventionDTO.builder()
                .id(intervention.getId())
                .titre(intervention.getTitre())
                .description(intervention.getDescription())
                .typeIntervention(intervention.getTypeIntervention())
                .priorite(intervention.getPriorite())
                .statut(intervention.getStatut())
                .dateCreation(intervention.getDateCreation())
                .dateDebut(intervention.getDateDebut())
                .dateFin(intervention.getDateFin())
                .dateEcheance(intervention.getDateEcheance())
                .dureeEstimee(intervention.getDureeEstimee())
                .dureeReelle(intervention.getDureeReelle())
                .cout(intervention.getCout())
                .creePar(intervention.getCreePar())
                .validateur(intervention.getValidateur())
                .dateValidation(intervention.getDateValidation());

        // Asset - mapper avec nom
        if (intervention.getAsset() != null) {
            builder.assetId(intervention.getAsset().getId())
                    .assetNom(intervention.getAsset().getNom());
        }

        // Technicien - mapper avec toutes les infos
        if (intervention.getTechnicienAssigne() != null) {
            User tech = intervention.getTechnicienAssigne();
            builder.technicienId(tech.getId())
                    .technicienAssigne(InterventionDTO.TechnicienDTO.builder()
                            .id(tech.getId())
                            .nom(tech.getFirstName())
                            .prenom(tech.getLastName())
                            .email(tech.getEmail())
                            .build());
        }

        // Service Direction - mapper avec nom
        if (intervention.getServiceDirection() != null) {
            builder.serviceDirectionId(intervention.getServiceDirection().getId())
                    .serviceDirectionNom(intervention.getServiceDirection().getNom());
        }

        return builder.build();
    }

    public Intervention toEntity(InterventionDTO dto, Asset asset, User technicien, ServiceDirection serviceDirection) {
        if (dto == null) {
            return null;
        }

        Intervention.InterventionBuilder builder = Intervention.builder()
                .id(dto.getId())
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .dateCreation(dto.getDateCreation())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .dateEcheance(dto.getDateEcheance())
                .dureeEstimee(dto.getDureeEstimee())
                .dureeReelle(dto.getDureeReelle())
                .cout(dto.getCout())
                .creePar(dto.getCreePar())
                .validateur(dto.getValidateur())
                .dateValidation(dto.getDateValidation())
                .asset(asset)
                .technicienAssigne(technicien)
                .serviceDirection(serviceDirection);

        // S'assurer que les enums ont des valeurs par défaut
        builder.typeIntervention(dto.getTypeIntervention() != null ?
                dto.getTypeIntervention() : Intervention.TypeIntervention.AUTRE);

        builder.priorite(dto.getPriorite() != null ?
                dto.getPriorite() : Intervention.PrioriteIntervention.MOYENNE);

        builder.statut(dto.getStatut() != null ?
                dto.getStatut() : Intervention.StatutIntervention.PLANIFIEE);

        return builder.build();
    }

    public void updateEntityFromDTO(InterventionDTO dto, Intervention intervention,
                                    Asset asset, User technicien, ServiceDirection serviceDirection) {
        if (dto == null || intervention == null) {
            return;
        }

        // Mise à jour des champs simples
        if (dto.getTitre() != null) {
            intervention.setTitre(dto.getTitre());
        }
        if (dto.getDescription() != null) {
            intervention.setDescription(dto.getDescription());
        }

        // Mise à jour des enums avec valeurs par défaut
        intervention.setTypeIntervention(dto.getTypeIntervention() != null ?
                dto.getTypeIntervention() : Intervention.TypeIntervention.AUTRE);

        intervention.setPriorite(dto.getPriorite() != null ?
                dto.getPriorite() : Intervention.PrioriteIntervention.MOYENNE);

        intervention.setStatut(dto.getStatut() != null ?
                dto.getStatut() : intervention.getStatut());

        // Mise à jour des dates
        intervention.setDateDebut(dto.getDateDebut());
        intervention.setDateFin(dto.getDateFin());
        intervention.setDateEcheance(dto.getDateEcheance());

        // Mise à jour des champs numériques
        intervention.setDureeEstimee(dto.getDureeEstimee());
        intervention.setDureeReelle(dto.getDureeReelle());
        intervention.setCout(dto.getCout());

        // Mise à jour de la validation
        intervention.setValidateur(dto.getValidateur());
        intervention.setDateValidation(dto.getDateValidation());

        // Mise à jour des relations
        intervention.setAsset(asset);
        intervention.setTechnicienAssigne(technicien);
        intervention.setServiceDirection(serviceDirection);
    }
}