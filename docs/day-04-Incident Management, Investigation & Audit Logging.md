# Day 04 — Incident Management, Investigation & Audit Logging

## 1. Day 04 Objective

The objective of Day 04 was to build the **Security Operations Core** of the SecureOps Platform.

During this day, the project moved beyond basic authentication and authorization into real security operations.

The main goals were:

* Build incident management
* Implement incident lifecycle control
* Implement investigation notes
* Apply Role-Based Access Control (RBAC)
* Implement audit logging
* Audit authentication events
* Validate security boundaries
* Document and test the security controls

The main security idea is:

> A security application must not only protect access. It must also control actions, record important events, and provide evidence for investigation.

---

# 2. Security Operations Architecture

The Day 04 architecture can be represented as:

```text
                         SecureOps Platform
                                |
                +---------------+---------------+
                |                               |
          Incident Management              Security Controls
                |                               |
        +-------+-------+              +--------+--------+
        |       |       |              |        |        |
      Report  Assign  Status          RBAC    Audit   Validation
        |       |       |              |        |        |
        +-------+-------+--------------+--------+--------+
                                |
                                v
                           PostgreSQL
```

The application now has a security operations layer capable of managing incidents and recording security-relevant actions.

---

# 3. Incident Management

## 3.1 Purpose

Incident management allows users to report and track security incidents.

An incident contains information such as:

* Title
* Description
* Severity
* Category
* Status
* Reporter
* Assigned security analyst

The system stores incidents in the `incidents` table.

---

## 3.2 Incident Entity

The `Incident` entity represents a security incident.

Important fields include:

```text
id
title
description
severity
category
status
reportedBy
assignedTo
```

Relationships:

```text
Incident
   |
   +---- reportedBy ----> User
   |
   +---- assignedTo ----> User
```

The reporter represents the authenticated user who created the incident.

The assigned user represents the security analyst responsible for the investigation.

---

# 4. Incident Severity

The application supports four severity levels:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

Severity represents the importance or potential impact of the incident.

Example:

```text
LOW
    |
    | Normal security event
    |
MEDIUM
    |
    | Important security incident
    |
HIGH
    |
    | Serious security impact
    |
CRITICAL
```

---

# 5. Incident Categories

The application supports:

```text
AUTHENTICATION_ATTACK
MALWARE
PHISHING
UNAUTHORIZED_ACCESS
DATA_BREACH
NETWORK_ATTACK
OTHER
```

These categories help security analysts classify incidents consistently.

---

# 6. Incident Lifecycle

The application implements a controlled incident lifecycle.

```text
OPEN
  |
  v
ASSIGNED
  |
  v
INVESTIGATING
  |
  v
CONTAINED
  |
  v
RESOLVED
  |
  v
CLOSED
```

Each state represents a stage of the incident response process.

---

## 6.1 OPEN

The incident has been reported but has not yet been assigned to a security analyst.

Example:

```text
Employee reports suspicious email
        |
        v
     OPEN
```

---

## 6.2 ASSIGNED

The incident has been assigned to a security analyst.

```text
OPEN
  |
  | Assignment
  v
ASSIGNED
```

---

## 6.3 INVESTIGATING

The assigned analyst is actively investigating the incident.

```text
ASSIGNED
    |
    v
INVESTIGATING
```

---

## 6.4 CONTAINED

The threat has been contained.

For example, a compromised endpoint may be isolated from the network.

```text
INVESTIGATING
      |
      v
  CONTAINED
```

---

## 6.5 RESOLVED

The incident has been resolved.

```text
CONTAINED
    |
    v
RESOLVED
```

---

## 6.6 CLOSED

The incident has completed the response lifecycle.

```text
RESOLVED
    |
    v
 CLOSED
```

---

# 7. Lifecycle Security

The backend does not allow arbitrary status changes.

For example:

```text
ASSIGNED → INVESTIGATING
```

is valid.

But:

```text
ASSIGNED → CLOSED
```

is invalid.

The service checks the current state before applying a new state.

This prevents users from bypassing the intended incident-response workflow by directly manipulating the API.

Example:

```text
PATCH /api/incidents/8/status

{
    "status": "CLOSED"
}
```

If the current state does not allow this transition, the backend returns:

```text
400 Bad Request
```

with an error describing the invalid transition.

