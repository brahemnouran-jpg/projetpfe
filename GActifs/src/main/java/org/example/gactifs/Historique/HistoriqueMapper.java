package org.example.gactifs.Historique;

import org.springframework.stereotype.Component;

@Component
public class HistoriqueMapper {

    public HistoriqueEntryDTO toDTO(HistoriqueEntry entry) {
        if (entry == null) {
            return null;
        }

        return HistoriqueEntryDTO.builder()
                .id(entry.getId())
                .action(entry.getAction())
                .description(entry.getDescription())
                .utilisateur(entry.getUtilisateur())
                .roleUtilisateur(entry.getRoleUtilisateur())
                .dateAction(entry.getDateAction())
                .adresseIP(entry.getAdresseIP())
                .ancienneValeur(entry.getAncienneValeur())
                .nouvelleValeur(entry.getNouvelleValeur())
                .details(entry.getDetails())
                .reclamationId(entry.getReclamation() != null ? entry.getReclamation().getId() : null)
                .reclamationNumero(entry.getReclamationNumero())
                .build();
    }

    public HistoriqueEntry toEntity(HistoriqueEntryDTO dto) {
        if (dto == null) {
            return null;
        }

        return HistoriqueEntry.builder()
                .id(dto.getId())
                .action(dto.getAction())
                .description(dto.getDescription())
                .utilisateur(dto.getUtilisateur())
                .roleUtilisateur(dto.getRoleUtilisateur())
                .dateAction(dto.getDateAction())
                .adresseIP(dto.getAdresseIP())
                .ancienneValeur(dto.getAncienneValeur())
                .nouvelleValeur(dto.getNouvelleValeur())
                .build();
    }
}