import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  credentials: LoginRequest = {
    email: '',
    password: ''
  };

  errorMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login(): void {
    this.errorMessage = '';

    if (!this.credentials.email.trim() || !this.credentials.password) {
      this.errorMessage = 'Please enter your email and password.';
      return;
    }

    this.isLoading = true;

    const request: LoginRequest = {
      email: this.credentials.email.trim().toLowerCase(),
      password: this.credentials.password
    };

    console.log('Sending login request...');

    this.authService.login(request).subscribe({
      next: (response) => {
        console.log('Login successful');
        console.log('JWT received successfully');

        this.isLoading = false;

        console.log('Token stored:', this.authService.getToken() !== null);

        this.router.navigate(['/dashboard']).then((success) => {
          console.log('Dashboard navigation:', success);

          if (!success) {
            this.errorMessage = 'Login succeeded, but dashboard navigation failed.';
          }
        });
      },

      error: (error) => {
        console.error('Login failed');
        console.error('Status:', error.status);
        console.error('Response:', error.error);

        this.isLoading = false;

        if (error.status === 400) {
          this.errorMessage = 'Invalid email format.';
        } else if (error.status === 401) {
          this.errorMessage = 'Invalid email or password.';
        } else if (error.status === 0) {
          this.errorMessage = 'Cannot connect to the backend.';
        } else {
          this.errorMessage = 'Login failed. Please try again.';
        }
      }
    });
  }
}