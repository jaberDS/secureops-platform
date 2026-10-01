import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  User,
  UserService,
  UpdateUserRequest
} from '../../services/user.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './users.html',
  styleUrl: './users.css'
})
export class Users implements OnInit {

  users: User[] = [];

  isLoading = false;
  isSaving = false;

  errorMessage = '';
  successMessage = '';

  showCreateForm = false;

  editingUserId: number | null = null;

  editForm = {
    email: '',
    password: ''
  };

  newUser = {
    email: '',
    password: '',
    role: 'EMPLOYEE'
  };

  roles = [
    'ADMIN',
    'SECURITY_ANALYST',
    'MANAGER',
    'EMPLOYEE'
  ];

  constructor(
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
        this.cdr.markForCheck();
      },

      error: (error) => {
        console.error('Users API failed:', error);

        this.isLoading = false;

        this.errorMessage =
          this.getErrorMessage(
            error,
            'Unable to load users.'
          );

        this.cdr.markForCheck();
      }
    });
  }

  createUser(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (
      !this.newUser.email.trim() ||
      !this.newUser.password.trim()
    ) {
      this.errorMessage =
        'Email and password are required.';
      return;
    }

    this.isSaving = true;

    this.userService
      .createUser({
        email: this.newUser.email.trim(),
        password: this.newUser.password,
        role: this.newUser.role
      })
      .subscribe({
        next: (user) => {
          this.users = [
            ...this.users,
            user
          ];

          this.newUser = {
            email: '',
            password: '',
            role: 'EMPLOYEE'
          };

          this.showCreateForm = false;
          this.isSaving = false;

          this.successMessage =
            `User ${user.email} created successfully.`;

          this.cdr.markForCheck();
        },

        error: (error) => {
          console.error(
            'Create user failed:',
            error
          );

          this.isSaving = false;

          this.errorMessage =
            this.getErrorMessage(
              error,
              'Unable to create user.'
            );

          this.cdr.markForCheck();
        }
      });
  }

  changeRole(
    user: User,
    role: string
  ): void {

    if (role === user.role) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.userService
      .updateRole(user.id, role)
      .subscribe({
        next: (updatedUser) => {

          this.replaceUser(updatedUser);

          this.successMessage =
            `Role updated for ${updatedUser.email}.`;

          this.cdr.markForCheck();
        },

        error: (error) => {

          console.error(
            'Role update failed:',
            error
          );

          this.errorMessage =
            this.getErrorMessage(
              error,
              'Unable to update user role.'
            );

          this.loadUsers();
        }
      });
  }

  toggleStatus(user: User): void {

    const newStatus =
      user.status === 'ACTIVE'
        ? 'DISABLED'
        : 'ACTIVE';

    this.errorMessage = '';
    this.successMessage = '';

    this.userService
      .updateStatus(
        user.id,
        newStatus
      )
      .subscribe({
        next: (updatedUser) => {

          this.replaceUser(updatedUser);

          this.successMessage =
            `${updatedUser.email} is now ${updatedUser.status}.`;

          this.cdr.markForCheck();
        },

        error: (error) => {

          console.error(
            'Status update failed:',
            error
          );

          this.errorMessage =
            this.getErrorMessage(
              error,
              'Unable to update account status.'
            );

          this.loadUsers();
        }
      });
  }

  startEdit(user: User): void {

    this.errorMessage = '';
    this.successMessage = '';

    this.editingUserId = user.id;

    this.editForm = {
      email: user.email,
      password: ''
    };
  }

  cancelEdit(): void {

    this.editingUserId = null;

    this.editForm = {
      email: '',
      password: ''
    };
  }

  saveEdit(user: User): void {

    this.errorMessage = '';
    this.successMessage = '';

    const email =
      this.editForm.email.trim();

    const password =
      this.editForm.password;

    if (!email) {

      this.errorMessage =
        'Email is required.';

      return;
    }

    if (
      !password.trim() &&
      email === user.email
    ) {

      this.errorMessage =
        'Make at least one change.';

      return;
    }

    const request: UpdateUserRequest = {
      email
    };

    if (password.trim()) {

      request.password =
        password;
    }

    this.isSaving = true;

    this.userService
      .updateUser(
        user.id,
        request
      )
      .subscribe({

        next: (updatedUser) => {

          this.replaceUser(
            updatedUser
          );

          this.editingUserId = null;

          this.editForm = {
            email: '',
            password: ''
          };

          this.isSaving = false;

          this.successMessage =
            `User ${updatedUser.email} updated successfully.`;

          this.cdr.markForCheck();
        },

        error: (error) => {

          console.error(
            'User update failed:',
            error
          );

          this.isSaving = false;

          this.errorMessage =
            this.getErrorMessage(
              error,
              'Unable to update user.'
            );

          this.cdr.markForCheck();
        }
      });
  }

  goToDashboard(): void {
    this.router.navigate([
      '/dashboard'
    ]);
  }

  goToSettings(): void {
    this.router.navigate([
      '/settings'
    ]);
  }

  logout(): void {

    localStorage.removeItem(
      'secureops_token'
    );

    this.router.navigate([
      '/login'
    ]);
  }

  getActiveCount(): number {

    return this.users.filter(
      user =>
        user.status === 'ACTIVE'
    ).length;
  }

  getDisabledCount(): number {

    return this.users.filter(
      user =>
        user.status === 'DISABLED'
    ).length;
  }

  private replaceUser(
    updatedUser: User
  ): void {

    this.users =
      this.users.map(
        user =>
          user.id === updatedUser.id
            ? updatedUser
            : user
      );
  }

  private getErrorMessage(
    error: any,
    fallback: string
  ): string {

    if (error?.status === 400) {

      if (
        typeof error?.error === 'string'
      ) {
        return error.error;
      }

      if (
        error?.error?.errors &&
        typeof error.error.errors === 'object'
      ) {

        const messages =
          Object.values(
            error.error.errors
          )
          .filter(
            message =>
              typeof message === 'string'
          ) as string[];

        if (messages.length > 0) {
          return messages.join(' ');
        }
      }

      if (error?.error?.message) {
        return error.error.message;
      }

      return (
        'Invalid request. Check the submitted information.'
      );
    }

    if (error?.status === 401) {

      return (
        'Your session has expired. Please login again.'
      );
    }

    if (error?.status === 403) {

      return (
        'Access denied. ADMIN permission is required.'
      );
    }

    if (error?.status === 409) {

      if (error?.error?.message) {
        return error.error.message;
      }

      return (
        'The requested operation conflicts with existing data.'
      );
    }

    if (error?.status === 0) {

      return (
        'Cannot connect to the backend.'
      );
    }

    return fallback;
  }
}