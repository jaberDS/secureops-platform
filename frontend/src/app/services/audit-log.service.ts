import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuditLog {
  id: number;
  actor: string | null;
  action: string;
  resourceType: string;
  resourceId: number;
  timestamp: string;
  details: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {

  private readonly apiUrl =
    'http://localhost:9000/api/audit-logs';

  constructor(
    private http: HttpClient
  ) {}

  getLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(
      this.apiUrl
    );
  }

  getLogsByAction(
    action: string
  ): Observable<AuditLog[]> {

    const params =
      new HttpParams()
        .set('action', action);

    return this.http.get<AuditLog[]>(
      this.apiUrl,
      { params }
    );
  }

  getLogsByResourceType(
    resourceType: string
  ): Observable<AuditLog[]> {

    const params =
      new HttpParams()
        .set('resourceType', resourceType);

    return this.http.get<AuditLog[]>(
      this.apiUrl,
      { params }
    );
  }

  getLogsByActor(
    actor: string
  ): Observable<AuditLog[]> {

    const params =
      new HttpParams()
        .set('actor', actor);

    return this.http.get<AuditLog[]>(
      this.apiUrl,
      { params }
    );
  }
}
