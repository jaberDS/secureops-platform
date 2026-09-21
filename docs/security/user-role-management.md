# User & Role Management Security

## 1. Overview

SecureOps Platform implements a centralized **User and Role Management** system.

The system allows administrators to:

* View users
* View a specific user
* Create users
* Update user information
* Change user roles
* Disable accounts
* Reactivate accounts

The system also protects administrative operations using **JWT authentication** and **Role-Based Access Control (RBAC)**.

The main security principle is:

```text
Authenticated User
       |
       v
    JWT Token
       |
       v
 Identify Actor
       |
       v
   Check Role
       |
       +------ ADMIN ------> Administrative Operation
       |
       +------ Other ------> 403 Forbidden
```

---

# 2. Security Architecture

The user-management security flow is:

```text
                Client
                  |
                  v
          POST /api/auth/login
                  |
                  v
             JWT Token
                  |
                  v
       Authorization: Bearer JWT
                  |
                  v
      JwtAuthenticationFilter
                  |
                  v
        User Authentication
                  |
                  v
         Spring Security RBAC
                  |
                  v
          Administrative API
                  |
                  v
            UserController
                  |
                  v
             UserService
                  |
                  v
             PostgreSQL
```

The JWT identifies the authenticated user.

The backend does not trust a user ID supplied by the client to identify the administrator performing an action.

Instead, the actor is obtained from the authenticated security context:

```java
Authentication authentication
```

The application then uses:

```java
authentication.getName()
```

to identify the authenticated administrator.

---

# 3. User Entity

The `User` entity represents an application account.

Main fields:

| Field          | Purpose                 |
| -------------- | ----------------------- |
| `id`           | Unique user identifier  |
| `email`        | Unique login identifier |
| `passwordHash` | BCrypt password hash    |
| `role`         | User authorization role |
| `status`       | Account state           |

The password is stored as:

```text
passwordHash
```

and never as plaintext.

The password field is also protected from JSON serialization using:

```java
@JsonIgnore
```

Therefore, the password hash is not returned by API responses.

---

# 4. Role Model

SecureOps Platform uses four roles:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

## ADMIN

Administrative privileges include:

* User management
* Role management
* Account status management
* Audit log access

Administrative endpoints are protected with:

```java
.hasRole("ADMIN")
```

## SECURITY_ANALYST

Security analysts can access security-related operations such as:

* Incident investigation
* Investigation notes
* Audit logs

They cannot manage users.

## MANAGER

Managers can access manager-authorized application operations.

They cannot manage users or change roles.

## EMPLOYEE

Employees have normal application privileges.

They cannot access administrative user-management operations.

---

# 5. Administrative API

The administrative API is:

```text
/api/admin/users
```

Available endpoints:

| HTTP  | Endpoint                       | Permission |
| ----- | ------------------------------ | ---------- |
| GET   | `/api/admin/users`             | ADMIN      |
| GET   | `/api/admin/users/{id}`        | ADMIN      |
| POST  | `/api/admin/users`             | ADMIN      |
| PATCH | `/api/admin/users/{id}`        | ADMIN      |
| PATCH | `/api/admin/users/{id}/role`   | ADMIN      |
| PATCH | `/api/admin/users/{id}/status` | ADMIN      |

The Spring Security configuration protects the complete administrative path:

```java
.requestMatchers("/api/admin/**")
.hasRole("ADMIN")
```

This creates a central security boundary around administrative functionality.

---

# 6. User Creation

Administrators can create users using:

```text
POST /api/admin/users
```

The request contains:

```text
email
password
role
```

The service performs the following operations:

```text
Receive request
      |
      v
Check email uniqueness
      |
      v
Hash password with BCrypt
      |
      v
Assign role
      |
      v
Set status = ACTIVE
      |
      v
Save user
      |
      v
Create audit event
```

Passwords are never stored directly.

Example:

```text
Input password
      |
      v
BCrypt
      |
      v
Password hash
      |
      v
Database
```

---

# 7. User Information Updates

Administrators can update user information using:

```text
PATCH /api/admin/users/{id}
```

Supported information includes:

* Email
* Password

When a password is changed, the new password is hashed again with BCrypt.

The previous password is never stored or returned.

The operation generates:

```text
USER_UPDATED
```

in the audit trail.

---

# 8. Role Management

Administrators can change a user's role with:

```text
PATCH /api/admin/users/{id}/role
```

Supported roles are defined by the Java enum:

