# Day 05 — User & Role Management

> **Project:** SecureOps Platform
> **Day:** 05
> **Topic:** User & Role Management
> **Security Focus:** Authentication, Authorization, RBAC, Account Security, Audit Logging

---

# 1. Day 05 Objective

The objective of Day 05 was to build a secure **User and Role Management system** for the SecureOps Platform.

The system must allow authorized administrators to manage users while preventing unauthorized users from accessing administrative operations.

The main objectives were:

* Manage application users
* View users
* Create users
* Update user information
* Manage user roles
* Manage account status
* Protect passwords
* Implement administrative RBAC
* Prevent privilege escalation
* Prevent administrative lockout
* Validate user-management requests
* Audit security-sensitive actions
* Test the complete security model

---

# 2. Security Problem

User management is a sensitive part of an application.

If an attacker can access administrative user-management APIs, they may be able to:

```text
Create an ADMIN account
        ↓
Change their own role
        ↓
Disable legitimate administrators
        ↓
Take control of the application
```

Therefore, user-management functionality must be protected at the backend level.

The SecureOps Platform uses:

```text
JWT Authentication
        +
Role-Based Access Control
        +
Input Validation
        +
Password Hashing
        +
Security Rules
        +
Audit Logging
```

---

# 3. Day 05 Architecture

The main security architecture is:

```text
                         Client
                           |
                           v
                     Login Request
                           |
                           v
                     Authentication
                           |
                           v
                          JWT
                           |
                           v
              Authorization: Bearer JWT
                           |
                           v
              JwtAuthenticationFilter
                           |
                           v
                 Spring Security
                           |
                           v
                        RBAC
                           |
              +------------+------------+
              |                         |
            ADMIN                  Other Roles
              |                         |
              v                         v
       User Management                403
              |
              v
       UserController
              |
              v
         UserService
              |
              v
        UserRepository
              |
              v
          PostgreSQL
              |
              v
         Audit Logging
```

The important security boundary is:

```text
/api/admin/**
```

Only users with the `ADMIN` role can access this area.

---

# 4. User Model

The `User` entity represents an application account.

The main attributes are:

```text
User
├── id
├── email
├── passwordHash
├── role
└── status
```

## 4.1 ID

The `id` uniquely identifies the user.

Example:

```text
1
2
3
```

---

## 4.2 Email

The email is the user's login identifier.

It is also unique in the database.

Therefore, two users cannot have the same email.

---

## 4.3 Password Hash

The database does not store the user's plaintext password.

Instead:

```text
Password
    ↓
BCrypt
    ↓
Password Hash
    ↓
Database
```

The `passwordHash` field is also excluded from JSON responses.

This prevents accidental password-hash exposure through the API.

---

## 4.4 Role

Each user has one application role.

Supported roles:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

---

## 4.5 Account Status

Each account has a status:

```text
ACTIVE
DISABLED
```

This allows administrators to disable an account without deleting the user.

---

# 5. User Management API

The administrative API is:

```text
/api/admin/users
```

The implemented endpoints are:

| Method | Endpoint                       | Purpose       | Access |
| ------ | ------------------------------ | ------------- | ------ |
| GET    | `/api/admin/users`             | List users    | ADMIN  |
| GET    | `/api/admin/users/{id}`        | View user     | ADMIN  |
| POST   | `/api/admin/users`             | Create user   | ADMIN  |
| PATCH  | `/api/admin/users/{id}`        | Update user   | ADMIN  |
| PATCH  | `/api/admin/users/{id}/role`   | Change role   | ADMIN  |
| PATCH  | `/api/admin/users/{id}/status` | Change status | ADMIN  |

The security configuration protects the administrative API using:

```text
/api/admin/**
```

with:

```text
ADMIN role required
```

---

# 6. User Response Security

The application does not directly expose sensitive user information.

A dedicated DTO called:

```text
UserResponse
```

is used.

The API response contains:

```text
id
email
role
status
```

It does not contain:

```text
passwordHash
```

This follows the security principle:

> Return only the information required by the client.

---

# 7. Creating a User

An administrator can create a new user.

Endpoint:

```text
POST /api/admin/users
```

The request contains:

```text
email
password
role
```

The backend performs:

