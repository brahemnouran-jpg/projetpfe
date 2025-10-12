import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastrModule } from 'ngx-toastr';
import { routes } from './app.routes';
import {authInterceptor} from "./core/interceptors/AuthInterceptor";

export const appConfig: ApplicationConfig = {
    providers: [
        provideZoneChangeDetection({ eventCoalescing: true }),
        provideRouter(routes),
        // HttpClient avec Fetch API ET intercepteur
        provideHttpClient(
            withFetch(),
            withInterceptors([authInterceptor]) // Ajout de l'intercepteur
        ),
        // Import external modules
        importProvidersFrom(
            BrowserAnimationsModule,
            NgbModule,
            ToastrModule.forRoot({
                positionClass: 'toast-top-right',
                preventDuplicates: true,
                timeOut: 3000,
                closeButton: true,
                progressBar: true
            })
        ),
    ]
};