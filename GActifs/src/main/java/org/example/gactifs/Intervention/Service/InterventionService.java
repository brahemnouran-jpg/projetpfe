package org.example.gactifs.Intervention.Service;

import lombok.RequiredArgsConstructor;
import org.example.gactifs.Intervention.Mapper.InterventionMapper;
import org.example.gactifs.Intervention.dto.InterventionDTO;
import org.example.gactifs.Intervention.model.Intervention;
import org.example.gactifs.Intervention.repository.InterventionRepository;
import org.example.gactifs.Notification.Notification;
import org.example.gactifs.Notification.NotificationService;
import org.example.gactifs.asset.models.Asset;
import org.example.gactifs.asset.models.ServiceDirection;
import org.example.gactifs.asset.repository.AssetRepository;
import org.example.gactifs.asset.repository.ServiceDirectionRepository;
import org.example.gactifs.auth.Model.User;
import org.example.gactifs.auth.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final ServiceDirectionRepository serviceDirectionRepository;
    private final InterventionMapper interventionMapper;
    private final NotificationService notificationService;

    // CRUD Operations
    public InterventionDTO createIntervention(InterventionDTO interventionDTO) {
        Intervention intervention = interventionMapper.toEntity(
                interventionDTO,
                getAssetById(interventionDTO.getAssetId()),
                getTechnicienById(interventionDTO.getTechnicienId()),
                getServiceDirectionById(interventionDTO.getServiceDirectionId())
        );

        intervention.setDateCreation(LocalDateTime.now());
        intervention.setCreePar("SYSTEM");

        Intervention savedIntervention = interventionRepository.save(intervention);

        // Envoi notification au technicien
        if (savedIntervention.getTechnicienAssigne() != null) {
            Notification notification = Notification.builder()
                    .utilisateurId(savedIntervention.getTechnicienAssigne().getId())
                    .titre("Nouvelle intervention assignée")
                    .message("Vous avez une nouvelle intervention: " + savedIntervention.getTitre())
                    .adresseIP("SYSTEM")
                    .build();

            notificationService.envoyerNotification(notification);
        }

        return interventionMapper.toDTO(savedIntervention);
    }

    // ✅ CALENDRIER: TOUTES les interventions (peu importe la date)
    public List<InterventionDTO> getInterventionsForCalendar(LocalDateTime start, LocalDateTime end) {
        // Retourner TOUTES les interventions, pas seulement celles du mois
        List<Intervention> interventions = interventionRepository.findAllInterventionsWithoutDateLimit();

        System.out.println("Backend: Total interventions (no date limit): " + interventions.size());

        return interventions.stream()
                .map(interventionMapper::toDTO)
                .toList();
    }

    // ✅ CALENDRIER: TOUTES les interventions d'un technicien (peu importe la date)
    public List<InterventionDTO> getInterventionsForCalendarByTechnicien(
            LocalDateTime start, LocalDateTime end, UUID technicienId) {
        // Retourner TOUTES les interventions du technicien, pas seulement celles du mois
        List<Intervention> interventions = interventionRepository.findAllByTechnicienWithoutDateLimit(technicienId);

        System.out.println("Backend: Total interventions for technicien " + technicienId + ": " + interventions.size());

        return interventions.stream()
                .map(interventionMapper::toDTO)
                .toList();
    }

    public Optional<InterventionDTO> getInterventionById(Long id) {
        return interventionRepository.findById(id)
                .map(interventionMapper::toDTO);
    }

    public Page<InterventionDTO> getAllInterventions(Pageable pageable) {
        return interventionRepository.findAll(pageable)
                .map(interventionMapper::toDTO);
    }

    public InterventionDTO updateIntervention(Long id, InterventionDTO interventionDTO) {
        Intervention existingIntervention = interventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention non trouvée avec l'id: " + id));

        interventionMapper.updateEntityFromDTO(
                interventionDTO,
                existingIntervention,
                getAssetById(interventionDTO.getAssetId()),
                getTechnicienById(interventionDTO.getTechnicienId()),
                getServiceDirectionById(interventionDTO.getServiceDirectionId())
        );

        Intervention updatedIntervention = interventionRepository.save(existingIntervention);
        return interventionMapper.toDTO(updatedIntervention);
    }

    public void deleteIntervention(Long id) {
        interventionRepository.deleteById(id);
    }

    // Search and Filter methods
    public Page<InterventionDTO> searchInterventions(String titre,
                                                     Intervention.StatutIntervention statut,
                                                     Intervention.PrioriteIntervention priorite,
                                                     Intervention.TypeIntervention typeIntervention,
                                                     Pageable pageable) {
        Page<Intervention> interventions = interventionRepository.findByFilters(
                titre, statut, priorite, typeIntervention, pageable);
        return interventions.map(interventionMapper::toDTO);
    }

    // Statistics
    public InterventionStats getInterventionStats() {
        long total = interventionRepository.count();
        long enCours = interventionRepository.countByStatut(Intervention.StatutIntervention.EN_COURS);
        long terminees = interventionRepository.countByStatut(Intervention.StatutIntervention.TERMINEE);
        long critiques = interventionRepository.countByPriorite(Intervention.PrioriteIntervention.CRITIQUE);
        long enRetard = interventionRepository.countByDateEcheanceBeforeAndStatutNotIn(
                LocalDateTime.now(),
                List.of(Intervention.StatutIntervention.TERMINEE, Intervention.StatutIntervention.ANNULEE, Intervention.StatutIntervention.VALIDEE)
        );

        return new InterventionStats(total, enCours, terminees, critiques, enRetard);
    }

    // Status management
    public InterventionDTO changeStatus(Long id, Intervention.StatutIntervention newStatus) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention non trouvée"));

        intervention.setStatut(newStatus);

        if (newStatus == Intervention.StatutIntervention.EN_COURS && intervention.getDateDebut() == null) {
            intervention.setDateDebut(LocalDateTime.now());
        } else if (newStatus == Intervention.StatutIntervention.TERMINEE && intervention.getDateFin() == null) {
            intervention.setDateFin(LocalDateTime.now());
        }

        Intervention updatedIntervention = interventionRepository.save(intervention);
        return interventionMapper.toDTO(updatedIntervention);
    }

    // Assignment
    public InterventionDTO assignTechnician(Long interventionId, UUID technicienId) {
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new RuntimeException("Intervention non trouvée"));

        User technicien = getTechnicienById(technicienId);
        intervention.setTechnicienAssigne(technicien);

        Intervention updatedIntervention = interventionRepository.save(intervention);
        return interventionMapper.toDTO(updatedIntervention);
    }

    // Helper methods
    private Asset getAssetById(Long assetId) {
        if (assetId == null) return null;
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset non trouvé avec l'id: " + assetId));
    }

    private User getTechnicienById(UUID technicienId) {
        if (technicienId == null) return null;
        return userRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé avec l'id: " + technicienId));
    }

    private ServiceDirection getServiceDirectionById(Long serviceDirectionId) {
        if (serviceDirectionId == null) return null;
        return serviceDirectionRepository.findById(serviceDirectionId)
                .orElseThrow(() -> new RuntimeException("Service direction non trouvé avec l'id: " + serviceDirectionId));
    }

    public record InterventionStats(long total, long enCours, long terminees, long critiques, long enRetard) {}
}