```java
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

This prevents arbitrary role strings from being accepted.

For example:

```text
SECURITY_ANALYST
```

is valid.

An unknown value such as:

```text
SUPER_ADMIN
```

is rejected with:

```text
400 Bad Request
```

Role changes generate the following audit event:

```text
ROLE_CHANGED
```

The audit record contains:

* Actor
* Target user
* Action
* Resource type
* Resource ID
* Timestamp
* Details

---

# 9. Account Status

SecureOps Platform supports two account states:

```text
ACTIVE
DISABLED
```

The enum is:

```java
public enum AccountStatus {
    ACTIVE,
    DISABLED
}
```

New users are automatically created with:

```text
ACTIVE
```

Administrators can change account status through:

```text
PATCH /api/admin/users/{id}/status
```

---

# 10. Disabled Account Protection

A disabled account cannot authenticate.

The authentication flow checks the account status through the custom `UserDetailsService`.

The important security rule is:

```text
ACTIVE
  |
  +--> Login allowed

DISABLED
  |
  +--> Login rejected
```

Spring Security represents the disabled state through the `UserDetails` object.

Therefore, disabling an account immediately prevents authentication using that account.

The API returns:

```text
401 Unauthorized
```

for an attempted login with a disabled account.

---

# 11. Last Administrator Protection

The system contains an important self-protection rule.

The last active administrator cannot be disabled.

Before disabling an administrator, the service counts active administrators:

```text
Count ACTIVE ADMIN accounts
```

If:

```text
activeAdminCount <= 1
```

the operation is rejected.

The API returns:

```text
409 Conflict
```

with:

```text
Cannot disable the last active ADMIN
```

This protects the system from accidental administrative lockout.

Example:

```text
ADMIN A
   |
   +--> Disable ADMIN A
             |
             v
      Is another active ADMIN available?
             |
          NO |
             v
          REJECT
```

---

# 12. Identity and Privilege Protection

Administrative operations use the authenticated JWT identity.

The client does not provide the administrator identity in the request body.

For example, an administrator request is processed using:

```java
Authentication authentication
```

and:

```java
authentication.getName()
```

This prevents a client from claiming:

```text
"I am another administrator."
```

The security model is:

```text
JWT
 |
 v
Authenticated identity
 |
 v
Spring Security
 |
 v
ADMIN?
 |
 +---- NO ---> 403
 |
 YES
 |
 v
Administrative operation
 |
 v
Audit actor = authenticated identity
```

---

# 13. Password Security

Passwords are protected using BCrypt.

The application uses a `PasswordEncoder`.

Password processing:

```text
Plaintext password
       |
       v
PasswordEncoder
       |
       v
BCrypt hash
       |
       v
Database
```

The following security requirements are implemented:

* Passwords are hashed
* Plaintext passwords are not stored
* Password hashes are not returned
* Passwords are not written to audit logs
* Password length is validated
* Password changes generate a new hash

A password shorter than 8 characters is rejected.

Example:

```text
123
```

results in:

```text
400 Bad Request
```

---

# 14. DTO Security

The API does not directly expose the `User` entity for normal user responses.

Instead, the application uses:

```text
UserResponse
```

The response contains:

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

This follows the principle of **data minimization**.

Only data required by the client is exposed.

---

# 15. Validation and Error Handling

The API validates incoming requests.

Important validation cases include:

| Case                    | HTTP Status |
| ----------------------- | ----------: |
| Invalid email           |         400 |
| Empty email             |         400 |
| Invalid role            |         400 |
| Invalid status          |         400 |
| Password too short      |         400 |
| Empty password          |         400 |
| User not found          |         404 |
| Duplicate email         |         409 |
| Insufficient privileges |         403 |
| Invalid authentication  |         401 |

The application uses a centralized:

```text
GlobalExceptionHandler
```

This keeps API error responses consistent.

---

# 16. Audit Logging

Administrative operations generate security audit events.

Implemented events include:

```text
USER_CREATED
USER_UPDATED
ROLE_CHANGED
USER_DISABLED
USER_ENABLED
```

Authentication events are also logged, including:

```text
LOGIN_SUCCESS
LOGIN_FAILURE
```

Example audit record:

```text
Actor:
admin@example.com

Action:
ROLE_CHANGED

Resource:
USER

Resource ID:
6

Timestamp:
2026-09-19T19:59:04...

