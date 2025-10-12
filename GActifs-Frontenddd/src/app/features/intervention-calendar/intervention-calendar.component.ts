import { Component, OnInit, signal, computed, OnDestroy, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterventionService } from '../../core/services/intervention.service';
import { Intervention } from '../../shared/models/Intervention';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { User, UserRole } from '../../shared/models/User';
import { UserService } from '../../core/services/UserService';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-intervention-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './intervention-calendar.component.html',
  styleUrls: ['./intervention-calendar.component.scss']
})
export class InterventionCalendarComponent implements OnInit, OnDestroy {
  // Injection de services avec inject()
  private interventionService = inject(InterventionService);
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  // Signaux
  currentDate = signal(new Date());
  selectedDate = signal(new Date());
  interventions = signal<Intervention[]>([]);
  selectedTechnicienId = signal<string | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  // Propriétés classiques
  availableTechniciens: User[] = [];
  currentUser: User | null = null;
  isAdmin = false;
  isTechnicien = false;

  // Computed properties
  monthName = computed(() => {
    return this.currentDate().toLocaleDateString('fr-FR', {
      month: 'long',
      year: 'numeric'
    });
  });

  calendarData = computed(() => {
    const date = this.currentDate();
    const year = date.getFullYear();
    const month = date.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    return { daysInMonth, startingDayOfWeek, year, month };
  });

  selectedDateInterventions = computed(() => {
    return this.getInterventionsForDate(this.selectedDate());
  });

  constructor() {
    console.log('%c🟢 Calendar Component Constructor', 'color: green; font-weight: bold; font-size: 14px;');
  }

  ngOnInit(): void {
    console.log('%c🟢 ngOnInit called', 'color: green; font-weight: bold; font-size: 14px;');

    // ✅ Utiliser AuthService pour récupérer l'utilisateur actuel
    this.authService.currentUser$
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((user) => {
          if (user) {
            this.currentUser = user;
            console.log('%c👤 Current user updated:', 'color: blue; font-size: 12px;', {
              id: user.id,
              email: user.email,
              role: user.role
            });

            // Mettre à jour les rôles
            this.updateUserRole();
            this.loadTechniciens();
            this.loadInterventions();
          }
        });

    // Fallback: si aucun utilisateur depuis AuthService, vérifier localStorage
    if (!this.currentUser) {
      this.checkUserRole();
      this.loadTechniciens();

      setTimeout(() => {
        console.log('%c🟡 About to load interventions', 'color: orange; font-weight: bold; font-size: 14px;');
        this.loadInterventions();
      }, 200);
    }
  }

  ngOnDestroy(): void {
    console.log('%c🔴 Component destroyed', 'color: red; font-weight: bold; font-size: 14px;');
  }

  // ✅ Nouvelle méthode pour mettre à jour le rôle
  private updateUserRole(): void {
    if (!this.currentUser) return;

    console.log('%c📋 updateUserRole() called', 'color: blue; font-size: 12px;');

    this.isAdmin = this.currentUser.role === UserRole.ADMIN;
    this.isTechnicien = this.currentUser.role === UserRole.TECHNICIEN;

    console.log('%c✅ User role updated:', 'color: green; font-size: 12px;', {
      role: this.currentUser.role,
      isAdmin: this.isAdmin,
      isTechnicien: this.isTechnicien,
      userId: this.currentUser.id
    });
  }

