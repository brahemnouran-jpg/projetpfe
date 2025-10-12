import {
    Component,
    OnInit,
    AfterViewInit,
    OnDestroy,
    Inject,
    PLATFORM_ID,
    ViewChild,
    ElementRef,
    Renderer2,
    HostListener
} from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [RouterOutlet, CommonModule, FormsModule, RouterLinkActive, RouterLink],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('menuToggle') menuToggle!: ElementRef<HTMLButtonElement>;
    @ViewChild('sidebar') sidebar!: ElementRef<HTMLDivElement>;
    @ViewChild('overlay') overlay!: ElementRef<HTMLDivElement>;

    private isBrowser: boolean;
    private menuClickUnlisten: (() => void) | null = null;
    private overlayClickUnlisten: (() => void) | null = null;
    private resizeUnlisten: (() => void) | null = null;

    sidebarOpen = false;
    isMobile = false;

    role: string | null = null; // rôle utilisateur

    constructor(
        private authService: AuthService,
        private renderer: Renderer2,
        private router: Router,
        @Inject(PLATFORM_ID) private platformId: Object
    ) {
        this.isBrowser = isPlatformBrowser(platformId);
    }

    ngOnInit(): void {
        if (this.isBrowser) {
            this.checkScreenSize();
            this.role = localStorage.getItem('role'); // récupère le rôle
        }
    }

    ngAfterViewInit(): void {
        if (!this.isBrowser) return;
        this.setupEventListeners();
    }

    ngOnDestroy(): void {
        if (this.menuClickUnlisten) this.menuClickUnlisten();
        if (this.overlayClickUnlisten) this.overlayClickUnlisten();
        if (this.resizeUnlisten) this.resizeUnlisten();
    }

    @HostListener('window:resize', ['$event'])
    onResize(event: any): void {
        if (!this.isBrowser) return;
        this.checkScreenSize();
        if (!this.isMobile && this.sidebarOpen) {
            this.closeSidebar();
        }
    }

    @HostListener('document:keydown.escape', ['$event'])
    onEscapeKey(event: KeyboardEvent): void {
        if (!this.isBrowser) return;
        if (this.sidebarOpen && this.isMobile) {
            this.closeSidebar();
        }
    }

    private checkScreenSize(): void {
        if (!this.isBrowser) return;

        this.isMobile = window.innerWidth < 1024;

        if (!this.isMobile) {
            this.sidebarOpen = true;
            this.sidebar?.nativeElement.classList.add('open');
            this.overlay?.nativeElement?.classList.remove('active');
        } else {
            this.sidebarOpen = false;
            this.sidebar?.nativeElement.classList.remove('open');
            this.overlay?.nativeElement?.classList.remove('active');
        }
    }

    private setupEventListeners(): void {
        if (this.menuToggle && this.sidebar && this.overlay) {
            this.menuClickUnlisten = this.renderer.listen(
                this.menuToggle.nativeElement,
                'click',
                () => this.toggleSidebar()
            );

            this.overlayClickUnlisten = this.renderer.listen(
                this.overlay.nativeElement,
                'click',
                () => this.closeSidebar()
            );
        }

        this.resizeUnlisten = this.renderer.listen('window', 'resize', () => {
            this.onResize(null);
        });

        this.renderer.listen('document', 'click', (event) => {
            const target = event.target as HTMLElement;
            if (target.classList.contains('nav-link') && this.isMobile) {
                setTimeout(() => {
                    this.closeSidebar();
                }, 200);
            }
        });
    }

    toggleSidebar(): void {
        if (!this.isBrowser) return;
        this.sidebarOpen = !this.sidebarOpen;
        if (this.sidebar?.nativeElement && this.overlay?.nativeElement) {
            if (this.sidebarOpen) {
                this.sidebar.nativeElement.classList.add('open');
                this.overlay.nativeElement.classList.add('active');
            } else {
                this.sidebar.nativeElement.classList.remove('open');
                this.overlay.nativeElement.classList.remove('active');
            }
        }
    }

    closeSidebar(): void {
        if (!this.isBrowser) return;
        if (this.isMobile) {
            this.sidebarOpen = false;
            this.sidebar?.nativeElement.classList.remove('open');
            this.overlay?.nativeElement.classList.remove('active');
        }
    }

    openSidebar(): void {
        if (!this.isBrowser) return;
        this.sidebarOpen = true;
        this.sidebar?.nativeElement.classList.add('open');
        this.overlay?.nativeElement.classList.add('active');
    }

    logout(): void {
        if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
            this.authService.logout();
        }
    }

    // Vérifie si l’utilisateur a accès à un menu
    hasAccess(roles: string[]): boolean {
        return this.role !== null && roles.includes(this.role);
    }
}
