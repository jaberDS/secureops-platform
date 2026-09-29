import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { FormsModule } from '@angular/forms';

import { Router } from '@angular/router';

import {
  Incident,
  IncidentService,
  InvestigationNote
} from '../../services/incident.service';

import {
  User,
  UserService
} from '../../services/user.service';

@Component({
  selector: 'app-incidents',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './incidents.html',
  styleUrl: './incidents.css'
})
export class Incidents implements OnInit {

  incidents: Incident[] = [];

  filteredIncidents: Incident[] = [];

  selectedIncident: Incident | null = null;

  analysts: User[] = [];

  investigationNotes: InvestigationNote[] = [];

  newNoteContent = '';

  selectedAnalystEmail = '';

  selectedStatus = '';

  searchTerm = '';

  severityFilter = '';

  statusFilter = '';

  isLoading = false;
  isLoadingAnalysts = false;

  isLoadingNotes = false;
  isCreatingNote = false;

  isUpdatingStatus = false;
  isAssigning = false;

  loadError = '';
  analystsError = '';

  notesError = '';

  actionSuccess = '';
  actionError = '';

  constructor(
    private incidentService: IncidentService,
    private userService: UserService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadIncidents();
    this.loadAnalysts();
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  loadIncidents(): void {
    this.isLoading = true;
    this.loadError = '';

    this.incidentService.getIncidents().subscribe({
      next: (incidents) => {

        this.incidents = [...incidents];

        this.filteredIncidents = [...this.incidents];

        if (this.selectedIncident) {

          const updatedIncident =
            this.incidents.find(
              incident =>
                incident.id === this.selectedIncident?.id
            );

          this.selectedIncident =
            updatedIncident ?? null;

          if (this.selectedIncident) {

            this.selectedStatus =
              this.getNextStatus(
                this.selectedIncident.status
              );

            this.selectedAnalystEmail =
              this.selectedIncident.assignedTo?.email ?? '';
          }
        }

        this.isLoading = false;

        this.applyFilters();

        this.cdr.markForCheck();
      },

      error: (error) => {

        console.error(
          'Incidents API failed:',
          error.status,
          error.error
        );

        this.isLoading = false;

        switch (error?.status) {

          case 401:
            this.loadError =
              'Authentication failed. Your session may have expired.';
            break;

          case 403:
            this.loadError =
              'Access denied. You do not have permission to view incidents.';
            break;

          case 0:
            this.loadError =
              'Cannot connect to the backend.';
            break;

          default:
            this.loadError =
              'Unable to load incidents.';
        }

        this.cdr.markForCheck();
      }
    });
  }

  loadAnalysts(): void {

    this.isLoadingAnalysts = true;
    this.analystsError = '';

    this.userService.getUsers().subscribe({

      next: (users) => {

        this.analysts =
          users.filter(
            user =>
              user.role === 'SECURITY_ANALYST'
              && user.status !== 'DISABLED'
          );

        this.isLoadingAnalysts = false;

        this.cdr.markForCheck();
      },

      error: (error) => {

        console.error(
          'Users API failed:',
          error.status,
          error.error
        );

        this.isLoadingAnalysts = false;

        switch (error?.status) {

          case 401:
            this.analystsError =
              'Authentication failed. Your session may have expired.';
            break;

          case 403:
            this.analystsError =
              'You do not have permission to load security analysts.';
            break;

          case 0:
            this.analystsError =
              'Cannot connect to the backend.';
            break;

          default:
            this.analystsError =
              'Unable to load security analysts.';
        }

        this.cdr.markForCheck();
      }
    });
  }

  selectIncident(incident: Incident): void {

    this.selectedIncident = incident;

    this.selectedStatus =
      this.getNextStatus(
        incident.status
      );

    this.selectedAnalystEmail =
      incident.assignedTo?.email ?? '';

    this.newNoteContent = '';

    this.notesError = '';

    this.clearActionMessages();

    this.loadInvestigationNotes(
      incident.id
    );

    this.cdr.markForCheck();
  }

  loadInvestigationNotes(
    incidentId: number
  ): void {

    this.isLoadingNotes = true;

    this.notesError = '';

    this.investigationNotes = [];

    this.incidentService
      .getInvestigationNotes(incidentId)
      .subscribe({

        next: (notes) => {

          this.investigationNotes =
            [...notes];

          this.isLoadingNotes = false;

          this.cdr.markForCheck();
        },

        error: (error) => {

          console.error(
            'Investigation notes API failed:',
            error.status,
            error.error
          );

          this.isLoadingNotes = false;

          switch (error?.status) {

            case 401:
              this.notesError =
                'Authentication failed. Your session may have expired.';
              break;

            case 403:
              this.notesError =
                'Access denied. You do not have permission to view investigation notes.';
              break;

            case 404:
              this.notesError =
                'The selected incident was not found.';
              break;

            case 0:
              this.notesError =
                'Cannot connect to the backend.';
              break;

            default:
              this.notesError =
                'Unable to load investigation notes.';
          }

          this.cdr.markForCheck();
        }
      });
  }

  createInvestigationNote(): void {

    if (!this.selectedIncident) {
      return;
    }

    const content =
      this.newNoteContent.trim();

    if (!content) {

      this.notesError =
        'Investigation note cannot be empty.';

      return;
    }

    if (content.length > 5000) {

      this.notesError =
        'Investigation note must not exceed 5000 characters.';

      return;
    }

    this.isCreatingNote = true;

    this.notesError = '';

    this.incidentService
      .createInvestigationNote(
        this.selectedIncident.id,
        content
      )
      .subscribe({

        next: (note) => {

          this.investigationNotes = [
            ...this.investigationNotes,
            note
          ];

          this.newNoteContent = '';

          this.isCreatingNote = false;

          this.actionSuccess =
            'Investigation note added successfully.';

          this.actionError = '';

          this.cdr.markForCheck();
        },

        error: (error) => {

          console.error(
            'Create investigation note failed:',
            error.status,
            error.error
          );

          this.isCreatingNote = false;

          switch (error?.status) {

            case 400:
              this.notesError =
                error?.error?.message ||
                'The investigation note is invalid.';
              break;

            case 401:
              this.notesError =
                'Authentication failed. Your session may have expired.';
              break;

            case 403:
              this.notesError =
                'Access denied. You do not have permission to create investigation notes.';
              break;

            case 404:
              this.notesError =
                'The selected incident was not found.';
              break;

            case 0:
              this.notesError =
                'Cannot connect to the backend.';
              break;

            default:
              this.notesError =
                error?.error?.message ||
                'Unable to create investigation note.';
          }

          this.cdr.markForCheck();
        }
      });
  }

  clearSelectedIncident(): void {

    this.selectedIncident = null;

    this.selectedStatus = '';

    this.selectedAnalystEmail = '';

    this.investigationNotes = [];

    this.newNoteContent = '';

    this.notesError = '';

    this.clearActionMessages();

    this.cdr.markForCheck();
  }

  updateIncidentStatus(): void {

    if (!this.selectedIncident) {
      return;
    }

    if (!this.selectedStatus) {

      this.actionError =
        'No valid next status is available.';

      return;
    }

    this.isUpdatingStatus = true;

    this.clearActionMessages();

    this.incidentService.updateStatus(
      this.selectedIncident.id,
      this.selectedStatus
    ).subscribe({

      next: (updatedIncident) => {

        this.isUpdatingStatus = false;

        this.actionSuccess =
          `Incident #${updatedIncident.id} status changed to ${updatedIncident.status}.`;

        this.selectedIncident =
          updatedIncident;

        this.selectedStatus =
          this.getNextStatus(
            updatedIncident.status
          );

        this.updateIncidentInList(
          updatedIncident
        );

        this.applyFilters();

        this.cdr.markForCheck();
      },

      error: (error) => {

        console.error(
          'Status update failed:',
          error.status,
          error.error
        );

        this.isUpdatingStatus = false;

        this.actionError =
          this.getActionErrorMessage(
            error,
            'Unable to update incident status.'
          );

        this.cdr.markForCheck();
      }
    });
  }

  assignIncident(): void {

    if (!this.selectedIncident) {
      return;
    }

    if (!this.selectedAnalystEmail) {

      this.actionError =
        'Please select a security analyst.';

      return;
    }

    this.isAssigning = true;

    this.clearActionMessages();

    this.incidentService.assignIncident(
      this.selectedIncident.id,
      this.selectedAnalystEmail
    ).subscribe({

      next: (updatedIncident) => {

        this.isAssigning = false;

        this.actionSuccess =
          `Incident #${updatedIncident.id} assigned successfully.`;

        this.selectedIncident =
          updatedIncident;

        this.selectedAnalystEmail =
          updatedIncident.assignedTo?.email ?? '';

        this.selectedStatus =
          this.getNextStatus(
            updatedIncident.status
          );

        this.updateIncidentInList(
          updatedIncident
        );

        this.applyFilters();

        this.cdr.markForCheck();
      },

      error: (error) => {

        console.error(
          'Incident assignment failed:',
          error.status,
          error.error
        );

        this.isAssigning = false;

        this.actionError =
          this.getActionErrorMessage(
            error,
            'Unable to assign incident.'
          );

        this.cdr.markForCheck();
      }
    });
  }

  applyFilters(): void {

    const search =
      this.searchTerm
        .trim()
        .toLowerCase();

    this.filteredIncidents =
      this.incidents.filter(
        incident => {

          const matchesSearch =
            !search ||
            incident.title?.toLowerCase().includes(search) ||
            incident.description?.toLowerCase().includes(search) ||
            incident.category?.toLowerCase().includes(search) ||
            incident.reportedBy?.email?.toLowerCase().includes(search) ||
            incident.assignedTo?.email?.toLowerCase().includes(search) ||
            String(incident.id).includes(search);

          const matchesSeverity =
            !this.severityFilter ||
            incident.severity?.toUpperCase() ===
              this.severityFilter;

          const matchesStatus =
            !this.statusFilter ||
            incident.status?.toUpperCase() ===
              this.statusFilter;

          return (
            matchesSearch &&
            matchesSeverity &&
            matchesStatus
          );
        }
      );

    this.cdr.markForCheck();
  }

  clearFilters(): void {

    this.searchTerm = '';

    this.severityFilter = '';

    this.statusFilter = '';

    this.filteredIncidents =
      [...this.incidents];

    this.cdr.markForCheck();
  }

  getNextStatus(
    currentStatus: string
  ): string {

    switch (
      currentStatus?.toUpperCase()
    ) {

      case 'OPEN':
        return 'ASSIGNED';

      case 'ASSIGNED':
        return 'INVESTIGATING';

      case 'INVESTIGATING':
        return 'CONTAINED';

      case 'CONTAINED':
        return 'RESOLVED';

      case 'RESOLVED':
        return 'CLOSED';

      case 'CLOSED':
        return '';

      default:
        return '';
    }
  }

  getStatusActionLabel(
    currentStatus: string
  ): string {

    const nextStatus =
      this.getNextStatus(
        currentStatus
      );

    if (!nextStatus) {
      return 'No further transition';
    }

    return `Move to ${nextStatus}`;
  }

  getSeverityClass(
    severity: string
  ): string {

    switch (
      severity?.toUpperCase()
    ) {

      case 'CRITICAL':
        return 'severity-critical';

      case 'HIGH':
        return 'severity-high';

      case 'MEDIUM':
        return 'severity-medium';

      case 'LOW':
        return 'severity-low';

      default:
        return 'severity-default';
    }
  }

  getStatusClass(
    status: string
  ): string {

    switch (
      status?.toUpperCase()
    ) {

      case 'OPEN':
        return 'status-open';

      case 'ASSIGNED':
        return 'status-assigned';

      case 'INVESTIGATING':
        return 'status-investigating';

      case 'CONTAINED':
        return 'status-contained';

      case 'RESOLVED':
        return 'status-resolved';

      case 'CLOSED':
        return 'status-closed';

      default:
        return 'status-default';
    }
  }

  private updateIncidentInList(
    updatedIncident: Incident
  ): void {

    this.incidents =
      this.incidents.map(
        incident =>
          incident.id === updatedIncident.id
            ? updatedIncident
            : incident
      );
  }

  private clearActionMessages(): void {

    this.actionSuccess = '';

    this.actionError = '';
  }

  private getActionErrorMessage(
    error: any,
    fallback: string
  ): string {

    switch (error?.status) {

      case 400:
        return (
          error?.error?.message ||
          'The requested incident action is invalid.'
        );

      case 401:
        return 'Authentication failed. Your session may have expired.';

      case 403:
        return 'Access denied. You do not have permission to perform this action.';

      case 404:
        return 'The incident or selected analyst was not found.';

      case 0:
        return 'Cannot connect to the backend.';

      default:
        return (
          error?.error?.message ||
          fallback
        );
    }
  }
}
