import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistoriqueEntry } from '../../shared/models/historique';
import { HistoriqueFilter, HistoriqueStats } from "../../features/historique/historique.component";
import { environment } from "../../../environments/environment";

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class HistoriqueService {
    private readonly apiUrl = `${environment.SERVER_API_URL_2}/historique`;

    constructor(private http: HttpClient) {}

    getHistorique(
        filter?: HistoriqueFilter,
        page: number = 0,
        size: number = 50,
        sort: string = 'dateAction',
        direction: string = 'desc'
    ): Observable<PageResponse<HistoriqueEntry>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sort', sort)
            .set('direction', direction);



        return this.http.get<PageResponse<HistoriqueEntry>>(this.apiUrl, { params });
    }
    getAllHistorique(): Observable<HistoriqueEntry[]> {
        return this.http.get<HistoriqueEntry[]>(`${this.apiUrl}/all`);
    }

    getHistoriqueByReclamation(reclamationId: number): Observable<HistoriqueEntry[]> {
        return this.http.get<HistoriqueEntry[]>(`${this.apiUrl}/reclamation/${reclamationId}`);
    }

    getHistoriqueStats(dateDebut?: Date, dateFin?: Date): Observable<HistoriqueStats> {
        let params = new HttpParams();

        if (dateDebut) {
            params = params.set('dateDebut', this.formatToLocalDateTime(dateDebut));
        }
        if (dateFin) {
            params = params.set('dateFin', this.formatToLocalDateTime(dateFin));
        }

        return this.http.get<HistoriqueStats>(`${this.apiUrl}/stats`, { params });
    }

    exportHistorique(filter?: HistoriqueFilter): Observable<Blob> {
        let params = new HttpParams();

        if (filter) {
            if (filter.reclamationId) {
                params = params.set('reclamationId', filter.reclamationId.toString());
            }
            if (filter.utilisateur) {
                params = params.set('utilisateur', filter.utilisateur);
            }
            if (filter.action) {
                params = params.set('action', filter.action);
            }
            if (filter.roleUtilisateur) {
                params = params.set('roleUtilisateur', filter.roleUtilisateur);
            }
            if (filter.dateDebut) {
                params = params.set('dateDebut', this.formatToLocalDateTime(filter.dateDebut));
            }
            if (filter.dateFin) {
                params = params.set('dateFin', this.formatToLocalDateTime(filter.dateFin));
            }
        }

        return this.http.get(`${this.apiUrl}/export`, {
            params,
            responseType: 'blob'
        });
    }

    logAction(
        action: string,
        description: string,
        utilisateur: string,
        roleUtilisateur: string,
        adresseIP?: string,
        reclamationId?: number
    ): Observable<void> {
        let params = new HttpParams()
            .set('action', action)
            .set('description', description)
            .set('utilisateur', utilisateur)
            .set('roleUtilisateur', roleUtilisateur);

        if (adresseIP) {
            params = params.set('adresseIP', adresseIP);
        }
        if (reclamationId) {
            params = params.set('reclamationId', reclamationId.toString());
        }

        return this.http.post<void>(`${this.apiUrl}/log`, null, { params });
    }

    /**
     * Formate une date JavaScript en format ISO 8601 compatible avec LocalDateTime
     * Format: yyyy-MM-ddTHH:mm:ss
     */
    private formatToLocalDateTime(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');

        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    }
}