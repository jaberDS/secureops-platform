import {
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import { DatePipe } from '@angular/common';

import { Router } from '@angular/router';

import {
  SecurityEventService,
  SecurityAlert,
  ThreatSeverity
} from '../../services/security-event.service';

import { AuthService } from '../../services/auth.service';

import { Subscription, timer } from 'rxjs';

@Component({
  selector: 'app-security-events',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './security-events.html',
  styleUrl: './security-events.css'
})
export class SecurityEvents
  implements OnInit, OnDestroy {

  alerts: SecurityAlert[] = [];

  riskScore = 0;

  isLoading = false;

  errorMessage = '';

  private refreshSubscription?: Subscription;

  constructor(
    private securityEventService:
      SecurityEventService,

    private authService:
      AuthService,

    private router:
      Router
  ) {}

  ngOnInit(): void {

    if (!this.authService.isSecurityAnalyst()) {

      this.errorMessage =
        'Security Events are available to SECURITY_ANALYST users only.';

      return;
    }

    this.loadData();

    this.refreshSubscription =
      timer(10000, 10000)
        .subscribe(() => {
          this.loadData();
        });
  }

  ngOnDestroy(): void {

    this.refreshSubscription?.unsubscribe();
  }

  loadData(): void {

    this.isLoading = true;

    this.errorMessage = '';

    this.securityEventService
      .getAlerts()
      .subscribe({

        next: (alerts) => {

          this.alerts = [...alerts].sort(
            (a, b) =>
              new Date(b.timestamp).getTime() -
              new Date(a.timestamp).getTime()
          );

          this.loadRiskScore();
        },

        error: (error) => {

          console.error(
            'Security Events API failed:',
            error.status,
            error.error
          );

          this.isLoading = false;

          if (error.status === 401) {

            this.errorMessage =
              'Authentication failed. Please login again.';

          } else if (error.status === 403) {

            this.errorMessage =
              'Access denied. SECURITY_ANALYST permission is required.';

          } else if (error.status === 0) {

            this.errorMessage =
              'Cannot connect to the backend. Make sure Spring Boot is running on port 9000.';

          } else {

            this.errorMessage =
              'Unable to load security events.';
          }
        }
      });
  }

  private loadRiskScore(): void {

    this.securityEventService
      .getRiskScore()
      .subscribe({

        next: (score) => {

          this.riskScore = score;

          this.isLoading = false;
        },

        error: (error) => {

          console.error(
            'Risk API failed:',
            error.status,
            error.error
          );

          this.riskScore = 0;

          this.isLoading = false;
        }
      });
  }

  getThreatClass(): string {

    if (this.riskScore >= 70) {
      return 'critical';
    }

    if (this.riskScore >= 45) {
      return 'warning';
    }

    return 'safe';
  }

  getThreatLabel(): string {

    if (this.riskScore >= 70) {
      return 'HIGH';
    }

    if (this.riskScore >= 45) {
      return 'ELEVATED';
    }

    return 'LOW';
  }

  getSeverityClass(
    severity: ThreatSeverity
  ): string {

    return String(severity).toLowerCase();
  }

  getStatusClass(
    status: string
  ): string {

    return String(status).toLowerCase();
  }

  goToDashboard(): void {

    this.router.navigate([
      '/dashboard'
    ]);
  }

  logout(): void {

    this.authService.logout();

    this.router.navigate([
      '/login'
    ]);
  }

  getNewAlertsCount(): number {
    return this.alerts.filter(
      alert => alert.status === 'NEW'
    ).length;
  }

  getHighAlertsCount(): number {
    return this.alerts.filter(
      alert =>
        alert.severity === 'HIGH' ||
        alert.severity === 'CRITICAL'
    ).length;
  }
}


