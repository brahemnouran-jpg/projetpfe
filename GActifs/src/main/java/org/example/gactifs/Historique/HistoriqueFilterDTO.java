package org.example.gactifs.Historique;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistoriqueFilterDTO {
    private Long reclamationId;
    private String utilisateur;
    private String action;
    private String roleUtilisateur;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String searchTerm;
}