  checkUserRole(): void {
    console.log('%c📋 checkUserRole() called', 'color: blue; font-size: 12px;');

    // ✅ Récupérer l'utilisateur avec AuthService
    this.currentUser = this.authService.getCurrentUser();

    if (this.currentUser) {
      this.updateUserRole();

      console.log('%c✅ User loaded from AuthService', 'color: green; font-size: 12px;', {
        id: this.currentUser.id,
        email: this.currentUser.email,
        role: this.currentUser.role,
        isAdmin: this.isAdmin,
        isTechnicien: this.isTechnicien
      });
      return;
    }

    // Fallback: vérifier le rôle dans localStorage
    const roleStr = this.authService.getRole();
    const userId = this.authService.getCurrentUserId();

    console.log('Data from AuthService:', {
      role: roleStr,
      userId: userId
    });

    if (roleStr) {
      this.isAdmin = roleStr === 'ADMIN';
      this.isTechnicien = roleStr === 'TECHNICIEN';

      console.log('%c✅ Role loaded from AuthService', 'color: green; font-size: 12px;', {
        role: roleStr,
        userId: userId,
        isAdmin: this.isAdmin,
        isTechnicien: this.isTechnicien
      });

      // ✅ Créer un objet utilisateur minimal avec l'ID
      if (userId) {
        this.currentUser = { id: userId, role: roleStr as UserRole } as User;
        console.log('%c✅ User object created with ID:', 'color: green; font-size: 12px;', this.currentUser);
      }
      return;
    }

    console.warn('%c⚠️ No user or role found', 'color: orange; font-size: 12px;');
  }

