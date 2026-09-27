import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { LoginRequest, LoginResponse } from '../models/auth';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = 'http://localhost:8081/api/auth';
  private readonly tokenKey = 'secureops_token';

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap((response) => {
          console.log('AuthService: login response received');

          if (response && response.token) {
            localStorage.setItem(this.tokenKey, response.token);
            console.log('AuthService: JWT stored successfully');
          } else {
            console.error('AuthService: No JWT token in response');
          }
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    console.log('AuthService: JWT removed');
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }
}