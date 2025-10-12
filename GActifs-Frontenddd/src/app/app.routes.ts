import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AssetManagementComponent } from './features/asset-management/asset-management.component';
import {DashboardComponent} from "./features/dashboard/dashboard/dashboard.component";
import {ServiceDirectionComponent} from "./features/service-direction/service-direction.component";
import {UserManagementComponent} from "./features/user-management/user-management.component";
import {CategoryManagementComponent} from "./features/category-management/category-management.component";
import {InterventionComponent} from "./features/intervention/intervention.component";
import {ReclamationManagementComponent} from "./features/reclamation-management/reclamation-management.component";
import {HistoriqueComponent} from "./features/historique/historique.component";
import {NotificationsComponent} from "./features/notifications/notifications.component";
import {InterventionCalendarComponent} from "./features/intervention-calendar/intervention-calendar.component";

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
        import('./features/auth/login/login.component').then(m => m.LoginComponent),
    title: 'Connexion - '
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    title: 'Dashboard - ',
    children: [
      {
        path: 'assetmanagement',
        component: AssetManagementComponent,
        title: 'Gestion des actifs'
      },
      {
        path: 'aservicedirection',
        component: ServiceDirectionComponent,
        title: 'Gestion Des Direction'
      },
      {
        path: 'categories',
        component: CategoryManagementComponent,
        title: 'Gestion Catégories'
      },
      {
        path: 'interventions',
        component: InterventionComponent,
        title: 'Gestion Interventions'
      },
      {
        path: 'reclamations',
        component: ReclamationManagementComponent,
        title: 'Gestion reclamations'
      },
      {
        path: 'users',
        component: UserManagementComponent,
        title: 'Gestion Des Utulisateurs'
      }, {
        path: 'Calendar',
        component: InterventionCalendarComponent,
        title: 'Gestion Des Calendar'
      },
      {
        path: 'notifications',
        component: NotificationsComponent,
        title: 'Gestion Des notifications'
      },
      {
        path: 'historique',
        component: HistoriqueComponent,
        title: 'Gestion Des historique'
      },
      {
        path: '',
        redirectTo: () => {
          const role = localStorage.getItem('role');
          switch (role) {
            case 'ADMIN': return '/dashboard/assetmanagement';
            case 'TECHNICIEN': return '/dashboard/interventions';
            case 'RESPONSABLE': return '/dashboard/reclamations';
            case 'AGENT': return '/dashboard/Calendar';
            default: return '/login';
          }
        },
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
