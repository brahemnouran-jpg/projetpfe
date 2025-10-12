package org.example.gactifs.Intervention.controlleur;

import lombok.RequiredArgsConstructor;
import org.example.gactifs.Intervention.Service.InterventionService;
import org.example.gactifs.Intervention.dto.InterventionDTO;
import org.example.gactifs.Intervention.model.Intervention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/interventions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InterventionController {

    private final InterventionService interventionService;

    @PostMapping
    public ResponseEntity<InterventionDTO> createIntervention(@RequestBody InterventionDTO interventionDTO) {
        InterventionDTO createdIntervention = interventionService.createIntervention(interventionDTO);
        return ResponseEntity.ok(createdIntervention);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterventionDTO> getInterventionById(@PathVariable Long id) {
        Optional<InterventionDTO> intervention = interventionService.getInterventionById(id);
        return intervention.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<InterventionDTO>> getAllInterventions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<InterventionDTO> interventions = interventionService.getAllInterventions(pageable);
        return ResponseEntity.ok(interventions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterventionDTO> updateIntervention(@PathVariable Long id,
                                                              @RequestBody InterventionDTO interventionDTO) {
        InterventionDTO updatedIntervention = interventionService.updateIntervention(id, interventionDTO);
        return ResponseEntity.ok(updatedIntervention);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIntervention(@PathVariable Long id) {
        interventionService.deleteIntervention(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<InterventionDTO>> searchInterventions(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) Intervention.StatutIntervention statut,
            @RequestParam(required = false) Intervention.PrioriteIntervention priorite,
            @RequestParam(required = false) Intervention.TypeIntervention typeIntervention,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<InterventionDTO> interventions = interventionService.searchInterventions(
                titre, statut, priorite, typeIntervention, pageable);
        return ResponseEntity.ok(interventions);
    }

    @GetMapping("/stats")
    public ResponseEntity<InterventionService.InterventionStats> getInterventionStats() {
        InterventionService.InterventionStats stats = interventionService.getInterventionStats();
        return ResponseEntity.ok(stats);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InterventionDTO> changeStatus(@PathVariable Long id,
                                                        @RequestParam Intervention.StatutIntervention newStatus) {
        InterventionDTO updatedIntervention = interventionService.changeStatus(id, newStatus);
        return ResponseEntity.ok(updatedIntervention);
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<InterventionDTO> startIntervention(@PathVariable Long id) {
        InterventionDTO updatedIntervention = interventionService.changeStatus(id, Intervention.StatutIntervention.EN_COURS);
        return ResponseEntity.ok(updatedIntervention);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<InterventionDTO> completeIntervention(@PathVariable Long id) {
        InterventionDTO updatedIntervention = interventionService.changeStatus(id, Intervention.StatutIntervention.TERMINEE);
        return ResponseEntity.ok(updatedIntervention);
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<InterventionDTO> assignTechnician(@PathVariable Long id,
                                                            @RequestParam UUID technicienId) {
        InterventionDTO updatedIntervention = interventionService.assignTechnician(id, technicienId);
        return ResponseEntity.ok(updatedIntervention);
    }

    // Calendrier pour tous (ADMIN voit tout)
    @GetMapping("/calendar")
    public ResponseEntity<List<InterventionDTO>> getInterventionsForCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<InterventionDTO> interventions = interventionService.getInterventionsForCalendar(start, end);
        return ResponseEntity.ok(interventions);
    }

    // Calendrier filtré par technicien (pour TECHNICIEN)
    @GetMapping("/calendar/technician/{technicienId}")
    public ResponseEntity<List<InterventionDTO>> getInterventionsForCalendarByTechnicien(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PathVariable UUID technicienId) {

        List<InterventionDTO> interventions = interventionService
                .getInterventionsForCalendarByTechnicien(start, end, technicienId);
        return ResponseEntity.ok(interventions);
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueInterventions() {
        return ResponseEntity.ok().build();
    }
}