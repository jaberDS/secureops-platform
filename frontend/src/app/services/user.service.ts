import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id: number;
  email: string;
  role: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl = 'http://localhost:9000/api/admin/users';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }
}