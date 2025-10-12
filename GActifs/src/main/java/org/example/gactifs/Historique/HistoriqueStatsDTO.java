package org.example.gactifs.Historique;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class HistoriqueStatsDTO {
    private long totalActions;
    private Map<String, Long> actionsParType;
    private Map<String, Long> actionsParUtilisateur;
    private List<ActionParJour> actionsParJour;
    private long utilisateursActifs;

    @Data
    @AllArgsConstructor
    public static class ActionParJour {
        private String date;
        private long count;
    }
}