This is an example of enforcing **business logic at the backend**, rather than trusting the frontend.

---

# 8. Role-Based Access Control

## 8.1 Concept

RBAC means:

> Access is granted according to the user's role.

SecureOps uses four roles:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

The roles have different permissions.

---

# 9. Employee Permissions

Employees can report security incidents.

```text
EMPLOYEE
   |
   +---- Create incident       ✅
   |
   +---- View protected incidents   ❌
   |
   +---- Change status        ❌
   |
   +---- Assign analyst       ❌
   |
   +---- Investigation notes ❌
   |
   +---- Audit logs           ❌
```

This follows the principle of **least privilege**.

An employee does not need the same security privileges as an analyst or administrator.

---

# 10. Security Analyst Permissions

Security analysts are responsible for investigation.

```text
SECURITY_ANALYST
   |
   +---- View incidents           ✅
   |
   +---- Assign incidents         ✅
   |
   +---- Change status            ✅
   |
   +---- View investigation notes ✅
   |
   +---- Create notes             ✅
   |
   +---- View audit logs          ✅
```

---

# 11. Manager Permissions

Managers can monitor incidents and review investigation information.

```text
MANAGER
   |
   +---- View incidents           ✅
   |
   +---- View investigation notes ✅
   |
   +---- Modify incidents        ❌
   |
   +---- Create investigation notes ❌
   |
   +---- View audit logs          ❌
```

The manager has visibility but does not receive security-analyst privileges.

---

# 12. Administrator Permissions

Administrators have broad management permissions.

```text
ADMIN
   |
   +---- Manage incidents    ✅
   |
   +---- Assign incidents    ✅
   |
   +---- Change status       ✅
   |
   +---- View audit logs     ✅
```

Administrative permissions will be expanded later with dedicated user and role management.

---

# 13. Investigation Notes

## 13.1 Purpose

Investigation notes allow security analysts to record information discovered during incident investigation.

Examples:

```text
- Suspicious IP identified
- Malicious process detected
- Endpoint isolated
- User account disabled
- Evidence collected
```

The notes are linked to the incident.

---

## 13.2 Data Model

```text
Incident
   |
   +---- InvestigationNote
   |
   +---- InvestigationNote
   |
   +---- InvestigationNote
```

Each investigation note contains:

```text
id
content
incident
createdBy
```

The `createdBy` relationship records which authenticated user created the note.

---

# 14. Investigation Note Security

Only authorized roles can access investigation notes.

### Read

```text
ADMIN              → ALLOW
SECURITY_ANALYST   → ALLOW
MANAGER            → ALLOW
EMPLOYEE           → DENY
```

### Create

```text
ADMIN              → ALLOW
SECURITY_ANALYST   → ALLOW
MANAGER            → DENY
EMPLOYEE           → DENY
```

This separates **investigation visibility** from **investigation modification**.

---

# 15. Input Validation

Validation was implemented to prevent invalid or unexpected input.

Examples include:

```text
Incident title
    → required
    → maximum 150 characters

Incident description
    → required
    → maximum 2000 characters

Severity
    → required

Category
    → required

Investigation note
    → required
    → maximum 5000 characters

Analyst email
    → required
    → valid email format
```

Invalid requests return:

```text
400 Bad Request
```

The application uses a global exception handler to provide structured validation errors.

---

# 16. Audit Logging

## 16.1 Purpose

Audit logging records important actions performed inside the application.

The main questions are:

```text
WHO?
WHAT?
WHEN?
```

For example:

```text
Who:
admin@example.com

What:
ASSIGN

When:
2026-09-19 17:22
```

This provides accountability.

---

# 17. Audit Log Architecture

The audit logging flow is:

```text
User Action
     |
     v
Controller
     |
     v
Service
     |
     v
AuditLogService
     |
     v
AuditLogRepository
     |
     v
PostgreSQL
     |
     v
audit_logs
```

Audit events are generated automatically by backend services.

---

# 18. Audit Log Data Model

The `AuditLog` entity contains:

```text
id
user
action
resourceType
resourceId
timestamp
details
```

Example:

```text
id:           6
user_id:      3
action:       LOGIN_SUCCESS
resourceType: AUTHENTICATION
resourceId:   0
timestamp:     ...
details:      Successful authentication
```

---

# 19. Incident Audit Events

