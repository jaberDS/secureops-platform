import {
  Component,
  OnInit
} from '@angular/core';

import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/role';

interface SessionInfo {
  email: string;
  role: string;
  issuedAt: string;
  expiresAt: string;
  remainingMinutes: number;
}

@Component({
  selector: 'app-settings',
  standalone: true,
  templateUrl: './settings.html',
  styleUrl: './settings.css'
})
export class Settings implements OnInit {

  role: Role | null = null;

  session: SessionInfo = {
    email: 'Unknown',
    role: 'UNKNOWN',
    issuedAt: 'Unknown',
    expiresAt: 'Unknown',
    remainingMinutes: 0
  };

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role =
      this.authService.getRole();

    this.loadSession();
  }

  loadSession(): void {

    const token =
      this.authService.getToken();

    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    try {

      const payload =
        this.decodeToken(token);

      const email =
        String(
          payload['sub'] ??
          payload['email'] ??
          'Unknown'
        );

      const issuedAt =
        this.formatTimestamp(
          payload['iat']
        );

      const expiresAt =
        this.formatTimestamp(
          payload['exp']
        );

      const expiration =
        typeof payload['exp'] === 'number'
          ? payload['exp'] * 1000
          : Date.now();

      const remainingMinutes =
        Math.max(
          0,
          Math.floor(
            (expiration - Date.now()) /
            60000
          )
        );

      this.session = {
        email,
        role: this.role ?? 'UNKNOWN',
        issuedAt,
        expiresAt,
        remainingMinutes
      };

    } catch (error) {

      console.error(
        'Unable to inspect session:',
        error
      );
    }
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goToUsers(): void {
    this.router.navigate(['/users']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getRoleLabel(): string {

    switch (this.role) {

      case Role.ADMIN:
        return 'Administrator';

      case Role.MANAGER:
        return 'Manager';

      case Role.SECURITY_ANALYST:
        return 'Security Analyst';

      case Role.EMPLOYEE:
        return 'Employee';

      default:
        return 'Unknown';
    }
  }

  private decodeToken(
    token: string
  ): Record<string, unknown> {

    const parts =
      token.split('.');

    if (parts.length !== 3) {
      throw new Error('Invalid JWT');
    }

    const base64 =
      parts[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/');

    const decoded =
      decodeURIComponent(
        atob(base64)
          .split('')
          .map(
            character =>
              `%${(
                '00' +
                character
                  .charCodeAt(0)
                  .toString(16)
              ).slice(-2)}`
          )
          .join('')
      );

    return JSON.parse(decoded);
  }

  private formatTimestamp(
    value: unknown
  ): string {

    if (
      typeof value !== 'number'
    ) {
      return 'Unknown';
    }

    return new Date(
      value * 1000
    ).toLocaleString();
  }
}
