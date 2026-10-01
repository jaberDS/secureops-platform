import { Routes } from '@angular/router';

import { Login } from './pages/login/login';

import { Dashboard } from './pages/dashboard/dashboard';

import { AuditLogs } from './pages/audit-logs/audit-logs';

import { Incidents } from './pages/incidents/incidents';

import { SecurityEvents } from './pages/security-events/security-events';

import { authGuard } from './guards/auth.guard';

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
    canActivate: [authGuard]
  },

  {
    path: 'incidents',
    component: Incidents,
    canActivate: [authGuard]
  },

  {
    path: 'security-events',
    component: SecurityEvents,
    canActivate: [authGuard]
  }

];
