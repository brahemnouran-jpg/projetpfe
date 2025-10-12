// historique.component.ts
import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { HistoriqueEntry } from "../../shared/models/historique";
import { HistoriqueService, PageResponse } from "../../core/services/HistoriqueService";

export enum TypeAction {
  CREATION = 'CREATION',
  MODIFICATION = 'MODIFICATION',
  CHANGEMENT_STATUT = 'CHANGEMENT_STATUT',
  ASSIGNATION = 'ASSIGNATION',
  COMMENTAIRE = 'COMMENTAIRE',
  PIECE_JOINTE = 'PIECE_JOINTE',
  RESOLUTION = 'RESOLUTION',
  FERMETURE = 'FERMETURE',
  SUPPRESSION = 'SUPPRESSION',
  EXPORT = 'EXPORT',
  CONSULTATION = 'CONSULTATION'
}

export enum RoleUtilisateur {
  ADMIN = 'ADMIN',
  TECHNICIEN = 'TECHNICIEN',
  RECLAMANT = 'RECLAMANT',
  SUPERVISEUR = 'SUPERVISEUR',
  SYSTEM = 'SYSTEM'
}

export interface HistoriqueFilter {
  reclamationId?: number;
  utilisateur?: string;
  action?: TypeAction;
  roleUtilisateur?: RoleUtilisateur;
  dateDebut?: Date;
  dateFin?: Date;
}

export interface HistoriqueStats {
  totalActions: number;
  actionsParType: { [key: string]: number };
  actionsParUtilisateur: { [key: string]: number };
  actionsParJour: { date: string; count: number }[];
  utilisateursActifs: number;
}

@Component({
  selector: 'app-historique',
  templateUrl: './historique.component.html',
  imports: [
    FormsModule,
    NgClass,
    DatePipe,
    NgIf,
    NgForOf
  ],
  styleUrls: ['./historique.component.scss']
})
export class HistoriqueComponent implements OnInit, OnDestroy {
  @Input() reclamationId?: number;
  @Input() mode: 'full' | 'widget' = 'full';

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  // Data
  historiqueEntries: HistoriqueEntry[] = [];
  totalElements = 0;

  // Stats
  stats: HistoriqueStats = {
    totalActions: 0,
    actionsParType: {},
    actionsParUtilisateur: {},
    actionsParJour: [],
    utilisateursActifs: 0
  };

  // Filters
  filters: HistoriqueFilter = {};
  searchTerm = '';
  dateDebutFilter: string = '';
  dateFinFilter: string = '';

  // Pagination - Backend pagination
  currentPage = 0; // Backend utilise 0-indexed
  pageSize = 20;
  totalPages = 0;
  loading = false;

  // Sorting
  sortField = 'dateAction';
  sortDirection: 'asc' | 'desc' = 'desc';

  // View options
  showDetails = false;
  groupByDate = false;
  showStats = true;

  // Enums for template
  TypeAction = TypeAction;
  RoleUtilisateur = RoleUtilisateur;
  Math = Math;
  Object = Object;

  constructor(private historiqueService: HistoriqueService) {}