Details:
Changed role for user:
audit.test@example.com
to SECURITY_ANALYST
```

The audit record allows security personnel to answer:

```text
Who performed the action?
What action was performed?
Which resource was affected?
When did it happen?
What changed?
```

---

# 17. Audit Trail Protection

The audit API exposes read operations:

```text
GET /api/audit-logs
```

Access is restricted to:

```text
ADMIN
SECURITY_ANALYST
```

There are no application endpoints allowing users to:

```text
POST audit logs
PATCH audit logs
DELETE audit logs
```

Therefore, from the application API perspective, the audit trail is append-only.

Unauthorized write attempts were tested and rejected.

---

# 18. Audit Log Filtering

Audit logs support filtering by:

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

Combined filtering is also supported.

Example:

```text
GET /api/audit-logs?resourceType=USER&action=ROLE_CHANGED
```

This is useful for security investigations and administrative monitoring.

---

# 19. Security Testing

The following security tests were completed.

## Authentication

* Active user can log in
* Disabled user cannot log in
* Wrong password is rejected
* Unknown user is rejected
* Reactivated user can log in again

## Authorization

* ADMIN can manage users
* SECURITY_ANALYST cannot manage users
* MANAGER cannot manage users
* EMPLOYEE cannot manage users
* Unauthenticated requests cannot access administrative endpoints

## Privilege Escalation

Tested that non-admin users cannot:

* Change roles
* Promote themselves
* Disable another user
* Access administrative user-management APIs

## Input Validation

Tested:

* Duplicate email
* Invalid email
* Empty email
* Invalid role
* Invalid status
* Nonexistent user
* Short password
* Empty password

## Self-Protection

Tested:

* Last active ADMIN cannot be disabled
* User identity comes from JWT authentication
* Target user must exist
* Target role must be a supported role
* Target status must be a supported status

---

# 20. Threats Addressed

The implementation addresses several common application security threats.

## Privilege Escalation

Threat:

```text
EMPLOYEE
   |
   +--> Change own role to ADMIN
```

Protection:

```text
/api/admin/**
       |
       v
ADMIN only
```

---

## Account Takeover Through Disabled Accounts

Threat:

```text
Compromised account
       |
       v
Administrator disables account
       |
       v
Attacker continues logging in
```

Protection:

```text
DISABLED
   |
   v
Authentication rejected
```

---

## Password Exposure

Threat:

```text
Database
   |
   +--> Plaintext password
```

Protection:

```text
Password
   |
   v
BCrypt
   |
   v
Password hash
```

---

## Sensitive Data Exposure

Threat:

```text
API response
   |
   +--> passwordHash
```

Protection:

```text
UserResponse
   |
   +--> id
   +--> email
   +--> role
   +--> status
```

---

## Administrative Lockout

Threat:

```text
Only ADMIN
   |
   +--> Disable own account
   |
   v
No administrator remains
```

Protection:

```text
Count active ADMIN accounts
       |
       v
Last ADMIN?
       |
      YES
       |
       v
Reject operation
```

---

## Lack of Accountability

Threat:

```text
Admin changes user role
       |
       v
No record
```

Protection:

```text
Administrative action
       |
       v
AuditLog
       |
       +--> actor
       +--> action
       +--> target
       +--> timestamp
       +--> details
```

---

# 21. Future Improvements

Possible future improvements include:

1. Add a dedicated hard-delete workflow if required.
2. Add protection against an administrator changing their own role.
3. Prevent deletion of the last administrator if deletion is implemented.
4. Add password reset audit events such as `PASSWORD_RESET`.
5. Add pagination to the user-management API.
6. Add advanced audit-log filtering.
7. Add automated integration tests for all RBAC cases.
8. Add rate limiting to authentication endpoints.
9. Add account lockout or temporary login throttling.
10. Add frontend administrative dashboards.
11. Add centralized security monitoring through the SIEM.
12. Add immutable external audit storage for production environments.

---

# 22. Security Design Summary

The SecureOps Platform user-management security model follows:

```text
                 JWT Authentication
                         |
                         v
                 Identify User
                         |
                         v
                    RBAC Check
                         |
              +----------+----------+
              |                     |
             ADMIN                OTHER
              |                     |
              v                     v
       Admin Operation            403
              |
              v
       Validate Request
              |
              v
       Validate Target
              |
              v
       Apply Security Rules
              |
              v
       Perform Operation
              |
              v
          Audit Event
              |
              v
          PostgreSQL
```

The result is a centralized user-management system with authentication, authorization, password protection, account lifecycle management, validation, self-protection rules, and security auditing.
