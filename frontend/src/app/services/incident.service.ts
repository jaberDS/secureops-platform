import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Incident {
  id: number;
  title: string;
  description: string;
  severity: string;
  category: string;
  status: string;
  reportedBy?: {
    id: number;
    email: string;
  };
  assignedTo?: {
    id: number;
    email: string;
  } | null;
}

@Injectable({
  providedIn: 'root'
})
export class IncidentService {

  private readonly apiUrl =
    'http://localhost:9000/api/incidents';

  constructor(
    private http: HttpClient
  ) {}

  getIncidents(): Observable<Incident[]> {
    return this.http.get<Incident[]>(this.apiUrl);
  }
}
