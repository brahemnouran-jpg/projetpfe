package org.example.gactifs.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/user/{utilisateurId}")
    public List<Notification> getNotificationsByUser(
            @PathVariable UUID utilisateurId,
            @RequestParam(required = false) StatutNotification statut) {
        return notificationService.getNotificationsByUser(utilisateurId, statut);
    }

    @PostMapping
    public Notification envoyerNotification(@RequestBody Notification notification) {
        return notificationService.envoyerNotification(notification);
    }

    @PatchMapping("/{id}/lue")
    public void marquerCommeLue(@PathVariable Long id) {
        notificationService.marquerCommeLue(id);
    }

    @PatchMapping("/user/{utilisateurId}/toutes-lues")
    public void marquerToutesCommeLues(@PathVariable UUID utilisateurId) {
        notificationService.marquerToutesCommeLues(utilisateurId);
    }

    @PatchMapping("/{id}/archiver")
    public void archiverNotification(@PathVariable Long id) {
        notificationService.archiverNotification(id);
    }

    @DeleteMapping("/{id}")
    public void supprimerNotification(@PathVariable Long id) {
        notificationService.supprimerNotification(id);
    }

    @GetMapping("/stats")
    public Object getStats(@RequestParam(required = false) UUID utilisateurId) {
        return notificationService.getStats(utilisateurId);
    }
}
