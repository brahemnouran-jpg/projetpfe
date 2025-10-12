package org.example.gactifs.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUtilisateurId(UUID utilisateurId);
    List<Notification> findByUtilisateurIdAndStatut(UUID utilisateurId, StatutNotification statut);
    long countByUtilisateurIdAndStatut(UUID utilisateurId, StatutNotification statut);
}
