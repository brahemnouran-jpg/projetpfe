package org.example.gactifs.Historique;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueEntryDTO {
    private Long id;
    private String action;
    private String description;
    private String utilisateur;
    private String roleUtilisateur;
    private LocalDateTime dateAction;
    private String adresseIP;
    private String ancienneValeur;
    private String nouvelleValeur;
    private Map<String, String> details;
    private Long reclamationId;
    private String reclamationNumero;
}