## 19.1 Incident Creation

When an authenticated user creates an incident:

```text
Action:
CREATE

Resource:
INCIDENT
```

Example:

```text
Incident created
```

The authenticated reporter is recorded as the actor.

---

## 19.2 Incident Assignment

When an incident is assigned:

```text
Action:
ASSIGN

Resource:
INCIDENT
```

Example:

```text
Incident assigned to analyst@example.com
```

The system distinguishes between:

```text
Actor
Target
```

For example:

```text
Actor:
admin@example.com

Target:
analyst@example.com
```

This is important for accountability.

The person performing the action must not be confused with the person receiving the assignment.

---

## 19.3 Status Change

When the incident status changes:

```text
Action:
STATUS_CHANGE

Resource:
INCIDENT
```

Example:

```text
Status changed from ASSIGNED to INVESTIGATING
```

The authenticated user who performed the change is recorded.

---

# 20. Authentication Audit Events

Authentication activity is also audited.

The application records:

```text
LOGIN_SUCCESS
LOGIN_FAILURE
```

---

## 20.1 Successful Login

When authentication succeeds:

```text
User
  |
  v
Authentication succeeds
  |
  v
LOGIN_SUCCESS
  |
  v
Audit log
```

Example:

```text
Action:
LOGIN_SUCCESS

Resource:
AUTHENTICATION

Details:
Successful authentication
```

---

## 20.2 Failed Login

When authentication fails:

```text
User
  |
  v
Authentication fails
  |
  v
LOGIN_FAILURE
  |
  v
Audit log
```

Example:

```text
Action:
LOGIN_FAILURE

Resource:
AUTHENTICATION

Details:
Authentication failed
```

---

# 21. Unknown User Authentication Failure

The system also handles a failed login for an email that does not exist.

Example:

```text
Email:
unknown@example.com
```

There is no matching user in the database.

The resulting audit event is:

```text
LOGIN_FAILURE
user_id = NULL
```

This is intentional.

The system records the security event without creating a fake user.

This can later help detect suspicious authentication activity.

---

# 22. Sensitive Data Protection

Audit logging must not become a source of credential leakage.

The application does NOT store:

```text
❌ Plain-text passwords
❌ Password hashes
❌ JWT tokens
❌ Authentication tokens
```

Authentication audit events contain only information necessary for security monitoring.

This follows the principle of **data minimization**.

---

# 23. Audit Log Access Control

The audit-log API is:

```text
GET /api/audit-logs
```

Access is restricted using Spring Security.

Allowed roles:

```text
ADMIN
SECURITY_ANALYST
```

Denied roles:

```text
MANAGER
EMPLOYEE
```

This prevents normal application users from accessing sensitive security history.

---

# 24. Security Testing

Day 04 included security testing of the implemented controls.

## 24.1 Audit Log RBAC

| Role             | Result         | Status |
| ---------------- | -------------- | ------ |
| ADMIN            | Access allowed | 200    |
| SECURITY_ANALYST | Access allowed | 200    |
| MANAGER          | Access denied  | 403    |
| EMPLOYEE         | Access denied  | 403    |

All tests passed.

---

# 25. Authentication Testing

The following scenarios were tested.

| Scenario                       | Expected Event | Result |
| ------------------------------ | -------------- | ------ |
| Valid credentials              | LOGIN_SUCCESS  | PASS   |
| Existing user + wrong password | LOGIN_FAILURE  | PASS   |
| Unknown email                  | LOGIN_FAILURE  | PASS   |

The database confirmed the expected events.

---

# 26. Incident Security Testing

The incident API was tested against different roles.

Examples:

```text
EMPLOYEE
    |
    +---- Create incident      → ALLOWED
    |
    +---- Change status        → DENIED
    |
    +---- Assign analyst       → DENIED
```

```text
SECURITY_ANALYST
    |
    +---- View incidents       → ALLOWED
    |
    +---- Assign incident      → ALLOWED
    |
    +---- Change status        → ALLOWED
```

```text
MANAGER
    |
    +---- View incidents       → ALLOWED
    |
    +---- Modify incident      → DENIED
```

These tests demonstrate that authorization is enforced on the backend.

---

# 27. Validation Testing

The following invalid inputs were tested:

