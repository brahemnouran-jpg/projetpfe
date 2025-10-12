package org.example.gactifs.Reclamation.maper;

import org.example.gactifs.Reclamation.dto.ReclamationDTO;
import org.example.gactifs.Reclamation.model.Reclamation;
import org.example.gactifs.asset.models.Asset;
import org.example.gactifs.asset.models.ServiceDirection;
import org.example.gactifs.auth.Model.User;

public class ReclamationMapper {

    public static ReclamationDTO toDto(Reclamation entity) {
        if (entity == null) return null;

        ReclamationDTO dto = new ReclamationDTO();
        dto.setId(entity.getId());
        dto.setNumero(entity.getNumero());
        dto.setObjet(entity.getObjet());
        dto.setDescription(entity.getDescription());

        if (entity.getTypeReclamation() != null)
            dto.setTypeReclamation(entity.getTypeReclamation().name());
        if (entity.getPriorite() != null)
            dto.setPriorite(entity.getPriorite().name());
        if (entity.getStatut() != null)
            dto.setStatut(entity.getStatut().name());

        dto.setDateCreation(entity.getDateCreation());
        dto.setDateEcheance(entity.getDateEcheance());
        dto.setDateResolution(entity.getDateResolution());

        dto.setReclamantNom(entity.getReclamantNom());
        dto.setReclamantEmail(entity.getReclamantEmail());
        dto.setReclamantTelephone(entity.getReclamantTelephone());

        dto.setResolution(entity.getResolution());
        dto.setSatisfactionClient(entity.getSatisfactionClient());
        dto.setCout(entity.getCout());
        dto.setTempsResolution(entity.getTempsResolution());

        if (entity.getTechnicienAssigne() != null)
            dto.setTechnicienAssigneId(entity.getTechnicienAssigne().getId());
        if (entity.getServiceDirection() != null)
            dto.setServiceDirectionId(entity.getServiceDirection().getId());
        if (entity.getAsset() != null)
            dto.setAssetId(entity.getAsset().getId());

        return dto;
    }

    public static Reclamation toEntity(ReclamationDTO dto) {
        if (dto == null) return null;

        Reclamation entity = new Reclamation();
        entity.setId(dto.getId());
        entity.setNumero(dto.getNumero());
        entity.setObjet(dto.getObjet());
        entity.setDescription(dto.getDescription());

        if (dto.getTypeReclamation() != null)
            entity.setTypeReclamation(Reclamation.TypeReclamation.valueOf(dto.getTypeReclamation()));
        if (dto.getPriorite() != null)
            entity.setPriorite(Reclamation.PrioriteReclamation.valueOf(dto.getPriorite()));
        if (dto.getStatut() != null)
            entity.setStatut(Reclamation.StatutReclamation.valueOf(dto.getStatut()));

        entity.setDateCreation(dto.getDateCreation());
        entity.setDateEcheance(dto.getDateEcheance());
        entity.setDateResolution(dto.getDateResolution());

        entity.setReclamantNom(dto.getReclamantNom());
        entity.setReclamantEmail(dto.getReclamantEmail());
        entity.setReclamantTelephone(dto.getReclamantTelephone());

        entity.setResolution(dto.getResolution());
        entity.setSatisfactionClient(dto.getSatisfactionClient());
        entity.setCout(dto.getCout());
        entity.setTempsResolution(dto.getTempsResolution());

        if (dto.getTechnicienAssigneId() != null)
            entity.setTechnicienAssigne(new User(dto.getTechnicienAssigneId()));
        if (dto.getServiceDirectionId() != null) {
            ServiceDirection sd = new ServiceDirection();
            sd.setId(dto.getServiceDirectionId());
            entity.setServiceDirection(sd);
        }
        if (dto.getAssetId() != null) {
            Asset asset = new Asset();
            asset.setId(dto.getAssetId());
            entity.setAsset(asset);
        }

        return entity;
    }
}
