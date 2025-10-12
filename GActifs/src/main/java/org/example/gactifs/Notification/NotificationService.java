package org.example.gactifs.Notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsByUser(UUID utilisateurId, StatutNotification statut) {
        if (statut != null) {
            return notificationRepository.findByUtilisateurIdAndStatut(utilisateurId, statut);
        }
        return notificationRepository.findByUtilisateurId(utilisateurId);
    }

    public Notification envoyerNotification(Notification notification) {
        notification.setDateEnvoi(LocalDateTime.now());
        notification.setStatut(StatutNotification.NON_LUE);
        return notificationRepository.save(notification);
    }

    public void marquerCommeLue(Long id) {
        Optional<Notification> notifOpt = notificationRepository.findById(id);
        notifOpt.ifPresent(notification -> {
            notification.setStatut(StatutNotification.LUE);
            notificationRepository.save(notification);
        });
    }

    public void marquerToutesCommeLues(UUID utilisateurId) {
        List<Notification> notifications = notificationRepository.findByUtilisateurId(utilisateurId);
        notifications.forEach(n -> n.setStatut(StatutNotification.LUE));
        notificationRepository.saveAll(notifications);
    }

    public void archiverNotification(Long id) {
        Optional<Notification> notifOpt = notificationRepository.findById(id);
        notifOpt.ifPresent(notification -> {
            notification.setStatut(StatutNotification.ARCHIVEE);
            notificationRepository.save(notification);
        });
    }

    public void supprimerNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public Object getStats(UUID utilisateurId) {
        long nonLues = notificationRepository.countByUtilisateurIdAndStatut(utilisateurId, StatutNotification.NON_LUE);
        long lues = notificationRepository.countByUtilisateurIdAndStatut(utilisateurId, StatutNotification.LUE);
        long archivees = notificationRepository.countByUtilisateurIdAndStatut(utilisateurId, StatutNotification.ARCHIVEE);
        return new Object() {
            public final long total = nonLues + lues + archivees;
            public final long nonLuesCount = nonLues;
            public final long luesCount = lues;
            public final long archiveesCount = archivees;
        };
    }
}
