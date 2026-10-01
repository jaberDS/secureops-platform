import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import { DatePipe } from '@angular/common';

import { Router } from '@angular/router';

import { EMPTY, interval, Subscription, timer } from 'rxjs';

import { catchError, switchMap, tap } from 'rxjs/operators';

import { AuthService } from '../../services/auth.service';
import { UserService, User } from '../../services/user.service';
import { IncidentService, Incident } from '../../services/incident.service';
import { AuditLogService, AuditLog } from '../../services/audit-log.service';
import { Role } from '../../models/role';

type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

interface SecurityEvent {
  time: string;
  type: string;
  source: string;
  severity: Severity;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit, OnDestroy {

  currentTime = new Date();

  activeIncidents = 0;
  criticalAlerts = 0;
  totalUsers = 0;
  auditEvents = 0;

  threatScore = 0;
  systemStatus = 'OPERATIONAL';

  chartValues = [
    10, 10, 10, 10, 10, 10,
    10, 10, 10, 10, 10, 10
  ];

  users: User[] = [];
  incidents: Incident[] = [];

  auditLogs: AuditLog[] = [];
  recentAuditLogs: AuditLog[] = [];
  securityEvents: SecurityEvent[] = [];

  isLoadingUsers = false;
  userLoadError = '';

  isLoadingIncidents = false;
  incidentLoadError = '';

  isLoadingAuditLogs = false;
  auditLogLoadError = '';

  private dashboardSubscription?: Subscription;
  private auditRefreshSubscription?: Subscription;

  currentUserRole: Role | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private incidentService: IncidentService,
    private auditLogService: AuditLogService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUserRole = this.authService.getRole();

    if (this.isAdmin()) {
      this.loadUsers();
    }

    if (
      this.isAdmin() ||
      this.isManager() ||
      this.isSecurityAnalyst()
    ) {
      this.loadIncidents();
    }

    if (
      this.isAdmin() ||
      this.isSecurityAnalyst()
    ) {
      this.startAuditLogPolling();
    }

    this.dashboardSubscription = interval(3000).subscribe(() => {
      this.currentTime = new Date();
      this.refreshSecurityEvents();
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy(): void {
    this.dashboardSubscription?.unsubscribe();
    this.auditRefreshSubscription?.unsubscribe();
  }

  private normalize(role: unknown): string {
    return String(role ?? '')
      .toUpperCase()
      .replace(/^ROLE_/, '');
  }

  private is(role: Role): boolean {
    return (
      this.normalize(this.currentUserRole) ===
      this.normalize(role)
    );
  }

  isAdmin(): boolean {
    return this.is(Role.ADMIN);
  }

  isManager(): boolean {
    return this.is(Role.MANAGER);
  }

  isSecurityAnalyst(): boolean {
    return this.is(Role.SECURITY_ANALYST);
  }

  isEmployee(): boolean {
    return this.is(Role.EMPLOYEE);
  }

  getRoleLabel(): string {
    if (this.isAdmin()) return 'Administrator';
    if (this.isManager()) return 'Manager';
    if (this.isSecurityAnalyst()) return 'Security Analyst';
    if (this.isEmployee()) return 'Employee';

    return 'Unknown Role';
  }

  getRoleInitial(): string {
    if (this.isAdmin()) return 'A';
    if (this.isManager()) return 'M';
    if (this.isSecurityAnalyst()) return 'S';

    return 'E';
  }

  private describeError(
    error: any,
    forbiddenMessage: string,
    fallback: string
  ): string {
    switch (error?.status) {
      case 401:
        return 'Authentication failed. Your session may have expired.';

      case 403:
        return forbiddenMessage;

      case 0:
        return 'Cannot connect to the backend.';

      default:
        return fallback;
    }
  }

  private loadUsers(): void {
    this.isLoadingUsers = true;
    this.userLoadError = '';

    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.totalUsers = users.length;
        this.isLoadingUsers = false;

        this.cdr.markForCheck();
      },

      error: (error) => {
        console.error(
          'User API failed:',
          error.status,
          error.error
        );

        this.isLoadingUsers = false;

        this.userLoadError = this.describeError(
          error,
          'Access denied. ADMIN permission is required.',
          'Unable to load users.'
        );

        this.cdr.markForCheck();
      }
    });
  }

  private loadIncidents(): void {
    this.isLoadingIncidents = true;
    this.incidentLoadError = '';

    this.incidentService.getIncidents().subscribe({
      next: (incidents) => {
        this.incidents = incidents;

        this.calculateIncidentMetrics();
        this.calculateThreatScore();

        this.isLoadingIncidents = false;

        this.cdr.markForCheck();
      },

      error: (error) => {
        console.error(
          'Incident API failed:',
          error.status,
          error.error
        );

        this.isLoadingIncidents = false;

        this.incidentLoadError = this.describeError(
          error,
          'Access denied. Your role cannot view all incidents.',
          'Unable to load incidents.'
        );

        this.cdr.markForCheck();
      }
    });
  }

  private calculateIncidentMetrics(): void {
    this.activeIncidents =
      this.incidents.filter(
        (incident) =>
          !this.isClosedOrResolved(incident)
      ).length;

    this.criticalAlerts =
      this.incidents.filter(
        (incident) =>
          this.normalizeSeverity(incident.severity) === 'CRITICAL' &&
          !this.isClosedOrResolved(incident)
      ).length;
  }

  private isClosedOrResolved(
    incident: Incident
  ): boolean {
    const status =
      String(incident.status ?? '').toUpperCase();

    return (
      status === 'CLOSED' ||
      status === 'RESOLVED'
    );
  }

  private normalizeSeverity(
    severity: string | undefined
  ): string {
    return String(severity ?? '').toUpperCase();
  }

