import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type ThreatSeverity =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'CRITICAL';

export type DetectionStatus =
  | 'NEW'
  | 'ACKNOWLEDGED'
  | 'RESOLVED';

export interface SecurityAlert {
  id: number;
  detectionRule: string;
  severity: ThreatSeverity;
  title: string;
  description: string;
  sourceEventId: number | null;
  timestamp: string;
  status: DetectionStatus;
  riskScore: number;
}

@Injectable({
  providedIn: 'root'
})
export class SecurityEventService {

  private readonly apiUrl =
    'http://localhost:9000/api/security';

  constructor(
    private http: HttpClient
  ) {}

  getAlerts(): Observable<SecurityAlert[]> {
    return this.http.get<SecurityAlert[]>(
      `${this.apiUrl}/alerts`
    );
  }

  getRiskScore(): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/risk`
    );
  }

  getAlert(id: number): Observable<SecurityAlert> {
    return this.http.get<SecurityAlert>(
      `${this.apiUrl}/alerts/${id}`
    );
  }
}
