import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe, DatePipe, DecimalPipe, NgClass, NgForOf, NgIf, SlicePipe } from '@angular/common';

import {
  Reclamation,
  TypeReclamation,
  PrioriteReclamation,
  StatutReclamation,
  ReclamationCreateRequest,
  ReclamationUpdateRequest,
  ReclamationFilter,
  ReclamationStats,
  CommentaireReclamation
} from '../../shared/models/reclamation.model';
import { Asset } from '../../shared/models/asset.model';
import { ServiceDirection } from '../../shared/models/service-direction.model';
import { ReclamationService } from '../../core/services/reclamation.service';
import { AssetService } from '../../core/services/asset.service';
import { ServiceDirectionService } from '../../core/services/service-direction.service';
import { User, UserRole } from '../../shared/models/User';
import { UserService } from '../../core/services/UserService';

@Component({
  selector: 'app-reclamation-management',
  templateUrl: './reclamation-management.component.html',
  imports: [
    FormsModule,
    NgClass,
    DatePipe,
    CurrencyPipe,
    NgIf,
    NgForOf,
    SlicePipe
  ],
  styleUrls: ['./reclamation-management.component.scss']
})
export class ReclamationManagementComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  // Data
  reclamations: Reclamation[] = [];
  filteredReclamations: Reclamation[] = [];
  paginatedReclamations: Reclamation[] = [];
  techniciens: User[] = [];
  assets: Asset[] = [];
  services: ServiceDirection[] = [];
  users: User[] = [];

  // Modal states
  showReclamationModal = false;
  showCommentModal = false;
  showDetailsModal = false;
  showUserModal = false;
  isEditMode = false;
  currentReclamation: Partial<Reclamation> = {};
  saving = false;

  // Dropdown state
  openDropdownId: number | null = null;

  // Form binding IDs
  selectedServiceId: number | null = null;
  selectedAssetId: number | null = null;
  selectedTechnicienId: number | null = null;

  // Comments
  selectedReclamationForComment: Reclamation | null = null;
  commentaires: CommentaireReclamation[] = [];
  newComment = '';

  // Filters
  filters: ReclamationFilter = {};
  searchTerm = '';

  // Selection
  selectedReclamations = new Set<number>();

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;

  // Sorting
  sortField = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Statistics
  stats: ReclamationStats = {
    total: 0,
    nouvelles: 0,
    enCours: 0,
    enAttente: 0,
    resolues: 0,
    fermees: 0,
    enRetard: 0,
    tempsMoyenResolution: 0,
    tauxSatisfaction: 0
  };

  // Enums for template
  TypeReclamation = TypeReclamation;
  PrioriteReclamation = PrioriteReclamation;
  StatutReclamation = StatutReclamation;
  UserRole = UserRole;

  Math = Math;
  Object = Object;

  constructor(
      private reclamationService: ReclamationService,
      private userService: UserService,
      private assetService: AssetService,
      private serviceDirectionService: ServiceDirectionService
  ) {}

  ngOnInit(): void {
    this.initializeComponent();
    this.setupSearchDebounce();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.openDropdownId = null;
  }

  toggleDropdown(reclamationId: number, event: Event): void {
    event.stopPropagation();
    this.openDropdownId = this.openDropdownId === reclamationId ? null : reclamationId;
  }

  toggleInterventionSelection(id: number): void {
    if (this.selectedReclamations.has(id)) {
      this.selectedReclamations.delete(id);
    } else {
      this.selectedReclamations.add(id);
    }
  }

  sort(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.loadReclamations();
  }

  private initializeComponent(): void {
    this.loadReclamations();
    this.loadTechniciens();
    this.loadAssets();
    this.loadServices();
    this.loadStatistics();
    this.loadUsers();
  }

  private setupSearchDebounce(): void {
    this.searchSubject
        .pipe(
            debounceTime(300),
            distinctUntilChanged(),
            takeUntil(this.destroy$)
        )
        .subscribe(() => {
          this.applyFilters();
        });
  }

  private loadReclamations(): void {
    const pageRequest = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sort: this.sortField,
      direction: this.sortDirection
    };

    this.reclamationService.getReclamationsPaginated(pageRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            this.reclamations = response.content;
            this.totalPages = response.totalPages;
            this.applyFilters();
          },
          error: (error) => {
            console.error('Erreur lors du chargement des réclamations:', error);
          }
        });
  }

  private loadTechniciens(): void {
    this.userService.getUsersByRole(UserRole.TECHNICIEN)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (users: User[]) => {
            this.techniciens = users.filter(user => user.isActive);
          },
          error: (error) => {
            console.error('Erreur lors du chargement des techniciens:', error);
          }
        });
  }

  private loadUsers(): void {
    this.userService.getAllUsers()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (users) => {
            this.users = users;
          },
          error: (error) => {
            console.error('Erreur lors du chargement des utilisateurs:', error);
          }
        });
  }

  private loadAssets(): void {
    this.assetService.getAllAssets()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (assets) => {
            this.assets = assets;
          },
          error: (error) => {
            console.error('Erreur lors du chargement des actifs:', error);
          }
        });
  }

  private loadServices(): void {
    this.serviceDirectionService.getAllServices()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (services) => {
            this.services = services;
          },
          error: (error) => {
            console.error('Erreur lors du chargement des services:', error);
          }
        });
  }

  private loadStatistics(): void {
    this.reclamationService.getReclamationStats()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (stats) => {
            this.stats = stats;
          },
          error: (error) => {
            console.error('Erreur lors du chargement des statistiques:', error);
          }
        });
  }

  onFilterChange(): void {
    if (this.searchTerm !== undefined) {
      this.searchSubject.next(this.searchTerm);
    } else {
      this.applyFilters();
    }
  }

  private applyFilters(): void {
    let filtered = [...this.reclamations];

    if (this.searchTerm) {
      const searchLower = this.searchTerm.toLowerCase();
      filtered = filtered.filter(reclamation =>
          reclamation.objet?.toLowerCase().includes(searchLower) ||
          reclamation.description?.toLowerCase().includes(searchLower) ||
          reclamation.reclamantNom?.toLowerCase().includes(searchLower) ||
          reclamation.reclamantEmail?.toLowerCase().includes(searchLower) ||
          reclamation.numero?.toLowerCase().includes(searchLower)
      );
    }

    if (this.filters.statut) {
      filtered = filtered.filter(r => r.statut === this.filters.statut);
    }
    if (this.filters.priorite) {
      filtered = filtered.filter(r => r.priorite === this.filters.priorite);
    }
    if (this.filters.typeReclamation) {
      filtered = filtered.filter(r => r.typeReclamation === this.filters.typeReclamation);
    }

    this.filteredReclamations = filtered;
    this.currentPage = 1;
    this.updatePagination();
  }

  clearFilters(): void {
    this.filters = {};
    this.searchTerm = '';
    this.applyFilters();
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.currentReclamation = {
      priorite: PrioriteReclamation.MOYENNE,
      typeReclamation: TypeReclamation.TECHNIQUE
    };
    this.selectedServiceId = null;
    this.selectedAssetId = null;
    this.selectedTechnicienId = null;
    this.showReclamationModal = true;
  }

  openEditModal(reclamation: Reclamation): void {
    this.currentReclamation = { ...reclamation };
    this.selectedServiceId = reclamation.serviceDirection?.id ?? null;
    this.selectedAssetId = reclamation.asset?.id ?? null;
    this.isEditMode = true;
    this.showReclamationModal = true;
  }

  openDetailsModal(reclamation: Reclamation): void {
    this.currentReclamation = { ...reclamation };
    this.showDetailsModal = true;
    this.loadCommentaires(reclamation);
  }

  openCommentModal(reclamation: Reclamation): void {
    this.selectedReclamationForComment = reclamation;
    this.newComment = '';
    this.showCommentModal = true;
  }

  closeModal(): void {
    this.showReclamationModal = false;
    this.showDetailsModal = false;
    this.showCommentModal = false;
    this.showUserModal = false;
    this.currentReclamation = {};
    this.selectedReclamationForComment = null;
    this.newComment = '';
    this.selectedServiceId = null;
    this.selectedAssetId = null;
    this.selectedTechnicienId = null;
  }

  saveReclamation(): void {
    if (!this.currentReclamation.objet || !this.currentReclamation.description) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    this.saving = true;

    if (this.isEditMode && this.currentReclamation.id) {
      const updateRequest: ReclamationUpdateRequest = {
        objet: this.currentReclamation.objet,
        description: this.currentReclamation.description,
        typeReclamation: this.currentReclamation.typeReclamation,
        priorite: this.currentReclamation.priorite,
        statut: this.currentReclamation.statut
      };

      this.reclamationService.updateReclamation(this.currentReclamation.id, updateRequest)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              if (this.selectedTechnicienId && this.currentReclamation.id) {
                this.assignerTechnicien(this.currentReclamation as Reclamation, this.selectedTechnicienId);
              }
              this.loadReclamations();
              this.loadStatistics();
              this.closeModal();
              this.saving = false;
            },
            error: (error) => {
              console.error('Erreur lors de la mise à jour:', error);
              alert('Erreur lors de la mise à jour de la réclamation');
              this.saving = false;
            }
          });
    } else {
      const createRequest: ReclamationCreateRequest = {
        objet: this.currentReclamation.objet!,
        description: this.currentReclamation.description!,
        typeReclamation: this.currentReclamation.typeReclamation!,
        priorite: this.currentReclamation.priorite!,
        reclamantNom: this.currentReclamation.reclamantNom!,
        reclamantEmail: this.currentReclamation.reclamantEmail!,
        reclamantTelephone: this.currentReclamation.reclamantTelephone
      };

      this.reclamationService.createReclamation(createRequest)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.loadReclamations();
              this.loadStatistics();
              this.closeModal();
              this.saving = false;
            },
            error: (error) => {
              console.error('Erreur lors de la création:', error);
              alert('Erreur lors de la création de la réclamation');
              this.saving = false;
            }
          });
    }
  }

  deleteReclamation(reclamation: Reclamation): void {
    if (reclamation.id && confirm('Êtes-vous sûr de vouloir supprimer cette réclamation ?')) {
      this.reclamationService.deleteReclamation(reclamation.id)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.loadReclamations();
              this.loadStatistics();
            },
            error: (error) => {
              console.error('Erreur lors de la suppression:', error);
              alert('Erreur lors de la suppression de la réclamation');
            }
          });
    }
  }

  changerStatut(reclamation: Reclamation, nouveauStatut: string): void {
    if (!reclamation.id) return;

    this.reclamationService.changerStatut(reclamation.id, nouveauStatut as StatutReclamation)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.loadReclamations();
            this.loadStatistics();
          },
          error: (error) => {
            console.error('Erreur lors du changement de statut:', error);
            alert('Erreur lors du changement de statut');
          }
        });
  }

  assignerTechnicien(reclamation: Reclamation, technicienId: number): void {
    if (!reclamation.id) return;

    this.reclamationService.assignerTechnicien(reclamation.id, technicienId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.loadReclamations();
          },
          error: (error) => {
            console.error('Erreur lors de l\'assignation:', error);
            alert('Erreur lors de l\'assignation du technicien');
          }
        });
  }

  private loadCommentaires(reclamation: Reclamation): void {
    if (!reclamation.id) return;

    this.reclamationService.getCommentaires(reclamation.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (commentaires) => {
            this.commentaires = commentaires;
          },
          error: (error) => {
            console.error('Erreur lors du chargement des commentaires:', error);
            this.commentaires = [];
          }
        });
  }

  ajouterCommentaire(): void {
    if (!this.selectedReclamationForComment?.id || !this.newComment.trim()) {
      alert('Veuillez saisir un commentaire');
      return;
    }

    this.reclamationService.ajouterCommentaire(
        this.selectedReclamationForComment.id,
        this.newComment
    )
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            if (this.showDetailsModal && this.selectedReclamationForComment) {
              this.loadCommentaires(this.selectedReclamationForComment);
            }
            this.closeModal();
          },
          error: (error) => {
            console.error('Erreur lors de l\'ajout du commentaire:', error);
            alert('Erreur lors de l\'ajout du commentaire');
          }
        });
  }

  getPrioriteColor(priorite?: PrioriteReclamation): string {
    if (!priorite) return '#6b7280';
    return this.reclamationService.getPrioriteColor(priorite);
  }

  getStatutColor(statut?: StatutReclamation): string {
    if (!statut) return '#6b7280';
    return this.reclamationService.getStatutColor(statut);
  }

  isEnRetard(reclamation: Reclamation): boolean {
    if (!reclamation.dateEcheance || reclamation.statut === StatutReclamation.RESOLUE) {
      return false;
    }
    return this.reclamationService.isEnRetard(reclamation);
  }



  formatDuration(heures: number): string {
    if (heures < 24) {
      return `${heures}h`;
    }
    const jours = Math.floor(heures / 24);
    const heuresRestantes = heures % 24;
    return heuresRestantes > 0 ? `${jours}j ${heuresRestantes}h` : `${jours}j`;
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredReclamations.length / this.pageSize);
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedReclamations = this.filteredReclamations.slice(startIndex, endIndex);
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePagination();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(this.totalPages, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  }

  toggleSelectAll(): void {
    if (this.isAllSelected()) {
      this.selectedReclamations.clear();
    } else {
      this.paginatedReclamations.forEach(r => {
        if (r.id) this.selectedReclamations.add(r.id);
      });
    }
  }

  isAllSelected(): boolean {
    return this.paginatedReclamations.length > 0 &&
        this.paginatedReclamations.every(r => r.id && this.selectedReclamations.has(r.id));
  }

  exportReclamations(format: 'excel' | 'pdf'): void {
    this.reclamationService.exporterReclamations(format, this.filters)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (blob) => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `reclamations_${new Date().getTime()}.${format}`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
          },
          error: (error) => {
            console.error('Erreur lors de l\'export:', error);
            alert('Erreur lors de l\'export');
          }
        });
  }
}