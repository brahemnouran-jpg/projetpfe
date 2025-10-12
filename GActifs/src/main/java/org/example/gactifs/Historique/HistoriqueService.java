package org.example.gactifs.Historique;

import lombok.RequiredArgsConstructor;

import org.example.gactifs.Historique.*;
import org.example.gactifs.Reclamation.model.Reclamation;
import org.example.gactifs.Reclamation.repository.ReclamationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoriqueService {

    private final HistoriqueRepository historiqueRepository;
    private final ReclamationRepository reclamationRepository;
    private final HistoriqueMapper historiqueMapper;

    public Page<HistoriqueEntryDTO> getHistorique(HistoriqueFilterDTO filter, Pageable pageable) {
        Page<HistoriqueEntry> entries = historiqueRepository.findByFilters(
                filter.getReclamationId(),
                filter.getUtilisateur(),
                filter.getAction(),
                filter.getRoleUtilisateur(),
                filter.getDateDebut(),
                filter.getDateFin(),
                pageable
        );
        return entries.map(historiqueMapper::toDTO);
    }

    public List<HistoriqueEntryDTO> getHistoriqueByReclamation(Long reclamationId) {
        return historiqueRepository.findByReclamationIdOrderByDateActionDesc(reclamationId)
                .stream()
                .map(historiqueMapper::toDTO)
                .collect(Collectors.toList());
    }
    public List<HistoriqueEntryDTO> getAllHistorique() {
        List<HistoriqueEntry> historiques = historiqueRepository.findAll(Sort.by(Sort.Direction.DESC, "dateAction"));
        return historiques.stream()
                .map(historiqueMapper::toDTO)
                .collect(Collectors.toList());
    }

    public HistoriqueStatsDTO getHistoriqueStats(LocalDateTime dateDebut, LocalDateTime dateFin) {
        // Total actions
        long totalActions = historiqueRepository.countByPeriod(dateDebut, dateFin);

        // Actions par type
        List<Object[]> actionsParTypeData = historiqueRepository.countActionsByType(dateDebut, dateFin);
        Map<String, Long> actionsParType = actionsParTypeData.stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));

        // Actions par utilisateur
        List<Object[]> actionsParUtilisateurData = historiqueRepository.countActionsByUtilisateur(dateDebut, dateFin);
        Map<String, Long> actionsParUtilisateur = actionsParUtilisateurData.stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));

        // Actions par jour
        List<Object[]> actionsParJourData = historiqueRepository.countActionsByJour(dateDebut, dateFin);
        List<HistoriqueStatsDTO.ActionParJour> actionsParJour = actionsParJourData.stream()
                .map(obj -> new HistoriqueStatsDTO.ActionParJour(
                        obj[0].toString(),
                        (Long) obj[1]
                ))
                .collect(Collectors.toList());

        // Utilisateurs actifs
        long utilisateursActifs = historiqueRepository.countDistinctUtilisateurs(dateDebut, dateFin);

        return new HistoriqueStatsDTO(
                totalActions,
                actionsParType,
                actionsParUtilisateur,
                actionsParJour,
                utilisateursActifs
        );
    }

    // Méthode pour logger une action
    public void logAction(String action, String description, String utilisateur,
                          String roleUtilisateur, String adresseIP, Long reclamationId) {

        HistoriqueEntry.HistoriqueEntryBuilder builder = HistoriqueEntry.builder()
                .action(action)
                .description(description)
                .utilisateur(utilisateur)
                .roleUtilisateur(roleUtilisateur)
                .dateAction(LocalDateTime.now())
                .adresseIP(adresseIP);

        // Si reclamationId est fourni, chercher l'entité Reclamation
        if (reclamationId != null) {
            Reclamation reclamation = reclamationRepository.findById(reclamationId)
                    .orElse(null); // ou lancez une exception si nécessaire
            builder.reclamation(reclamation);

            // Optionnellement, stocker aussi le numéro de réclamation
            if (reclamation != null && reclamation.getNumero() != null) {
                builder.reclamationNumero(reclamation.getNumero());
            }
        }

        HistoriqueEntry entry = builder.build();
        historiqueRepository.save(entry);
    }

    public void logActionWithDetails(String action, String description, String utilisateur,
                                     String roleUtilisateur, String adresseIP, Long reclamationId,
                                     Map<String, String> details) {

        HistoriqueEntry.HistoriqueEntryBuilder builder = HistoriqueEntry.builder()
                .action(action)
                .description(description)
                .utilisateur(utilisateur)
                .roleUtilisateur(roleUtilisateur)
                .dateAction(LocalDateTime.now())
                .adresseIP(adresseIP);

        // Si reclamationId est fourni, chercher l'entité Reclamation
        if (reclamationId != null) {
            Reclamation reclamation = reclamationRepository.findById(reclamationId)
                    .orElse(null);
            builder.reclamation(reclamation);

            if (reclamation != null && reclamation.getNumero() != null) {
                builder.reclamationNumero(reclamation.getNumero());
            }
        }

        HistoriqueEntry entry = builder.build();
        entry.setDetails(details);
        historiqueRepository.save(entry);
    }
}