```text
Receive request
      ↓
Validate request
      ↓
Check email uniqueness
      ↓
Hash password
      ↓
Assign role
      ↓
Set status = ACTIVE
      ↓
Save user
      ↓
Create audit event
```

New users are automatically created as:

```text
ACTIVE
```

---

# 8. Updating a User

An administrator can update:

* Email
* Password

Endpoint:

```text
PATCH /api/admin/users/{id}
```

If the email changes, the backend checks whether the new email already exists.

If it already exists:

```text
409 Conflict
```

If the password changes, the new password is hashed using BCrypt.

The plaintext password is never stored.

The operation generates:

```text
USER_UPDATED
```

in the audit log.

---

# 9. Role Management

The role-management endpoint is:

```text
PATCH /api/admin/users/{id}/role
```

The administrator can assign one of the supported roles:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

The role is represented using a Java enum.

This provides strict role validation.

For example:

```text
SECURITY_ANALYST
```

is valid.

But:

```text
SUPER_ADMIN
```

is not a valid application role.

The API rejects the invalid value with:

```text
400 Bad Request
```

---

# 10. Role-Based Access Control

The application uses **RBAC — Role-Based Access Control**.

The idea is simple:

```text
User
 ↓
Role
 ↓
Permissions
```

For administrative operations:

```text
ADMIN
  ↓
User Management
Role Management
Account Status Management
```

Other roles cannot access these APIs.

The resulting model is:

```text
                    ADMIN
                      |
        +-------------+-------------+
        |             |             |
       Users         Roles        Status


              SECURITY_ANALYST
                      |
                      X
              User Management


                  MANAGER
                      |
                      X
              User Management


                 EMPLOYEE
                      |
                      X
              User Management
```

---

# 11. Privilege Escalation Protection

One of the main security goals of Day 05 was preventing privilege escalation.

For example, an employee must not be able to do:

```text
EMPLOYEE
   ↓
PATCH /api/admin/users/5/role
   ↓
role = ADMIN
```

The request is rejected:

```text
403 Forbidden
```

The same protection was tested for:

* Employee
* Manager
* Security Analyst

Therefore, normal users cannot use the administrative API to promote themselves.

---

# 12. JWT Identity Protection

The backend does not trust an `actor` or administrator identity supplied by the client.

Instead, the authenticated identity comes from:

```text
JWT
 ↓
Spring Security
 ↓
Authentication
```

The application obtains the actor using the authenticated security context.

Conceptually:

```text
Client
  |
  v
JWT
  |
  v
Who is authenticated?
  |
  v
Is the user ADMIN?
  |
  +---- NO ---> 403
  |
 YES
  |
  v
Perform operation
  |
  v
Audit actor = JWT identity
```

This prevents clients from pretending to be another administrator.

---

# 13. Account Status

Day 05 introduced account lifecycle management.

Two states are supported:

```text
ACTIVE
DISABLED
```

The status-management endpoint is:

```text
PATCH /api/admin/users/{id}/status
```

The normal lifecycle is:

```text
        +---------+
        | ACTIVE  |
        +---------+
             |
             | Disable
             v
       +-------------+
       |  DISABLED   |
       +-------------+
             |
             | Reactivate
             v
        +---------+
        | ACTIVE  |
        +---------+
```

---

# 14. Disabled Account Protection

A disabled user cannot log in.

The authentication flow checks the account status.

The security model is:

```text
ACTIVE
  ↓
Login allowed


DISABLED
  ↓
Login rejected
  ↓
401 Unauthorized
```

This is important when an administrator needs to immediately stop an account from authenticating.

For example:

```text
Compromised account
       ↓
ADMIN disables account
       ↓
Account status = DISABLED
       ↓
Future login rejected
```

---

# 15. Last Active ADMIN Protection

A special self-protection rule was implemented.

The system must never accidentally remove its final active administrator.

Before disabling an ADMIN account, the application counts:

```text
ACTIVE ADMIN accounts
```

If there is only one active administrator, the operation is rejected.

Example:

```text
Active ADMIN count = 1
          ↓
Try to disable ADMIN
          ↓
       REJECT
          ↓
409 Conflict
```

The error is:

```text
Cannot disable the last active ADMIN
```

This protects the system against administrative lockout.

