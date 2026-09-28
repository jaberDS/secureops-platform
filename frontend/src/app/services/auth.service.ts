import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { LoginRequest, LoginResponse } from '../models/auth';
import { Role } from '../models/role';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = 'http://localhost:9000/api/auth';
  private readonly tokenKey = 'secureops_token';

  constructor(
    private http: HttpClient
  ) {}

  login(
    request: LoginRequest
  ): Observable<LoginResponse> {

    return this.http
      .post<LoginResponse>(
        `${this.apiUrl}/login`,
        request
      )
      .pipe(

        tap((response) => {

          console.log(
            'AuthService: login response received'
          );

          if (
            response &&
            response.token
          ) {

            localStorage.setItem(
              this.tokenKey,
              response.token
            );

            console.log(
              'AuthService: JWT stored successfully'
            );

            console.log(
              'AuthService: User role:',
              this.getRole()
            );

          } else {

            console.error(
              'AuthService: No JWT token in response'
            );
          }
        })
      );
  }

  logout(): void {

    localStorage.removeItem(
      this.tokenKey
    );

    console.log(
      'AuthService: JWT removed'
    );
  }

  getToken(): string | null {

    return localStorage.getItem(
      this.tokenKey
    );
  }

  isLoggedIn(): boolean {

    return this.getToken() !== null;
  }

  getRole(): Role | null {

    const token = this.getToken();

    if (!token) {
      return null;
    }

    try {

      const payload =
        this.decodeTokenPayload(token);

      const role =
        payload['role'] ??
        payload['roles'];

      /*
       * JWT contains:
       *
       * "role": "ADMIN"
       */
      if (typeof role === 'string') {

        return this.normalizeRole(role);
      }

      /*
       * JWT contains:
       *
       * "roles": ["ADMIN"]
       */
      if (
        Array.isArray(role) &&
        role.length > 0
      ) {

        return this.normalizeRole(
          String(role[0])
        );
      }

      return null;

    } catch (error) {

      console.error(
        'AuthService: Unable to read JWT role'
      );

      return null;
    }
  }

  isAdmin(): boolean {

    return this.getRole() === Role.ADMIN;
  }

  isManager(): boolean {

    return this.getRole() === Role.MANAGER;
  }

  isSecurityAnalyst(): boolean {

    return (
      this.getRole() ===
      Role.SECURITY_ANALYST
    );
  }

  isEmployee(): boolean {

    return this.getRole() === Role.EMPLOYEE;
  }

  private decodeTokenPayload(
    token: string
  ): Record<string, unknown> {

    const parts =
      token.split('.');

    if (parts.length !== 3) {

      throw new Error(
        'Invalid JWT format'
      );
    }

    const payload =
      parts[1];

    const base64 =
      payload
        .replace(/-/g, '+')
        .replace(/_/g, '/');

    const decodedPayload =
      decodeURIComponent(
        atob(base64)
          .split('')
          .map(
            (character) =>
              `%${(
                '00' +
                character
                  .charCodeAt(0)
                  .toString(16)
              ).slice(-2)}`
          )
          .join('')
      );

    return JSON.parse(
      decodedPayload
    );
  }

  private normalizeRole(
    role: string
  ): Role | null {

    const normalizedRole =
      role
        .replace('ROLE_', '')
        .toUpperCase();

    switch (normalizedRole) {

      case Role.ADMIN:
        return Role.ADMIN;

      case Role.MANAGER:
        return Role.MANAGER;

      case Role.SECURITY_ANALYST:
        return Role.SECURITY_ANALYST;

      case Role.EMPLOYEE:
        return Role.EMPLOYEE;

      default:
        return null;
    }
  }
}