import { Component, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import { UserService, User } from '../../services/user.service';
import {
  IncidentService,
  Incident
} from '../../services/incident.service';
import { Role } from '../../models/role';

interface SecurityEvent {
  time: string;
  type: string;
  source: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
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
  criticalAlerts = 2;
  totalUsers = 0;
  auditEvents = 156;

  threatScore = 28;
  systemStatus = 'OPERATIONAL';

  currentUserRole: Role | null = null;

  chartValues = [
    35, 52, 42, 68,
    55, 76, 61, 82,
    70, 88, 73, 91
  ];

  securityEvents: SecurityEvent[] = [
    {
      time: 'Just now',
      type: 'LOGIN_SUCCESS',
      source: 'admin@example.com',
      severity: 'LOW'
    },
    {
      time: '2 min ago',
      type: 'PORT_SCAN_DETECTED',
      source: '192.168.50.30',
      severity: 'HIGH'
    },
    {
      time: '5 min ago',
      type: 'LOGIN_FAILURE',
      source: 'unknown',
      severity: 'MEDIUM'
    },
    {
      time: '8 min ago',
      type: 'INCIDENT_CREATED',
      source: 'SOC Analyst',
      severity: 'HIGH'
    }
  ];

  users: User[] = [];

  incidents: Incident[] = [];

  isLoadingUsers = false;
  userLoadError = '';

  isLoadingIncidents = false;
  incidentLoadError = '';

  private dashboardSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private incidentService: IncidentService,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.currentUserRole =
      this.authService.getRole();

    console.log(
      'Dashboard: Current user role:',
      this.currentUserRole
    );

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

    this.dashboardSubscription =
      interval(3000).subscribe(() => {
        this.updateDashboard();
      });
  }

  ngOnDestroy(): void {
    this.dashboardSubscription?.unsubscribe();
  }

  /*
   * ============================
   * RBAC HELPERS
   * ============================
   */

  isAdmin(): boolean {
    return this.currentUserRole === Role.ADMIN;
  }

  isManager(): boolean {
    return this.currentUserRole === Role.MANAGER;
  }

  isSecurityAnalyst(): boolean {
    return this.currentUserRole === Role.SECURITY_ANALYST;
  }

  isEmployee(): boolean {
    return this.currentUserRole === Role.EMPLOYEE;
  }

  getRoleLabel(): string {

    switch (this.currentUserRole) {

      case Role.ADMIN:
        return 'Administrator';

      case Role.MANAGER:
        return 'Manager';

      case Role.SECURITY_ANALYST:
        return 'Security Analyst';

      case Role.EMPLOYEE:
        return 'Employee';

      default:
        return 'Unknown Role';
    }
  }

  /*
   * ============================
   * ADMIN USER MANAGEMENT
   * ============================
   */

  private loadUsers(): void {

    this.isLoadingUsers = true;
    this.userLoadError = '';

    this.userService.getUsers().subscribe({

      next: (users) => {

        console.log(
          'Protected user API request successful'
        );

        console.log(
          'Users received from backend:',
          users
        );

        this.users = users;
        this.totalUsers = users.length;

        this.isLoadingUsers = false;
      },

      error: (error) => {

        console.error(
          'Protected user API request failed'
        );

        console.error(
          'Status:',
          error.status
        );

        console.error(
          'Response:',
          error.error
        );

        this.isLoadingUsers = false;

        if (error.status === 401) {

          this.userLoadError =
            'Authentication failed. Your session may have expired.';

        } else if (error.status === 403) {

          this.userLoadError =
            'Access denied. ADMIN permission is required.';

        } else if (error.status === 0) {

          this.userLoadError =
            'Cannot connect to the backend.';

        } else {

          this.userLoadError =
            'Unable to load users.';
        }
      }
    });
  }

  /*
   * ============================
   * INCIDENT MANAGEMENT
   * ============================
   */

  private loadIncidents(): void {

    this.isLoadingIncidents = true;
    this.incidentLoadError = '';

    this.incidentService.getIncidents().subscribe({

      next: (incidents) => {

        console.log(
          'Incident API request successful'
        );

        console.log(
          'Incidents received from backend:',
          incidents
        );

        this.incidents = incidents;

        this.updateActiveIncidentCount();

        this.isLoadingIncidents = false;
      },

      error: (error) => {

        console.error(
          'Incident API request failed'
        );

        console.error(
          'Status:',
          error.status
        );

        console.error(
          'Response:',
          error.error
        );

        this.isLoadingIncidents = false;

        if (error.status === 401) {

          this.incidentLoadError =
            'Authentication failed. Your session may have expired.';

        } else if (error.status === 403) {

          this.incidentLoadError =
            'Access denied. Your role cannot view all incidents.';

        } else if (error.status === 0) {

          this.incidentLoadError =
            'Cannot connect to the backend.';

        } else {

          this.incidentLoadError =
            'Unable to load incidents.';
        }
      }
    });
  }

  private updateActiveIncidentCount(): void {

    this.activeIncidents =
      this.incidents.filter(
        (incident) =>
          incident.status !== 'CLOSED' &&
          incident.status !== 'RESOLVED'
      ).length;

    console.log(
      'Dashboard: Active incidents:',
      this.activeIncidents
    );
  }

  /*
   * ============================
   * DYNAMIC DASHBOARD
   * ============================
   */

  private updateDashboard(): void {

    this.currentTime = new Date();

    /*
     * Active incidents now come from
     * the backend instead of random data.
     */

    this.criticalAlerts = Math.max(
      0,
      this.criticalAlerts +
      this.randomChange(-1, 1)
    );

    this.auditEvents +=
      this.randomChange(1, 3);

    this.threatScore = Math.max(
      10,
      Math.min(
        90,
        this.threatScore +
        this.randomChange(-4, 4)
      )
    );

    this.chartValues =
      this.chartValues.map(() =>
        this.randomNumber(25, 95)
      );

    this.addSecurityEvent();
  }

  private addSecurityEvent(): void {

    const events: SecurityEvent[] = [

      {
        time: 'Just now',
        type: 'LOGIN_SUCCESS',
        source: 'admin@example.com',
        severity: 'LOW'
      },

      {
        time: 'Just now',
        type: 'LOGIN_FAILURE',
        source: 'unknown',
        severity: 'MEDIUM'
      },

      {
        time: 'Just now',
        type: 'PORT_SCAN_DETECTED',
        source: '192.168.50.30',
        severity: 'HIGH'
      },

      {
        time: 'Just now',
        type: 'SUSPICIOUS_ACTIVITY',
        source: '192.168.50.20',
        severity: 'HIGH'
      },

      {
        time: 'Just now',
        type: 'INCIDENT_CREATED',
        source: 'SOC Analyst',
        severity: 'HIGH'
      }
    ];

    const randomEvent =
      events[
        Math.floor(
          Math.random() * events.length
        )
      ];

    this.securityEvents.unshift(
      randomEvent
    );

    if (this.securityEvents.length > 6) {
      this.securityEvents.pop();
    }
  }

  private randomNumber(
    min: number,
    max: number
  ): number {

    return Math.floor(
      Math.random() *
      (max - min + 1)
    ) + min;
  }

  private randomChange(
    min: number,
    max: number
  ): number {

    return this.randomNumber(min, max);
  }

  /*
   * ============================
   * THREAT SCORE
   * ============================
   */

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

  /*
   * ============================
   * LOGOUT
   * ============================
   */

  logout(): void {

    this.authService.logout();

    this.router.navigate(['/login']);
  }
}