  private startAuditLogPolling(): void {
    this.auditRefreshSubscription =
      timer(0, 5000)
        .pipe(
          tap(() => {
            if (this.auditLogs.length === 0) {
              this.isLoadingAuditLogs = true;
              this.cdr.markForCheck();
            }
          }),

          switchMap(() =>
            this.auditLogService.getLogs().pipe(
              catchError((error) => {
                console.error(
                  'Audit API failed:',
                  error.status,
                  error.error
                );

                this.isLoadingAuditLogs = false;

                this.auditLogLoadError =
                  this.describeError(
                    error,
                    'Access denied. ADMIN or SECURITY_ANALYST permission is required.',
                    'Unable to load audit logs.'
                  );

                this.cdr.markForCheck();

                return EMPTY;
              })
            )
          )
        )
        .subscribe((logs) =>
          this.applyAuditLogs(logs)
        );
  }

  private applyAuditLogs(
    logs: AuditLog[]
  ): void {
    const sorted = [...logs].sort(
      (a, b) =>
        new Date(b.timestamp).getTime() -
        new Date(a.timestamp).getTime()
    );

    this.auditLogs = sorted;
    this.auditEvents = sorted.length;
    this.recentAuditLogs = sorted.slice(0, 6);

    this.refreshSecurityEvents();
    this.calculateThreatScore();
    this.calculateActivityChart();

    this.isLoadingAuditLogs = false;
    this.auditLogLoadError = '';

    this.cdr.markForCheck();
  }

  private refreshSecurityEvents(): void {
    this.securityEvents =
      this.recentAuditLogs.map((log) => ({
        time: this.getRelativeTime(log.timestamp),
        type: this.formatAuditAction(log.action),
        source: log.actor || 'SYSTEM',
        severity: this.getAuditSeverity(log.action)
      }));
  }

  private formatAuditAction(
    action: string
  ): string {
    switch (action) {
      case 'CREATE':
        return 'INCIDENT_CREATED';

      case 'ASSIGN':
        return 'INCIDENT_ASSIGNED';

      case 'STATUS_CHANGE':
        return 'INCIDENT_STATUS_CHANGED';

      default:
        return action;
    }
  }

  private getAuditSeverity(
    action: string
  ): Severity {
    switch (action) {
      case 'CREATE':
        return 'HIGH';

      case 'LOGIN_FAILURE':
      case 'PASSWORD_RESET_REQUEST':
      case 'STATUS_CHANGE':
        return 'MEDIUM';

      default:
        return 'LOW';
    }
  }

  private getRelativeTime(
    timestamp: string
  ): string {
    const time = new Date(timestamp).getTime();

    if (Number.isNaN(time)) {
      return 'Unknown';
    }

    const seconds =
      Math.floor(
        Math.max(
          0,
          Date.now() - time
        ) / 1000
      );

    if (seconds < 60) {
      return 'Just now';
    }

    const minutes =
      Math.floor(seconds / 60);

    if (minutes < 60) {
      return `${minutes} min ago`;
    }

    const hours =
      Math.floor(minutes / 60);

    if (hours < 24) {
      return `${hours} hour${hours === 1 ? '' : 's'} ago`;
    }

    const days =
      Math.floor(hours / 24);

    return `${days} day${days === 1 ? '' : 's'} ago`;
  }

  private calculateThreatScore(): void {
    let score = 0;

    for (const incident of this.incidents) {
      if (this.isClosedOrResolved(incident)) {
        continue;
      }

      switch (
        this.normalizeSeverity(
          incident.severity
        )
      ) {
        case 'CRITICAL':
          score += 25;
          break;

        case 'HIGH':
          score += 15;
          break;

        case 'MEDIUM':
          score += 8;
          break;

        case 'LOW':
          score += 2;
          break;
      }
    }

    const recentWindow =
      Date.now() -
      60 * 60 * 1000;

    const recentLoginFailures =
      this.auditLogs.filter((log) => {
        const timestamp =
          new Date(log.timestamp).getTime();

        return (
          log.action === 'LOGIN_FAILURE' &&
          timestamp >= recentWindow
        );
      }).length;

    score +=
      recentLoginFailures * 2;

    this.threatScore =
      Math.min(100, score);
  }

  private calculateActivityChart(): void {
    const now = Date.now();
    const bucketSize =
      5 * 60 * 1000;

    const counts =
      Array(12).fill(0) as number[];

    for (const log of this.auditLogs) {
      const timestamp =
        new Date(log.timestamp).getTime();

      if (Number.isNaN(timestamp)) {
        continue;
      }

      const age =
        now - timestamp;

      if (
        age < 0 ||
        age >= bucketSize * 12
      ) {
        continue;
      }

      const bucket =
        Math.floor(
          age / bucketSize
        );

      const index =
        11 - bucket;

      if (
        index >= 0 &&
        index < 12
      ) {
        counts[index]++;
      }
    }

    const max =
      Math.max(...counts, 1);

    this.chartValues =
      counts.map((count) => {
        if (count === 0) {
          return 10;
        }

        return Math.max(
          10,
          Math.round(
            (count / max) * 100
          )
        );
      });
  }

  getThreatLabel(): string {
    if (this.threatScore >= 70) {
      return 'HIGH';
    }

    if (this.threatScore >= 45) {
      return 'ELEVATED';
    }

    return 'LOW';
  }

  getThreatClass(): string {
    if (this.threatScore >= 70) {
      return 'critical';
    }

    if (this.threatScore >= 45) {
      return 'warning';
    }

    return 'safe';
  }

  goToAuditLogs(): void {
    this.router.navigate(['/audit-logs']);
  }

  goToIncidents(): void {
    this.router.navigate(['/incidents']);
  }

  goToSecurityEvents(): void {
    this.router.navigate(['/security-events']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