---

# 16. Password Security

Password security is based on BCrypt.

The password lifecycle is:

```text
User enters password
        ↓
Backend receives password
        ↓
BCrypt PasswordEncoder
        ↓
Password hash
        ↓
PostgreSQL
```

During authentication:

```text
Login password
      ↓
BCrypt verification
      ↓
Compare with stored hash
      ↓
Authentication result
```

The application does not need to decrypt passwords because BCrypt is used for password verification.

---

# 17. Password Validation

Passwords must contain at least 8 characters.

Example:

```text
123
```

is rejected.

Result:

```text
400 Bad Request
```

The following password-related tests were performed:

* Short password
* Empty password
* Wrong password
* Correct password after password update
* Old password after password update

The old password stopped working after the password was changed.

---

# 18. Validation

Day 05 includes centralized request validation.

Important validation cases:

| Input               | Expected |
| ------------------- | -------: |
| Invalid email       |      400 |
| Empty email         |      400 |
| Invalid role        |      400 |
| Invalid status      |      400 |
| Empty password      |      400 |
| Password too short  |      400 |
| Unknown user        |      404 |
| Duplicate email     |      409 |
| Unauthorized role   |      403 |
| Invalid credentials |      401 |

The application uses a centralized:

```text
GlobalExceptionHandler
```

This provides consistent error responses.

---

# 19. Error Handling Model

The final error model is:

```text
400
Bad Request
↓
Invalid input


401
Unauthorized
↓
Authentication failure


403
Forbidden
↓
Insufficient privileges


404
Not Found
↓
Target user does not exist


409
Conflict
↓
Business/security conflict
```

Example:

```text
PATCH /api/admin/users/999
```

when user `999` does not exist:

```text
404 Not Found
```

---

# 20. Audit Logging

Security-sensitive user-management operations are logged.

Implemented audit events include:

```text
USER_CREATED
USER_UPDATED
ROLE_CHANGED
USER_DISABLED
USER_ENABLED
```

Authentication events also include:

```text
LOGIN_SUCCESS
LOGIN_FAILURE
```

Each administrative audit record contains:

```text
Actor
Action
Resource Type
Resource ID
Timestamp
Details
```

Example:

```text
Actor:
admin@example.com

Action:
ROLE_CHANGED

Resource Type:
USER

Resource ID:
6

Details:
Changed role for user:
audit.test@example.com
to SECURITY_ANALYST
```

---

# 21. Audit Logging Flow

The audit flow is:

```text
ADMIN performs action
        ↓
UserController
        ↓
UserService
        ↓
Database update
        ↓
AuditLogService
        ↓
AuditLog
        ↓
PostgreSQL
```

This creates accountability for administrative operations.

---

# 22. Audit Log Filtering

The audit API supports filtering.

Available filters:

```text
action
resourceType
actor
```

Examples:

```text
GET /api/audit-logs?action=ROLE_CHANGED
```

```text
GET /api/audit-logs?resourceType=USER
```

```text
GET /api/audit-logs?actor=admin@example.com
```

Filters can also be combined.

Example:

```text
GET /api/audit-logs?resourceType=USER&action=ROLE_CHANGED
```

This is useful for security investigations.

---

# 23. Audit Trail Protection

The audit log is protected.

Only:

```text
ADMIN
SECURITY_ANALYST
```

can read audit logs.

The application does not expose API endpoints for:

```text
POST /api/audit-logs
PATCH /api/audit-logs/{id}
DELETE /api/audit-logs/{id}
```

Unauthorized attempts to modify audit logs were tested and rejected.

Therefore, from the application API perspective, the audit trail behaves as:

```text
Append
  ↓
Read
  ↓
No application-level modification
  ↓
No application-level deletion
```

---

# 24. Security Testing

Day 05 included a complete end-to-end security test.

## Authentication

### Test 1 — ADMIN Login

```text
Result: PASS
```

The administrator successfully received a JWT.

---

### Test 2 — Disabled User Login

```text
Result: PASS
```

A disabled user was rejected with:

```text
401 Unauthorized
```

---

### Test 3 — Reactivated User Login

```text
Result: PASS
```

After reactivation, the user successfully authenticated again.

---

### Test 4 — Unknown User

