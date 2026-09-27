import { Component, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';

import { AuthService } from '../../services/auth.service';

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

  activeIncidents = 12;
  criticalAlerts = 2;
  totalUsers = 6;
  auditEvents = 156;

  threatScore = 28;
  systemStatus = 'OPERATIONAL';

  chartValues = [35, 52, 42, 68, 55, 76, 61, 82, 70, 88, 73, 91];

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

  private dashboardSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.dashboardSubscription = interval(3000).subscribe(() => {
      this.updateDashboard();
    });
  }

  ngOnDestroy(): void {
    this.dashboardSubscription?.unsubscribe();
  }

  private updateDashboard(): void {

    this.currentTime = new Date();

    this.activeIncidents = Math.max(
      1,
      this.activeIncidents + this.randomChange(-1, 1)
    );

    this.criticalAlerts = Math.max(
      0,
      this.criticalAlerts + this.randomChange(-1, 1)
    );

    this.auditEvents += this.randomChange(1, 3);

    this.threatScore = Math.max(
      10,
      Math.min(
        90,
        this.threatScore + this.randomChange(-4, 4)
      )
    );

    this.chartValues = this.chartValues.map(() =>
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
      events[Math.floor(Math.random() * events.length)];

    this.securityEvents.unshift(randomEvent);

    if (this.securityEvents.length > 6) {
      this.securityEvents.pop();
    }
  }

  private randomNumber(min: number, max: number): number {
    return Math.floor(
      Math.random() * (max - min + 1)
    ) + min;
  }

  private randomChange(min: number, max: number): number {
    return this.randomNumber(min, max);
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

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
