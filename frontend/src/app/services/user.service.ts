import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id: number;
  email: string;
  role: string;
  status: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  role: string;
}

export interface UpdateUserRequest {
  email?: string;
  password?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl =
    'http://localhost:9000/api/admin/users';

  constructor(
    private http: HttpClient
  ) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(
      this.apiUrl
    );
  }

  getUser(id: number): Observable<User> {
    return this.http.get<User>(
      `${this.apiUrl}/${id}`
    );
  }

  createUser(
    request: CreateUserRequest
  ): Observable<User> {

    return this.http.post<User>(
      this.apiUrl,
      request
    );
  }

  updateUser(
    id: number,
    request: UpdateUserRequest
  ): Observable<User> {

    return this.http.patch<User>(
      `${this.apiUrl}/${id}`,
      request
    );
  }

  updateRole(
    id: number,
    role: string
  ): Observable<User> {

    return this.http.patch<User>(
      `${this.apiUrl}/${id}/role`,
      { role }
    );
  }

  updateStatus(
    id: number,
    status: string
  ): Observable<User> {

    return this.http.patch<User>(
      `${this.apiUrl}/${id}/status`,
      { status }
    );
  }
}