```text
Result: PASS
```

Unknown credentials returned:

```text
401 Unauthorized
```

---

# 25. Authorization Testing

The following role tests were performed:

| Role             | Administrative API |
| ---------------- | ------------------ |
| ADMIN            | PASS               |
| SECURITY_ANALYST | DENIED             |
| MANAGER          | DENIED             |
| EMPLOYEE         | DENIED             |
| Unauthenticated  | DENIED             |

The expected authorization response for unauthorized users is:

```text
403 Forbidden
```

---

# 26. Privilege Escalation Tests

The following attacks were tested:

```text
Employee → ADMIN
Manager → ADMIN
Analyst → ADMIN
```

All attempts were rejected.

Also tested:

```text
Non-admin → Change another user's role
Non-admin → Disable another user's account
```

All were rejected.

This confirms that administrative operations are protected at the backend.

---

# 27. Input Security Tests

The following invalid inputs were tested:

```text
Invalid email
Empty email
Duplicate email
Invalid role
Invalid status
Empty password
Short password
Nonexistent user
```

The API returned the expected error categories.

---

# 28. Self-Protection Tests

The following rules were tested:

```text
Last active ADMIN cannot be disabled
```

Result:

```text
409 Conflict
```

Target user validation:

```text
Nonexistent user
      ↓
404 Not Found
```

Role validation:

```text
Invalid role
      ↓
400 Bad Request
```

Status validation:

```text
Invalid status
      ↓
400 Bad Request
```

---

# 29. Audit Tests

Administrative actions were verified in the database and through the API.

For a temporary test user, the following events were generated:

```text
USER_CREATED
ROLE_CHANGED
USER_DISABLED
USER_ENABLED
```

The actor was correctly recorded as:

```text
admin@example.com
```

This confirmed that audit events correctly identify the authenticated administrator.

---

# 30. Database Verification

After completing the tests, the database was checked.

The final intentional users were:

```text
ID   EMAIL                         ROLE
---------------------------------------------------------
1    alice.updated@example.com    EMPLOYEE
2    admin@example.com            ADMIN
3    analyst@example.com          SECURITY_ANALYST
4    manager@example.com          MANAGER
5    test.employee@example.com    SECURITY_ANALYST
6    audit.test@example.com       SECURITY_ANALYST
```

All intentional accounts were confirmed as:

```text
ACTIVE
```

Temporary test data was removed after the final security tests.

This keeps the development database clean before the next project phase.

---

# 31. Threats Addressed

Day 05 addresses several important security threats.

## 31.1 Privilege Escalation

Protection:

```text
/api/admin/**
       ↓
ADMIN only
```

---

## 31.2 Password Exposure

Protection:

```text
Password
   ↓
BCrypt
   ↓
Hash
   ↓
Database
```

---

## 31.3 Sensitive Data Exposure

Protection:

```text
UserResponse
   ↓
id
email
role
status
```

The password hash is not exposed.

---

## 31.4 Unauthorized User Management

Protection:

```text
Spring Security
      ↓
ADMIN check
      ↓
Administrative API
```

---

## 31.5 Account Takeover

Protection:

```text
Account disabled
      ↓
Authentication rejected
```

---

## 31.6 Administrative Lockout

Protection:

```text
Count active ADMIN
      ↓
Last ADMIN?
      ↓
Reject disabling
```

---

## 31.7 Lack of Accountability

Protection:

```text
Administrative action
        ↓
Audit event
        ↓
Actor + action + target + timestamp
```

---

# 32. Important Engineering Lessons

## Authentication ≠ Authorization

Authentication answers:

```text
Who are you?
```

Authorization answers:

```text
What are you allowed to do?
```

In SecureOps:

```text
JWT
 ↓
Authentication
 ↓
RBAC
 ↓
Authorization
```

---

## Never Trust the Client

The backend must not trust the client to define:

```text
Actor
Role
Permission
Authorization
```

The backend determines these values using authenticated security information.

---

## Frontend Security Is Not Enough

Hiding an ADMIN button in the frontend is not real authorization.

An attacker can directly call the API.

Therefore:

```text
Frontend restriction
       +
Backend authorization
```

is required.

The backend is the final security boundary.

---

## Security Actions Should Be Audited