  loadTechniciens(): void {
    if (this.isAdmin) {
      console.log('%c📥 Loading technicians', 'color: blue; font-size: 12px;');

      this.userService.getTechniciens()
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (techniciens) => {
              this.availableTechniciens = techniciens;
              console.log('%c✅ Technicians loaded:', 'color: green; font-size: 12px;', techniciens.length);
            },
            error: (error) => {
              console.error('%c❌ Error loading technicians:', 'color: red; font-size: 12px;', error);
            }
          });
    }
  }

  loadInterventions(): void {
    console.log('%c🔄 loadInterventions() called', 'color: purple; font-weight: bold; font-size: 12px;');
    this.loading.set(true);
    this.error.set(null);

    const startDate = new Date(
        this.currentDate().getFullYear(),
        this.currentDate().getMonth(),
        1,
        0, 0, 0, 0
    );

    const endDate = new Date(
        this.currentDate().getFullYear(),
        this.currentDate().getMonth() + 1,
        0,
        23, 59, 59, 999
    );

    console.log('%c📅 Date range:', 'color: purple; font-size: 12px;', {
      start: startDate.toISOString(),
      end: endDate.toISOString(),
      isAdmin: this.isAdmin,
      isTechnicien: this.isTechnicien,
      selectedTechnicienId: this.selectedTechnicienId(),
      currentUserId: this.currentUser?.id
    });

    // ✅ Cas 1: Admin sans filtre technicien
    if (this.isAdmin && !this.selectedTechnicienId()) {
      console.log('%c🔵 Case: ADMIN - Fetching ALL interventions', 'color: cyan; font-size: 12px;');

      this.interventionService.getInterventionCalendar(startDate, endDate)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (data) => {
              console.log('%c✅ Interventions received:', 'color: green; font-size: 12px;', data.length, data);
              this.interventions.set(data);
              this.loading.set(false);
            },
            error: (error) => {
              console.error('%c❌ Error loading interventions:', 'color: red; font-size: 12px;', error);
              this.error.set('Erreur lors du chargement des interventions');
              this.loading.set(false);
            }
          });
    }
    // ✅ Cas 2: Admin avec filtre technicien
    else if (this.isAdmin && this.selectedTechnicienId()) {
      console.log('%c🟡 Case: ADMIN with technician filter', 'color: gold; font-size: 12px;');

      this.interventionService.getInterventionCalendarByTechnicien(
          startDate,
          endDate,
          this.selectedTechnicienId()!
      )
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (data) => {
              console.log('%c✅ Filtered interventions received:', 'color: green; font-size: 12px;', data.length, data);
              this.interventions.set(data);
              this.loading.set(false);
            },
            error: (error) => {
              console.error('%c❌ Error loading filtered interventions:', 'color: red; font-size: 12px;', error);
              this.error.set('Erreur lors du chargement des interventions');
              this.loading.set(false);
            }
          });
    }
    // ✅ Cas 3: Technicien - Charger ses propres interventions avec son ID
    else if (this.isTechnicien && this.currentUser?.id) {
      console.log('%c🟢 Case: TECHNICIEN - Fetching own interventions', 'color: lightgreen; font-size: 12px;', {
        technicienId: this.currentUser.id,
        email: this.currentUser.email
      });

      this.interventionService.getInterventionCalendarByTechnicien(
          startDate,
          endDate,
          this.currentUser.id  // ✅ Utiliser l'ID de l'utilisateur connecté
      )
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (data) => {
              console.log('%c✅ Own interventions received:', 'color: green; font-size: 12px;', data.length, data);
              this.interventions.set(data);
              this.loading.set(false);
            },
            error: (error) => {
              console.error('%c❌ Error loading own interventions:', 'color: red; font-size: 12px;', error);
              this.error.set('Erreur lors du chargement de vos interventions');
              this.loading.set(false);
            }
          });
    }
    else {
      console.warn('%c⚠️ Case: Cannot determine which interventions to load', 'color: orange; font-size: 12px;', {
        isAdmin: this.isAdmin,
        isTechnicien: this.isTechnicien,
        hasCurrentUser: !!this.currentUser,
        currentUserId: this.currentUser?.id
      });
      this.loading.set(false);
    }
  }

  navigateMonth(direction: number): void {
    const newDate = new Date(this.currentDate());
    newDate.setMonth(newDate.getMonth() + direction);
    this.currentDate.set(newDate);
    console.log('%c📆 Month changed to:', 'color: purple; font-size: 12px;', newDate.toLocaleDateString('fr-FR'));
    this.loadInterventions();
  }

  selectDate(day: number): void {
    const date = new Date(
        this.calendarData().year,
        this.calendarData().month,
        day
    );
    this.selectedDate.set(date);
    console.log('%c📍 Date selected:', 'color: purple; font-size: 12px;', date.toDateString());
  }

  onTechnicienChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedTechnicienId.set(value || null);
    console.log('%c🧑‍🔧 Technician filter changed:', 'color: purple; font-size: 12px;', value || 'All');
    this.loadInterventions();
  }

  getInterventionsForDay(day: number): Intervention[] {
    const date = new Date(
        this.calendarData().year,
        this.calendarData().month,
        day
    );
    return this.getInterventionsForDate(date);
  }

  getInterventionsForDate(date: Date): Intervention[] {
    return this.interventions().filter(int => {
      const dateToUse = int.dateDebut || int.dateEcheance;

      if (!dateToUse) return false;

      const intDate = new Date(dateToUse);
      const isSameDay = intDate.toDateString() === date.toDateString();

      if (int.dateFin) {
        const endDate = new Date(int.dateFin);
        const isInRange = date >= intDate && date <= endDate;
        return isSameDay || isInRange;
      }

      return isSameDay;
    });
  }

  isToday(day: number): boolean {
    const date = new Date(
        this.calendarData().year,
        this.calendarData().month,
        day
    );
    return date.toDateString() === new Date().toDateString();
  }

  isSelected(day: number): boolean {
    const date = new Date(
        this.calendarData().year,
        this.calendarData().month,
        day
    );
    return date.toDateString() === this.selectedDate().toDateString();
  }

  getStatusColor(statut: string): string {
    const colors: Record<string, string> = {
      'PLANIFIEE': 'bg-blue-500',
      'EN_COURS': 'bg-yellow-500',
      'TERMINEE': 'bg-green-500',
      'SUSPENDUE': 'bg-orange-500',
      'ANNULEA': 'bg-red-500',
      'VALIDEE': 'bg-teal-500'
    };
    return colors[statut] || 'bg-gray-500';
  }

  getPriorityColor(priorite: string): string {
    const colors: Record<string, string> = {
      'CRITIQUE': 'border-red-500',
      'HAUTE': 'border-orange-500',
      'MOYENNE': 'border-yellow-500',
      'BASSE': 'border-blue-500'
    };
    return colors[priorite] || 'border-gray-300';
  }

  formatTime(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatDate(date: Date): string {
    return date.toLocaleDateString('fr-FR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long'
    });
  }

  getDayArray(): number[] {
    return Array.from({ length: this.calendarData().daysInMonth }, (_, i) => i + 1);
  }

  getEmptyDays(): number[] {
    return Array.from({ length: this.calendarData().startingDayOfWeek }, (_, i) => i);
  }
}