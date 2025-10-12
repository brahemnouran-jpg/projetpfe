
// role-redirect.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class RoleRedirectGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    const role = localStorage.getItem('role'); // récupère le rôle stocké au login

    switch (role) {
      case 'ADMIN':
        this.router.navigate(['/dashboard/assetmanagement']);
        break;
      case 'TECHNICIEN':
        this.router.navigate(['/dashboard/interventions']);
        break;
      case 'RESPONSABLE':
        this.router.navigate(['/dashboard/reclamations']);
        break;
      case 'AGENT':
        this.router.navigate(['/dashboard/calendar']);
        break;
      default:
        this.router.navigate(['/login']);
        break;
    }

    return false; // on bloque l'accès direct car on redirige toujours
  }
}
