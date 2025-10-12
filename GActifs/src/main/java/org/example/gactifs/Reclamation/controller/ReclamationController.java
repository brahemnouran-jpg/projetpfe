package org.example.gactifs.Reclamation.controller;

import lombok.RequiredArgsConstructor;
import org.example.gactifs.Reclamation.Service.ReclamationService;
import org.example.gactifs.Reclamation.dto.ReclamationDTO;
import org.example.gactifs.Reclamation.model.Reclamation;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/reclamations")
@RequiredArgsConstructor
public class ReclamationController {

    private final ReclamationService reclamationService;

    // 🔹 Récupérer toutes les réclamations
    @GetMapping("/paginated")
    public Page<ReclamationDTO> getReclamationsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "dateCreation") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return reclamationService.getPaginated(page, size, sort, direction);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(reclamationService.getStats());
    }

    // 🔹 Réclamations en retard
    @GetMapping("/en-retard")
    public ResponseEntity<?> getReclamationsEnRetard() {
        return ResponseEntity.ok(reclamationService.getReclamationsEnRetard());
    }

    // 🔹 Rapport entre deux dates
    @GetMapping("/rapport")
    public ResponseEntity<?> getReclamationRapport(
            @RequestParam String dateDebut,
            @RequestParam String dateFin
    ) {
        return ResponseEntity.ok(reclamationService.getRapport(dateDebut, dateFin));
    }



    // 🔹 Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(reclamationService.getDashboardData());
    }

    // 🔹 SLA
    @GetMapping("/sla")
    public ResponseEntity<?> getSLAs() {
        return ResponseEntity.ok(reclamationService.getSLAs());
    }

    // 🔹 Notifications
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {
        return ResponseEntity.ok(reclamationService.getNotifications());
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchReclamations(
            @RequestParam(required = false) String objet,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String priorite,
            @RequestParam(required = false) String typeReclamation,
            @RequestParam(required = false) String reclamantEmail,
            @RequestParam(required = false) Long technicienId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String dateCreationDebut,
            @RequestParam(required = false) String dateCreationFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(reclamationService.search(objet, statut, priorite, typeReclamation,
                reclamantEmail, technicienId, serviceId, dateCreationDebut, dateCreationFin,
                page, size, sort, direction));
    }
    @GetMapping("/{id}")
    public ReclamationDTO getById(@PathVariable Long id) {
        return reclamationService.getById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam("objet") String objet,
            @RequestParam("description") String description,
            @RequestParam("typeReclamation") String typeReclamation,
            @RequestParam("priorite") String priorite,
            @RequestParam("reclamantNom") String reclamantNom,
            @RequestParam("reclamantEmail") String reclamantEmail,
            @RequestParam(value = "reclamantTelephone", required = false) String reclamantTelephone,
            @RequestParam(value = "dateEcheance", required = false) String dateEcheance,
            @RequestParam(value = "serviceId", required = false) Long serviceId,
            @RequestParam(value = "assetId", required = false) Long assetId,
            @RequestPart(value = "pieceJointes", required = false) List<MultipartFile> pieceJointes,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            // Vérification de l'authentification
            if (userDetails == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non authentifié"));
            }

            ReclamationDTO dto = new ReclamationDTO();
            dto.setObjet(objet);
            dto.setDescription(description);
            dto.setTypeReclamation(typeReclamation);
            dto.setPriorite(priorite);
            dto.setReclamantNom(reclamantNom);
            dto.setReclamantEmail(reclamantEmail);
            dto.setReclamantTelephone(reclamantTelephone);
            dto.setServiceDirectionId(serviceId);
            dto.setAssetId(assetId);
            if (dateEcheance != null && !dateEcheance.isEmpty()) {
                try {
                    dto.setDateEcheance(LocalDateTime.parse(dateEcheance));
                } catch (Exception e) {
                    dto.setDateEcheance(LocalDateTime.parse(dateEcheance + "T00:00:00"));
                }
            }

            String utilisateur = userDetails.getUsername();
            String roleUtilisateur = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .findFirst()
                    .orElse("ANONYME");

            ReclamationDTO createdReclamation = reclamationService.create(dto, utilisateur, roleUtilisateur);
            return ResponseEntity.ok(createdReclamation);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }




    // 🔹 Mettre à jour une réclamation
    @PutMapping("/{id}")
    public ReclamationDTO update(@PathVariable Long id, @RequestBody ReclamationDTO dto) {
        return reclamationService.update(id, dto);
    }

    // 🔹 Supprimer une réclamation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reclamationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 🔹 Changer le statut d’une réclamation
    @PatchMapping("/{id}/statut")
    public ReclamationDTO changerStatut(
            @PathVariable Long id,
            @RequestParam Reclamation.StatutReclamation statut
    ) {
        return reclamationService.changerStatut(id, statut);
    }
}
