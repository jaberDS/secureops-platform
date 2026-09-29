import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';

import {
  AuditLog,
  AuditLogService
} from '../../services/audit-log.service';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.css'
})
export class AuditLogs implements OnInit {

  auditLogs: AuditLog[] = [];

  isLoading = false;
  loadError = '';

  constructor(
    private auditLogService: AuditLogService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    this.isLoading = true;
    this.loadError = '';

    this.auditLogService.getLogs().subscribe({
      next: (logs) => {
        this.auditLogs = [...logs].sort(
          (a, b) =>
            new Date(b.timestamp).getTime() -
            new Date(a.timestamp).getTime()
        );

        this.isLoading = false;
        this.cdr.markForCheck();
      },

      error: (error) => {
        console.error(
          'Audit Logs API failed:',
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
              'Access denied. ADMIN or SECURITY_ANALYST permission is required.';
            break;

          case 0:
            this.loadError =
              'Cannot connect to the backend.';
            break;

          default:
            this.loadError =
              'Unable to load audit logs.';
        }

        this.cdr.markForCheck();
      }
    });
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.router.navigate(['/login']);
  }
}