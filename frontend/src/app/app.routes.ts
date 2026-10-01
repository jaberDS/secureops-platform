import { Routes } from '@angular/router';

import { Login } from './pages/login/login';
import { Dashboard } from './pages/dashboard/dashboard';
import { AuditLogs } from './pages/audit-logs/audit-logs';
import { Incidents } from './pages/incidents/incidents';
import { SecurityEvents } from './pages/security-events/security-events';
import { Users } from './pages/users/users';
import { Settings } from './pages/settings/settings';

import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

import { Role } from './models/role';

export const routes: Routes = [

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    component: Login
  },

  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard]
  },

  {
    path: 'audit-logs',
    component: AuditLogs,
    canActivate: [authGuard],
    data: {
      roles: [
        Role.ADMIN,
        Role.SECURITY_ANALYST
      ]
    }
  },

  {
    path: 'incidents',
    component: Incidents,
    canActivate: [authGuard]
  },

  {
    path: 'security-events',
    component: SecurityEvents,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: [
        Role.SECURITY_ANALYST
      ]
    }
  },

  {
    path: 'users',
    component: Users,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: [
        Role.ADMIN
      ]
    }
  },

  {
    path: 'settings',
    component: Settings,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: [
        Role.ADMIN,
        Role.MANAGER
      ]
    }
  }

];
