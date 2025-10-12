package org.example.gactifs.Historique;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/historique")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HistoriqueController {

    private final HistoriqueService historiqueService;
    @GetMapping("/all")
    public ResponseEntity<List<HistoriqueEntryDTO>> getAllHistorique() {
        List<HistoriqueEntryDTO> entries = historiqueService.getAllHistorique();
        return ResponseEntity.ok(entries);
    }

    @GetMapping
    public ResponseEntity<Page<HistoriqueEntryDTO>> getHistorique(
            @RequestParam(required = false) Long reclamationId,
            @RequestParam(required = false) String utilisateur,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String roleUtilisateur,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "dateAction") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        HistoriqueFilterDTO filter = new HistoriqueFilterDTO();
        filter.setReclamationId(reclamationId);
        filter.setUtilisateur(utilisateur);
        filter.setAction(action);
        filter.setRoleUtilisateur(roleUtilisateur);
        filter.setDateDebut(dateDebut);
        filter.setDateFin(dateFin);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<HistoriqueEntryDTO> result = historiqueService.getHistorique(filter, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reclamation/{reclamationId}")
    public ResponseEntity<List<HistoriqueEntryDTO>> getHistoriqueByReclamation(
            @PathVariable Long reclamationId) {
        List<HistoriqueEntryDTO> entries = historiqueService.getHistoriqueByReclamation(reclamationId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/stats")
    public ResponseEntity<HistoriqueStatsDTO> getHistoriqueStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        HistoriqueStatsDTO stats = historiqueService.getHistoriqueStats(dateDebut, dateFin);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/log")
    public ResponseEntity<Void> logAction(
            @RequestParam String action,
            @RequestParam String description,
            @RequestParam String utilisateur,
            @RequestParam String roleUtilisateur,
            @RequestParam(required = false) String adresseIP,
            @RequestParam(required = false) Long reclamationId) {

        historiqueService.logAction(action, description, utilisateur, roleUtilisateur, adresseIP, reclamationId);
        return ResponseEntity.ok().build();
    }
}