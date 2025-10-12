export interface HistoriqueEntry {
    id: number;
    action: string; // TypeAction
    description: string;
    utilisateur: string;
    roleUtilisateur: string; // RoleUtilisateur
    dateAction: Date;
    adresseIP?: string;
    ancienneValeur?: string;
    nouvelleValeur?: string;
    details?: { [key: string]: string };
    reclamationId?: number;
    reclamationNumero?: string;
}