Changing roles and disabling accounts are security-sensitive operations.

Therefore:

```text
Security Action
      ↓
Audit Log
```

is necessary for accountability and investigation.

---

# 33. Day 05 Final Security Flow

The complete flow is:

```text
                         User
                          |
                          v
                       Login
                          |
                          v
                         JWT
                          |
                          v
                Authorization Header
                          |
                          v
              JwtAuthenticationFilter
                          |
                          v
                   Spring Security
                          |
                          v
                       RBAC
                          |
                  +-------+-------+
                  |               |
                ADMIN          Non-ADMIN
                  |               |
                  v               v
          User Management       403
                  |
                  v
             Validation
                  |
                  v
         Security Business Rules
                  |
                  v
            User Operation
                  |
                  v
             Audit Event
                  |
                  v
              PostgreSQL
```

---

# 34. Day 05 Deliverables

The following components were implemented:

```text
User
AccountStatus
Role

UserRepository
UserService

UserResponse
CreateUserRequest
UpdateUserRequest
UpdateUserRoleRequest
UpdateUserStatusRequest

UserController

GlobalExceptionHandler

CustomUserDetailsService

Administrative RBAC
Password Security
Account Status Management
Audit Logging
Audit Filtering
```

---

# 35. Final Checklist

## User Management

* [x] Review User entity
* [x] Review UserRepository
* [x] Review UserService
* [x] Define user-management API
* [x] Create UserResponse DTO
* [x] Protect passwordHash
* [x] List users
* [x] View user
* [x] Create user
* [x] Update user
* [x] Disable user
* [x] Reactivate user

## Role Management

* [x] ADMIN can view roles
* [x] ADMIN can change roles
* [x] Role validation
* [x] Non-admin role protection
* [x] Self-promotion protection
* [x] Role modification testing

## Account Status

* [x] ACTIVE status
* [x] DISABLED status
* [x] New users default to ACTIVE
* [x] ADMIN can disable accounts
* [x] ADMIN can reactivate accounts
* [x] Disabled accounts cannot authenticate
* [x] Disabled-account login tested

## Administrative RBAC

* [x] ADMIN can access `/api/admin/users`
* [x] SECURITY_ANALYST denied
* [x] MANAGER denied
* [x] EMPLOYEE denied
* [x] Unauthenticated access denied

## Self-Protection

* [x] Last active ADMIN protection
* [x] Target user validation
* [x] Target role validation
* [x] Target status validation
* [x] JWT identity used as actor
* [x] No client-controlled actor identity

## Password Security

* [x] BCrypt hashing
* [x] No plaintext password storage
* [x] No password hash in responses
* [x] Password validation
* [x] Wrong password rejected
* [x] Password update tested

## Validation

* [x] Invalid email
* [x] Empty email
* [x] Duplicate email
* [x] Invalid role
* [x] Invalid status
* [x] Empty password
* [x] Short password
* [x] Nonexistent user
* [x] Centralized error handling

## Audit Logging

* [x] USER_CREATED
* [x] USER_UPDATED
* [x] ROLE_CHANGED
* [x] USER_DISABLED
* [x] USER_ENABLED
* [x] LOGIN_SUCCESS
* [x] LOGIN_FAILURE
* [x] Actor recorded
* [x] Target recorded
* [x] Timestamp recorded
* [x] Audit filtering
* [x] Audit write protection

## Testing

* [x] Authentication tests
* [x] Authorization tests
* [x] Privilege escalation tests
* [x] Input validation tests
* [x] Self-protection tests
* [x] Audit tests
* [x] Database verification
* [x] Temporary test-data cleanup

---

# 36. Final Status

```text
╔══════════════════════════════════════════════╗
║                                              ║
║       DAY 05 — USER & ROLE MANAGEMENT        ║
║                                              ║
║                 COMPLETE                     ║
║                                              ║
╚══════════════════════════════════════════════╝
```

The SecureOps Platform now has a protected administrative user-management layer with:

```text
JWT Authentication
        +
RBAC Authorization
        +
User Management
        +
Role Management
        +
Account Status
        +
BCrypt Password Security
        +
Input Validation
        +
Self-Protection
        +
Audit Logging
        +
Security Testing
```

**Day 05 is complete.**
