package org.example.gactifs.Reclamation.Service;

import lombok.RequiredArgsConstructor;
import org.example.gactifs.Historique.HistoriqueService;
import org.example.gactifs.Reclamation.dto.ReclamationDTO;
import org.example.gactifs.Reclamation.maper.ReclamationMapper;
import org.example.gactifs.Reclamation.model.Reclamation;
import org.example.gactifs.Reclamation.repository.ReclamationRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final HistoriqueService historiqueService; // injection

    // 🔹 Récupérer toutes les réclamations
    public List<ReclamationDTO> getAll() {
        return reclamationRepository.findAll()
                .stream()
                .map(ReclamationMapper::toDto)
                .collect(Collectors.toList());
    }

    // 🔹 Récupérer une réclamation par ID
    public ReclamationDTO getById(Long id) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Réclamation non trouvée"));
        return ReclamationMapper.toDto(reclamation);
    }

    // 🔹 Créer une réclamation
    public ReclamationDTO create(ReclamationDTO dto, String utilisateur, String roleUtilisateur) {
        Reclamation reclamation = ReclamationMapper.toEntity(dto);
        reclamation.setDateCreation(LocalDateTime.now());
        reclamation.setStatut(Reclamation.StatutReclamation.NOUVELLE);
        reclamation.setNumero(generateNumero());

        Reclamation saved = reclamationRepository.save(reclamation);

        // 🔹 Log historique
        historiqueService.logAction(
                "CREATION_RECLAMATION",
                "Création de la réclamation " + saved.getNumero(),
                utilisateur,
                roleUtilisateur,
                null,
                saved.getId()
        );

        return ReclamationMapper.toDto(saved);
    }

    // 🔹 Modifier une réclamation
    public ReclamationDTO update(Long id, ReclamationDTO dto) {
        Reclamation existing = reclamationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Réclamation non trouvée"));

        existing.setObjet(dto.getObjet());
        existing.setDescription(dto.getDescription());
        existing.setTypeReclamation(Reclamation.TypeReclamation.valueOf(dto.getTypeReclamation().toUpperCase()));
        existing.setDateEcheance(dto.getDateEcheance());
        existing.setResolution(dto.getResolution());
        existing.setSatisfactionClient(dto.getSatisfactionClient());
        existing.setCout(dto.getCout());

        return ReclamationMapper.toDto(reclamationRepository.save(existing));
    }

    // 🔹 Supprimer une réclamation
    public void delete(Long id) {
        reclamationRepository.deleteById(id);
    }

    // 🔹 Changer le statut
    public ReclamationDTO changerStatut(Long id, Reclamation.StatutReclamation statut) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Réclamation non trouvée"));
        reclamation.setStatut(statut);
        return ReclamationMapper.toDto(reclamationRepository.save(reclamation));
    }

    // 🔹 Pagination (pour /paginated)
    public Page<ReclamationDTO> getPaginated(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size,
                sort != null ? Sort.by(sortDirection, sort) : Sort.by(sortDirection, "dateCreation"));
        Page<Reclamation> reclamations = reclamationRepository.findAll(pageable);
        return reclamations.map(ReclamationMapper::toDto);
    }

    // 🔹 Statistiques simples (pour /stats)
    public Map<String, Long> getStats() {
        List<Reclamation> all = reclamationRepository.findAll();
        long total = all.size();
        long nouvelles = all.stream().filter(r -> r.getStatut() == Reclamation.StatutReclamation.NOUVELLE).count();
        long enCours = all.stream().filter(r -> r.getStatut() == Reclamation.StatutReclamation.EN_COURS).count();
        long resolues = all.stream().filter(r -> r.getStatut() == Reclamation.StatutReclamation.RESOLUE).count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("nouvelles", nouvelles);
        stats.put("enCours", enCours);
        stats.put("resolues", resolues);
        return stats;
    }
    // 🔹 Recherche avec filtres (pour /search)
    public Page<ReclamationDTO> search(
            String objet,
            String statut,
            String priorite,
            String typeReclamation,
            String reclamantEmail,
            Long technicienId,
            Long serviceId,
            String dateCreationDebut,
            String dateCreationFin,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Sort sortOrder;
        if (sort == null || sort.isEmpty()) {
            sortOrder = Sort.by(Sort.Direction.ASC, "dateCreation"); // par exemple
        } else {
            sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        List<Reclamation> filtered = reclamationRepository.findAll().stream()
                .filter(r -> objet == null || r.getObjet().toLowerCase().contains(objet.toLowerCase()))
                .filter(r -> statut == null || r.getStatut().name().equalsIgnoreCase(statut))
                .filter(r -> typeReclamation == null || r.getTypeReclamation().name().equalsIgnoreCase(typeReclamation))
                .filter(r -> {
                    if (dateCreationDebut == null && dateCreationFin == null) return true;
                    LocalDateTime dateCreation = r.getDateCreation();
                    if (dateCreation == null) return false;
                    LocalDateTime debut = dateCreationDebut != null ? LocalDateTime.parse(dateCreationDebut + "T00:00:00") : LocalDateTime.MIN;
                    LocalDateTime fin = dateCreationFin != null ? LocalDateTime.parse(dateCreationFin + "T23:59:59") : LocalDateTime.MAX;
                    return !dateCreation.isBefore(debut) && !dateCreation.isAfter(fin);
                })
                .toList();

        // Pagination manuelle (car on a filtré en mémoire)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ReclamationDTO> pageContent = filtered.subList(start, end).stream()
                .map(ReclamationMapper::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    // 🔹 Réclamations en retard (pour /en-retard)
    public List<ReclamationDTO> getReclamationsEnRetard() {
        LocalDateTime now = LocalDateTime.now();
        return reclamationRepository.findAll()
                .stream()
                .filter(r -> r.getDateEcheance() != null &&
                        r.getDateEcheance().isBefore(now) &&
                        r.getStatut() != Reclamation.StatutReclamation.RESOLUE)
                .map(ReclamationMapper::toDto)
                .collect(Collectors.toList());
    }

    // 🔹 Rapport entre deux dates
    public List<ReclamationDTO> getRapport(String dateDebut, String dateFin) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime debut = LocalDateTime.parse(dateDebut + "T00:00:00");
        LocalDateTime fin = LocalDateTime.parse(dateFin + "T23:59:59");
        return reclamationRepository.findAll()
                .stream()
                .filter(r -> r.getDateCreation() != null &&
                        !r.getDateCreation().isBefore(debut) &&
                        !r.getDateCreation().isAfter(fin))
                .map(ReclamationMapper::toDto)
                .collect(Collectors.toList());
    }

    // 🔹 Export simulé (PDF / Excel)
    public String exportReclamations(String format, String objet, String statut, String priorite,
                                     String typeReclamation, String dateDebut, String dateFin) {
        // Pour l’instant : simulation (à remplacer par une vraie génération PDF/Excel)
        return "Export " + format.toUpperCase() + " généré avec succès";
    }

    // 🔹 Dashboard mocké
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("stats", getStats());
        dashboard.put("recentes", getAll().stream().limit(5).toList());
        return dashboard;
    }

    // 🔹 SLA mocké
    public List<Map<String, Object>> getSLAs() {
        return List.of(
                Map.of("niveau", "Gold", "delai", "24h"),
                Map.of("niveau", "Silver", "delai", "48h"),
                Map.of("niveau", "Bronze", "delai", "72h")
        );
    }

    // 🔹 Notifications mockées
    public List<Map<String, String>> getNotifications() {
        return List.of(
                Map.of("titre", "Nouvelle réclamation", "message", "Une nouvelle réclamation a été créée."),
                Map.of("titre", "Réclamation en retard", "message", "Certaines réclamations dépassent la date d’échéance.")
        );
    }

    // 🔹 Génération automatique du numéro
    private String generateNumero() {
        return "REC-" + System.currentTimeMillis();
    }
}