```text
Blank incident title
Blank description
Missing severity
Missing category
Title exceeding maximum length
Description exceeding maximum length
Blank investigation note
Investigation note exceeding maximum length
Invalid analyst email
Non-existent analyst
Assignment to a manager
Non-existent incident
Invalid status transition
```

The API correctly rejected invalid requests.

---

# 28. Fake JWT Testing

A fake JWT was submitted to protected endpoints.

The application denied access:

```text
403 Forbidden
```

The request did not receive protected application data.

This demonstrates that invalid authentication tokens cannot be used to access protected resources.

---

# 29. IDOR Review

IDOR means:

> Insecure Direct Object Reference.

During testing, the incident API did not expose a direct:

```text
GET /api/incidents/{id}
```

endpoint.

Therefore, the classic incident IDOR scenario was not applicable to the current API surface.

This should be tested again if direct incident retrieval is added in the future.

---

# 30. Day 04 Security Concepts Learned

## RBAC

Role-Based Access Control determines what a user can do based on their role.

```text
User → Role → Permissions
```

---

## Authentication

Authentication answers:

> Who are you?

Example:

```text
email + password
       |
       v
Authentication
```

---

## Authorization

Authorization answers:

> What are you allowed to do?

Example:

```text
EMPLOYEE
   |
   +---- Create incident → YES
   |
   +---- Change status  → NO
```

---

## Audit Logging

Audit logging answers:

> Who did what and when?

```text
Actor + Action + Time + Resource
```

---

## Least Privilege

Users should receive only the permissions they need.

```text
EMPLOYEE
    ↓
Limited permissions

ANALYST
    ↓
Investigation permissions

ADMIN
    ↓
Administrative permissions
```

---

## Backend Security

Security controls must be enforced by the backend.

The frontend cannot be trusted to enforce authorization.

For example:

```text
Frontend:
"Hide the Close button"

        ≠

Backend:
"Reject unauthorized PATCH request"
```

The backend enforcement is the real security boundary.

---

# 31. Day 04 Final Architecture

```text
                         SECUREOPS PLATFORM
                                |
              +-----------------+-----------------+
              |                                   |
       INCIDENT MANAGEMENT                  SECURITY LAYER
              |                                   |
      +-------+-------+                  +--------+--------+
      |       |       |                  |        |        |
    Report  Assign  Status              RBAC    Audit   Validation
      |       |       |                  |        |        |
      +-------+-------+------------------+--------+--------+
              |                                   |
              v                                   v
       Investigation Notes                 Authentication
              |                                   |
              +-------------------+---------------+
                                  |
                                  v
                             PostgreSQL
```

---

# 32. Day 04 Deliverables

The following deliverables were completed:

```text
[x] Incident entity
[x] Incident repository
[x] Incident service
[x] Incident controller
[x] Incident lifecycle
[x] Severity and categories
[x] Incident assignment
[x] Investigation notes
[x] Investigation note API
[x] RBAC enforcement
[x] Input validation
[x] Global exception handling
[x] AuditLog entity
[x] AuditLog repository
[x] AuditLog service
[x] AuditLog controller
[x] Incident CREATE auditing
[x] Incident ASSIGN auditing
[x] Incident STATUS_CHANGE auditing
[x] LOGIN_SUCCESS auditing
[x] LOGIN_FAILURE auditing
[x] Unknown-user failure auditing
[x] Audit-log RBAC
[x] Security testing
[x] Documentation
[x] Git checkpoint
```

---

# 33. Day 04 Result

Day 04 transformed SecureOps Platform from a basic authenticated application into a more realistic security operations platform.

The application can now:

```text
Report incidents
      ↓
Assign analysts
      ↓
Investigate
      ↓
Track lifecycle
      ↓
Resolve
      ↓
Close
      ↓
Record important actions
      ↓
Support security investigation
```

The main lesson is:

> **Security is not only about preventing access. A secure system must also control actions, validate business rules, record important events, and provide evidence for investigation.**

---

# 34. Next Step

The next planned feature is:

```text
DAY 05
   |
   v
Task 13 — User & Role Management
```

This will extend the administrative security layer with user management and role management.

The expected direction is:

```text
ADMIN
  |
  +---- View users
  |
  +---- Create users
  |
  +---- Manage roles
  |
  +---- Manage account status
  |
  +---- Audit administrative actions
```

Day 04 is therefore considered **COMPLETE**.
