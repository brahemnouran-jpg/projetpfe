package org.example.gactifs.Notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", columnDefinition = "CHAR(36)")
    private UUID utilisateurId;

    // ID du destinataire de la notification
    private String titre;
    private String message;

    @Enumerated(EnumType.STRING)
    private StatutNotification statut;

    private LocalDateTime dateEnvoi;
    private String adresseIP;
}