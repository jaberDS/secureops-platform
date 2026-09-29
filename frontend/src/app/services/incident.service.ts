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

export interface UpdateIncidentStatusRequest {
  status: string;
}

export interface AssignIncidentRequest {
  analystEmail: string;
}

export interface InvestigationNote {
  id: number;
  content: string;
  createdByEmail: string;
}

export interface CreateInvestigationNoteRequest {
  content: string;
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
    return this.http.get<Incident[]>(
      this.apiUrl
    );
  }

  updateStatus(
    incidentId: number,
    status: string
  ): Observable<Incident> {

    const request: UpdateIncidentStatusRequest = {
      status
    };

    return this.http.patch<Incident>(
      `${this.apiUrl}/${incidentId}/status`,
      request
    );
  }

  assignIncident(
    incidentId: number,
    analystEmail: string
  ): Observable<Incident> {

    const request: AssignIncidentRequest = {
      analystEmail
    };

    return this.http.patch<Incident>(
      `${this.apiUrl}/${incidentId}/assign`,
      request
    );
  }

  getInvestigationNotes(
    incidentId: number
  ): Observable<InvestigationNote[]> {

    return this.http.get<InvestigationNote[]>(
      `${this.apiUrl}/${incidentId}/notes`
    );
  }

  createInvestigationNote(
    incidentId: number,
    content: string
  ): Observable<InvestigationNote> {

    const request: CreateInvestigationNoteRequest = {
      content
    };

    return this.http.post<InvestigationNote>(
      `${this.apiUrl}/${incidentId}/notes`,
      request
    );
  }
}