  ngOnInit(): void {
    this.initializeComponent();
    this.setupSearchDebounce();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeComponent(): void {

      this.loadHistorique();

  }

  private setupSearchDebounce(): void {
    this.searchSubject
        .pipe(
            debounceTime(300),
            distinctUntilChanged(),
            takeUntil(this.destroy$)
        )
        .subscribe(() => {
          this.currentPage = 0; // Reset to first page
          this.loadHistorique();
        });
  }

  // Data Loading sans pagination (tous les historiques)
  private loadHistorique(): void {
    this.loading = true;

    this.historiqueService.getAllHistorique()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: HistoriqueEntry[]) => {
            this.historiqueEntries = response;
            this.totalPages = 1;        // Comme tout est chargé, une seule "page"
            this.totalElements = response.length;
            this.loading = false;
          },
          error: (error: any) => {
            console.error('Erreur lors du chargement de l\'historique:', error);
            this.loading = false;
          }
        });
  }



  private loadStats(): void {
    const dateDebut = this.dateDebutFilter ? new Date(this.dateDebutFilter) : undefined;
    const dateFin = this.dateFinFilter ? new Date(this.dateFinFilter) : undefined;

    this.historiqueService.getHistoriqueStats(dateDebut, dateFin)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (stats: HistoriqueStats) => {
            this.stats = stats;
          },
          error: (error: any) => {
            console.error('Erreur lors du chargement des statistiques:', error);
          }
        });
  }


  // Pagination
  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadHistorique();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    const currentPageDisplay = this.currentPage + 1; // Pour l'affichage (1-indexed)

    let startPage = Math.max(0, this.currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  }

  // Utility Methods
  getActionIcon(action: TypeAction): string {
    const icons = {
      [TypeAction.CREATION]: 'fa-plus-circle',
      [TypeAction.MODIFICATION]: 'fa-edit',
      [TypeAction.CHANGEMENT_STATUT]: 'fa-exchange-alt',
      [TypeAction.ASSIGNATION]: 'fa-user-tag',
      [TypeAction.COMMENTAIRE]: 'fa-comment',
      [TypeAction.PIECE_JOINTE]: 'fa-paperclip',
      [TypeAction.RESOLUTION]: 'fa-check-circle',
      [TypeAction.FERMETURE]: 'fa-lock',
      [TypeAction.SUPPRESSION]: 'fa-trash',
      [TypeAction.EXPORT]: 'fa-download',
      [TypeAction.CONSULTATION]: 'fa-eye'
    };
    return icons[action] || 'fa-info-circle';
  }

  getActionColor(action: TypeAction): string {
    const colors = {
      [TypeAction.CREATION]: '#28a745',
      [TypeAction.MODIFICATION]: '#007bff',
      [TypeAction.CHANGEMENT_STATUT]: '#ffc107',
      [TypeAction.ASSIGNATION]: '#17a2b8',
      [TypeAction.COMMENTAIRE]: '#6f42c1',
      [TypeAction.PIECE_JOINTE]: '#fd7e14',
      [TypeAction.RESOLUTION]: '#20c997',
      [TypeAction.FERMETURE]: '#6c757d',
      [TypeAction.SUPPRESSION]: '#dc3545',
      [TypeAction.EXPORT]: '#e83e8c',
      [TypeAction.CONSULTATION]: '#6c757d'
    };
    return colors[action] || '#6c757d';
  }

  getRoleIcon(role: RoleUtilisateur): string {
    const icons = {
      [RoleUtilisateur.ADMIN]: 'fa-user-shield',
      [RoleUtilisateur.TECHNICIEN]: 'fa-user-cog',
      [RoleUtilisateur.RECLAMANT]: 'fa-user',
      [RoleUtilisateur.SUPERVISEUR]: 'fa-user-tie',
      [RoleUtilisateur.SYSTEM]: 'fa-robot'
    };
    return icons[role] || 'fa-user';
  }

  formatActionDescription(entry: HistoriqueEntry): string {
    let description = entry.description;

    if (entry.ancienneValeur && entry.nouvelleValeur) {
      description += ` (${entry.ancienneValeur} → ${entry.nouvelleValeur})`;
    }

    return description;
  }

  getRelativeTime(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - new Date(date).getTime();
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    const months = Math.floor(days / 30);
    const years = Math.floor(days / 365);

    if (years > 0) return `il y a ${years} an${years > 1 ? 's' : ''}`;
    if (months > 0) return `il y a ${months} mois`;
    if (days > 0) return `il y a ${days} jour${days > 1 ? 's' : ''}`;
    if (hours > 0) return `il y a ${hours} heure${hours > 1 ? 's' : ''}`;
    if (minutes > 0) return `il y a ${minutes} minute${minutes > 1 ? 's' : ''}`;
    return 'à l\'instant';
  }

  groupEntriesByDate(): { [date: string]: HistoriqueEntry[] } {
    return this.historiqueEntries.reduce((groups, entry) => {
      const date = new Date(entry.dateAction).toDateString();
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(entry);
      return groups;
    }, {} as { [date: string]: HistoriqueEntry[] });
  }

  toggleStats(): void {
    this.showStats = !this.showStats;
    if (this.showStats) {
      this.loadStats();
    }
  }
}