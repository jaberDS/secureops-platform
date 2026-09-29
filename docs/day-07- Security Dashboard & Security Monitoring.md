# DAY 07 — Security Dashboard & Security Monitoring

## SecureOps Platform

**Project:** SecureOps Platform  
**Day:** 07  
**Phase:** Angular Security Dashboard  
**Status:** ✅ Completed

---

# 1. Day 07 Overview

Day 07 focuses on building the **Security Operations Center (SOC) dashboard** of the SecureOps Platform.

The objective was to connect the Angular frontend to the Spring Boot backend and display security information using real application data.

The dashboard provides:

- Authentication-aware access
- Role-based navigation
- Security metrics
- Incident monitoring
- User monitoring
- Audit monitoring
- Security event monitoring
- JWT authentication
- Automatic audit-log refresh
- Calculated threat level
- Security activity visualization
- Service health information

The dashboard is designed to provide a central place where security activity can be monitored.

---

# 2. Objectives

The main objectives of Day 07 were:

- Build the Angular frontend foundation
- Create the login page
- Connect Angular to Spring Boot
- Implement JWT authentication
- Store the JWT securely in browser storage
- Send JWT automatically with API requests
- Protect Angular routes
- Implement role-based UI access
- Connect dashboard data to backend APIs
- Display users
- Display incidents
- Display audit logs
- Display security events
- Add automatic audit-log refresh
- Calculate security metrics from real backend data
- Handle authentication errors
- Verify the Angular production build

---

# 3. Architecture

The Day 07 architecture is:

```text
                    ┌──────────────────────┐
                    │      Browser         │
                    │      Angular        │
                    └──────────┬───────────┘
                               │
                               │ HTTP
                               │ JWT
                               ▼
                    ┌──────────────────────┐
                    │   Spring Boot API    │
                    │      Port 9000       │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │     PostgreSQL       │
                    │      Database        │
                    └──────────────────────┘