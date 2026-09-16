# SecureOps Platform — Application Architecture

## 1. Overview

SecureOps Platform follows a three-tier architecture.

The application is divided into three main layers:

1. **Frontend** — user interface
2. **Backend** — business logic and security
3. **Database** — persistent data storage

This separation makes the application easier to develop, secure, test, and maintain.

---

## 2. High-Level Architecture

```text
                         USER
                           │
                           ▼
                  ┌─────────────────┐
                  │ Angular Frontend│
                  │    Web Client   │
                  └────────┬────────┘
                           │
                       HTTPS / REST
                           │
                           ▼
                  ┌─────────────────┐
                  │  Spring Boot    │
                  │      API        │
                  ├─────────────────┤
                  │ Authentication  │
                  │      &          │
                  │      RBAC       │
                  ├─────────────────┤
                  │ Business Logic  │
                  └────────┬────────┘
                           │
                           │ SQL
                           ▼
                  ┌─────────────────┐
                  │   PostgreSQL    │
                  │    Database     │
                  └─────────────────┘
```

---

## 3. Frontend — Angular

The frontend is responsible for the user interface.

Users interact with SecureOps Platform through an Angular web application.

### Main responsibilities

* Login interface
* Dashboard
* Employee management
* Security incident management
* Reports
* User management
* Role-based interface
* Communication with the backend API

The frontend communicates with the backend through a REST API using HTTP/HTTPS.

The frontend must not directly access the database.

---

## 4. Backend — Spring Boot

The Spring Boot application provides the main backend API.

It is responsible for:

* Authentication
* Authorization
* Role-Based Access Control (RBAC)
* Business logic
* Input validation
* API security
* Database communication
* Audit logging
* Error handling

The backend acts as the main security boundary between the user and the database.

---

## 5. Authentication and Authorization

The application implements authentication and authorization.

### Authentication

Authentication answers:

> **"Who are you?"**

The user provides credentials and the application verifies their identity.

### Authorization

Authorization answers:

> **"What are you allowed to do?"**

The application uses **Role-Based Access Control (RBAC)**.

### Planned roles

```text
ADMIN
  │
  ├── System management
  ├── User management
  └── Security configuration

SECURITY_ANALYST
  │
  ├── View incidents
  ├── Investigate incidents
  └── Manage incident status

MANAGER
  │
  ├── View team information
  └── View reports

EMPLOYEE
  │
  ├── View own profile
  └── Report security incidents
```

The backend enforces authorization rules.

The frontend may hide unauthorized functionality, but the backend must always verify permissions.

This prevents users from bypassing security controls by directly calling the API.

---

## 6. Business Logic

The business logic contains the rules of the application.

Examples include:

* Creating an incident
* Assigning an incident
* Changing incident severity
* Investigating an incident
* Closing an incident
* Generating reports
* Managing users
* Recording security-related activities

Business rules should remain in the backend rather than being trusted to the frontend.

---

## 7. Database — PostgreSQL

PostgreSQL will be used to store application data.

Potential data includes:

* Users
* Roles
* Employees
* Security incidents
* Incident comments
* Reports
* Audit logs
* Application configuration

The database will only be accessed through the backend.

```text
Angular
   │
   X
   │
   │ No direct database access
   │
Spring Boot
   │
   ▼
PostgreSQL
```

This architecture reduces the database's exposure to users and external clients.

---

## 8. Security Principles

The architecture follows several important security principles.

### 8.1 Least Privilege

Users should only have the permissions required for their responsibilities.

For example, an employee should not have administrative permissions.

### 8.2 Defense in Depth

Security should exist at multiple layers rather than relying on a single security control.

```text
Frontend Security
       +
API Security
       +
Authentication
       +
Authorization
       +
Input Validation
       +
Database Security
       +
Logging & Monitoring
```

If one security control fails, additional controls should still provide protection.

### 8.3 Secure by Design

Security requirements are considered during architecture and development rather than being added only after the application is completed.

### 8.4 Separation of Responsibilities

Frontend, backend, and database responsibilities are separated to reduce complexity and limit security risks.

---

## 9. Request Flow

A typical request follows this path:

```text
1. User
     │
     ▼
2. Angular Frontend
     │
     ▼
3. HTTPS Request
     │
     ▼
4. Spring Boot API
     │
     ├── Authenticate
     ├── Authorize
     ├── Validate Input
     └── Execute Business Logic
             │
             ▼
5. PostgreSQL
             │
             ▼
6. Response
             │
             ▼
7. Angular
             │
             ▼
8. User
```

For example, when an employee wants to view their incidents:

```text
Employee
   │
   ▼
Angular
   │
   │ GET /api/incidents/my
   ▼
Spring Boot
   │
   ├── Verify authentication
   ├── Check employee permissions
   ├── Validate request
   └── Query database
          │
          ▼
      PostgreSQL
          │
          ▼
     Incident data
          │
          ▼
     Spring Boot
          │
          ▼
       Angular
          │
          ▼
       Employee
```

---

## 10. Security Boundary

The backend represents an important security boundary.

```text
                 TRUST BOUNDARY
                       │
                       ▼
User ──→ Angular ──→ Spring Boot ──→ PostgreSQL
                       │
                       │
                 Security Controls
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
    Authentication Authorization Validation
```

The application must never trust information received from the client.

For example, if the frontend sends:

```json
{
  "role": "ADMIN"
}
```

the backend must not automatically trust that value.

The backend must determine the authenticated user's actual permissions.

---

## 11. API Security

The backend API will apply security controls to API endpoints.

Examples:

```text
Public endpoints
    │
    ├── Login
    └── Health check

Authenticated endpoints
    │
    ├── User profile
    ├── Incidents
    └── Reports

Administrative endpoints
    │
    ├── User management
    ├── Role management
    └── Security configuration
```

Each endpoint will have an appropriate authorization policy.

---

## 12. Audit Logging

Security-sensitive actions will be recorded in audit logs.

Examples include:

* Login attempts
* Failed authentication
* User creation
* Role changes
* Incident creation
* Incident modification
* Incident closure
* Administrative actions

The objective is to provide traceability.

**Traceability** means being able to understand:

> Who performed an action, what action was performed, and when it happened.

These logs can later be integrated with monitoring and SIEM components.

---

## 13. Error Handling

The backend will use controlled error handling.

The application should not expose sensitive internal information to users.

For example, an API should not return:

```text
Database password
SQL query
Stack trace
Internal server paths
```

Instead, the API should return an appropriate error message while detailed technical information remains available in controlled application logs.

---

## 14. Future DevSecOps Integration

The application architecture will later be integrated into a complete DevSecOps pipeline.

Security tools will analyze different parts of the application:

```text
Source Code
     │
     ├── SAST
     ├── SCA
     └── Secret Scanning
             │
             ▼
       Build Application
             │
             ▼
       Build Container
             │
             ├── Container Scan
             │
             ▼
       Test Environment
             │
             └── DAST
             │
             ▼
       Security Gate
             │
       ┌─────┴─────┐
       ▼           ▼
      FAIL        PASS
       │           │
   Stop Build    Deploy
                   │
                   ▼
             Production
                   │
                   ▼
          Monitoring / SIEM
```

---

## 15. Architecture Goals

The architecture is designed to achieve the following goals:

* Separation of responsibilities
* Secure communication
* Strong authentication
* Role-based authorization
* Controlled database access
* Input validation
* Auditability
* Maintainability
* Testability
* Security automation
* Continuous monitoring

The architecture will evolve as the project progresses through the different DevSecOps phases.
