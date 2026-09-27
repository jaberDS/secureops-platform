# DAY 06 — IAM HARDENING

## SecureOps Platform

---

# 1. Day 06 Objective

Day 06 focuses on **IAM Hardening**.

IAM means:

> **Identity and Access Management**

The goal is to make the application's user and authentication system more secure.

The main security questions are:

* Who is the user?
* Can the user authenticate?
* What role does the user have?
* Is the account active or disabled?
* Is the password strong enough?
* Can an administrator safely manage users?
* How are passwords reset?
* How are authentication secrets protected?
* What happens when authentication fails?

The Day 06 security flow is:

```text
                    USER
                      │
                      ▼
              Authentication
                      │
          ┌───────────┴───────────┐
          │                       │
       Email                   Password
          │                       │
          ▼                       ▼
   Normalization              Validation
          │                       │
          └───────────┬───────────┘
                      ▼
                BCrypt Hash
                      │
                      ▼
               Authentication
                      │
                      ▼
                    JWT
                      │
             ┌────────┴────────┐
             │                 │
          Expiration       Account Status
             │                 │
             └────────┬────────┘
                      ▼
                    RBAC
                      │
                      ▼
                API Access
```

---

# 2. IAM Architecture

SecureOps Platform uses several components to protect identities.

```text
Frontend
   │
   │ Login
   ▼
AuthController
   │
   ▼
AuthenticationManager
   │
   ▼
CustomUserDetailsService
   │
   ▼
UserRepository
   │
   ▼
PostgreSQL
```

After successful authentication:

```text
User
 │
 ▼
Authentication
 │
 ▼
JWT generated
 │
 ▼
Frontend stores JWT
 │
 ▼
Authorization: Bearer <JWT>
 │
 ▼
JwtAuthenticationFilter
 │
 ├── Validate signature
 ├── Validate expiration
 ├── Check account status
 └── Load user authorities
 │
 ▼
Spring Security
 │
 ▼
RBAC
 │
 ▼
Protected endpoint
```

---

# 3. User Identity

Each user has an identity stored in the database.

The `User` entity contains:

```text
id
email
passwordHash
role
status
```

Example:

```text
ID:       1
Email:    admin@example.com
Role:     ADMIN
Status:   ACTIVE
```

The password is never stored as plain text.

Instead:

```text
Plain Password
      │
      ▼
 BCrypt
      │
      ▼
Password Hash
      │
      ▼
Database
```

---

# 4. User Roles

SecureOps Platform uses four roles:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

These roles implement:

> **RBAC — Role-Based Access Control**

RBAC means that access is based on the user's role.

Example:

```text
ADMIN
  │
  ├── Manage users
  ├── Change roles
  ├── Enable/disable accounts
  └── Access administrative endpoints


SECURITY_ANALYST
  │
  ├── Security operations
  ├── Incident investigation
  └── Security logs


MANAGER
  │
  └── Manager-specific operations


EMPLOYEE
  │
  └── Basic application operations
```

The important principle is:

> Users should receive only the permissions required for their role.

This is called **Least Privilege**.

---

# 5. Email Normalization

Email addresses are normalized before authentication and storage.

For example:

```text
 JABER@EXAMPLE.COM
```

becomes:

```text
jaber@example.com
```

The `EmailNormalizationService` performs:

```text
Trim spaces
     │
     ▼
Convert to lowercase
     │
     ▼
Normalized email
```

This prevents problems such as:

```text
jaber@example.com

JABER@example.com

 jaber@example.com
```

being treated as different identities.

Email normalization is applied during:

* Registration
* Login
* User creation
* User update
* User lookup
* Password reset

---

# 6. Password Policy

Day 06 introduces a stronger password policy.

New passwords must:

* Have at least 12 characters
* Contain an uppercase letter
* Contain a lowercase letter
* Contain a number
* Contain a special character

Example:

```text
SecurePassword2026!
```

Example of a weak password:

```text
password
```

The password validation is implemented with Jakarta Validation.

The application validates passwords before storing or changing them.

---

# 7. Password Hashing

SecureOps Platform uses:

> **BCrypt**

BCrypt converts a password into a secure password hash.

Example:

```text
Password
   │
   ▼
BCrypt
   │
   ▼
$2a$10$...
```

The database stores:

```text
passwordHash
```

and never:

```text
password
```

During login:

```text
User enters password
        │
        ▼
BCrypt compares password
        │
        ▼
Stored hash
        │
        ▼
Match?
 ┌──────┴──────┐
 YES           NO
 │              │
 ▼              ▼
Login          Reject
```

---

# 8. Account Status

Users have an account status.

Currently:

```text
ACTIVE
DISABLED
```

The lifecycle is:

```text
ACTIVE
  │
  │ Administrator disables account
  ▼
DISABLED
```

And:

```text
DISABLED
  │
  │ Administrator enables account
  ▼
ACTIVE
```

---

# 9. Disabled User Login Prevention

A disabled user cannot authenticate.

During authentication, Spring Security loads the user.

The application checks:

```text
status == DISABLED
```

If the account is disabled:

```text
Authentication
      │
      ▼
Account status
      │
      ▼
DISABLED
      │
      ▼
Authentication rejected
```

This protects the application even if the user knows the correct password.

---

# 10. Disabled JWT Protection

Disabling an account must also invalidate access through existing JWT tokens.

Without this protection:

```text
User logs in
     │
     ▼
JWT generated
     │
     ▼
Administrator disables account
     │
     ▼
Old JWT still works
```

That would be a security problem.

SecureOps Platform checks the account status every time the JWT authentication filter processes a token.

The secure flow is:

```text
Existing JWT
     │
     ▼
JwtAuthenticationFilter
     │
     ▼
Load user
     │
     ▼
Check account status
     │
     ├── ACTIVE ──────► Continue
     │
     └── DISABLED ────► Reject authentication
```

Therefore, disabling an account also prevents the disabled user from continuing to use an existing JWT.

---

# 11. Last Active Administrator Protection

A dangerous situation would be:

```text
Only one ADMIN exists
        │
        ▼
ADMIN becomes DISABLED
        │
        ▼
No administrator remains
```

SecureOps Platform prevents this.

Before disabling an administrator, the application counts:

```text
ACTIVE + ADMIN
```

If only one active administrator exists:

```text
Cannot disable the last active ADMIN
```

The same protection exists for role changes.

For example:

```text
ADMIN A
ADMIN B
```

If ADMIN A is changed to EMPLOYEE:

```text
ADMIN B
```

still remains.

But if only one administrator exists:

```text
ADMIN A
```

the application prevents:

```text
ADMIN → EMPLOYEE
```

because that would leave the system without an active administrator.

---

# 12. User Creation

Administrators can create users.

The request contains:

```text
email
password
role
```

Example:

```json
{
    "email": "analyst@example.com",
    "password": "SecurePassword2026!",
    "role": "SECURITY_ANALYST"
}
```

The application then:

```text
Validate request
      │
      ▼
Normalize email
      │
      ▼
Check duplicate email
      │
      ▼
Validate password
      │
      ▼
Hash password with BCrypt
      │
      ▼
Assign role
      │
      ▼
Set ACTIVE status
      │
      ▼
Save user
      │
      ▼
Audit log
```

---

# 13. User Update

Administrators can update user information.

The update can contain:

```text
email
password
```

Email is normalized.

Password is hashed using BCrypt.

The application also checks that the new email does not already belong to another user.

---

# 14. Role Management

Administrators can change user roles.

Example:

```text
EMPLOYEE
    │
    ▼
SECURITY_ANALYST
```

or:

```text
MANAGER
    │
    ▼
EMPLOYEE
```

Before changing an ADMIN role, the application checks the number of active administrators.

This prevents accidental loss of administrative access.

---

# 15. Account Enable / Disable

The administrator can change the status:

```text
ACTIVE
```

or:

```text
DISABLED
```

Example:

```http
PATCH /api/admin/users/{userId}/status
```

Request:

```json
{
    "status": "DISABLED"
}
```

The action is also recorded in the audit system.

Example events:

```text
USER_DISABLED
USER_ENABLED
```

---

# 16. Password Reset

Day 06 introduces a secure password reset mechanism.

The flow is:

```text
User requests password reset
          │
          ▼
Application finds account
          │
          ▼
Generate random token
          │
          ▼
Hash token with SHA-256
          │
          ▼
Store token hash in database
          │
          ▼
Token expires after 15 minutes
```

The important security principle is:

> The raw reset token is not stored in the database.

The database stores:

```text
SHA-256(token)
```

instead of:

```text
raw token
```

---

# 17. Password Reset Token Security

Reset tokens are generated using:

```text
SecureRandom
```

The token contains cryptographically random bytes.

The application also stores:

```text
createdAt
expiresAt
usedAt
```

This allows the application to verify:

```text
Is token valid?
Is token expired?
Was token already used?
Is the account still active?
```

---

# 18. One-Time Password Reset Token

A reset token can only be used once.

Before reset:

```text
usedAt = null
```

After reset:

```text
usedAt = current time
```

If someone tries to reuse the token:

```text
Invalid or expired password reset token
```

This prevents replay of an old reset token.

---

# 19. Password Reset Expiration

The default reset-token lifetime is:

```text
15 minutes
```

The configuration is:

```properties
security.password-reset.expiration-minutes=${SECUREOPS_PASSWORD_RESET_EXPIRATION_MINUTES:15}
```

The value can be changed through an environment variable.

---

# 20. Local Development Reset Token

For the local cybersecurity lab, the reset token can temporarily be exposed in the API response.

The configuration is:

```text
SECUREOPS_PASSWORD_RESET_EXPOSE_TOKEN=true
```

This is useful for testing because there is currently no email service connected.

In production:

```text
SECUREOPS_PASSWORD_RESET_EXPOSE_TOKEN=false
```

The production design should send the reset link through a secure email service instead.

---

# 21. Authentication Error Handling

Authentication errors should not reveal too much information.

For example, the application should not tell an attacker:

```text
Email does not exist
```

or:

```text
Password is incorrect
```

Instead, login failures return:

```text
Invalid email or password
```

This reduces:

> **Account Enumeration**

Account enumeration means an attacker can discover which accounts exist by testing login responses.

The secure approach is:

```text
Wrong email
     │
     ▼
Invalid email or password


Wrong password
     │
     ▼
Invalid email or password
```

The external response is intentionally similar.

---

# 22. JWT Security

SecureOps Platform uses JWT for authentication.

JWT means:

> **JSON Web Token**

After successful login:

```text
Email + Password
      │
      ▼
Authentication
      │
      ▼
JWT
```

The token contains information such as the authenticated user's identity and expiration.

---

# 23. JWT Expiration

JWTs must not live forever.

The application uses:

```properties
security.jwt.expiration-ms=${SECUREOPS_JWT_EXPIRATION_MS:3600000}
```

The default value is:

```text
3,600,000 milliseconds
```

which equals:

```text
1 hour
```

The flow is:

```text
JWT created
    │
    ▼
1 hour
    │
    ▼
JWT expires
    │
    ▼
Authentication rejected
```

---

# 24. JWT Secret Management

The JWT signing secret is not hardcoded in Java.

Instead:

```properties
security.jwt.secret=${SECUREOPS_JWT_SECRET}
```

The secret comes from an environment variable:

```text
SECUREOPS_JWT_SECRET
```

This is safer than:

```java
String secret = "my-secret";
```

because source code can be committed to GitHub.

The secure architecture is:

```text
Environment Variable
        │
        ▼
Spring Configuration
        │
        ▼
JwtService
        │
        ▼
JWT signing
```

---

# 25. Database Password Management

The PostgreSQL password is also externalized.

Instead of:

```properties
spring.datasource.password=actual-password
```

the application uses:

```properties
spring.datasource.password=${SECUREOPS_DB_PASSWORD}
```

The password is provided through the environment.

This prevents database credentials from being directly stored in source code.

---

# 26. Secrets and Git

The following must never be committed to GitHub:

```text
SECUREOPS_JWT_SECRET
SECUREOPS_DB_PASSWORD
```

Also avoid committing:

```text
.env
production passwords
API keys
private keys
database credentials
```

The general rule is:

> Configuration can be committed. Secrets must be externalized.

---

# 27. Security Configuration

Protected endpoints use Spring Security.

For example:

```text
/api/admin/**
```

requires:

```text
ADMIN
```

Other endpoints use the required roles.

The security model is:

```text
Request
   │
   ▼
JWT Filter
   │
   ▼
Authentication
   │
   ▼
Role
   │
   ▼
Authorization
   │
   ├── Allowed
   │
   └── 403 Forbidden
```

---

# 28. Validation

Day 06 also strengthens request validation.

Examples:

```text
@NotBlank
@Email
@Size
@Pattern
@NotNull
@Valid
```

Validation occurs before business logic.

Example:

```text
HTTP Request
     │
     ▼
Validation
     │
 ┌───┴────┐
 │        │
Valid   Invalid
 │        │
 ▼        ▼
Service  400
```

This protects the application against invalid input.

---

# 29. Global Error Handling

The application uses:

```text
GlobalExceptionHandler
```

to provide consistent API errors.

Validation errors return:

```json
{
    "status": 400,
    "message": "Validation failed",
    "errors": {
        "password": "Password must contain..."
    }
}
```

Invalid request bodies return HTTP:

```text
400 Bad Request
```

Missing users return:

```text
404 Not Found
```

Business conflicts such as duplicate email or last-admin protection return:

```text
409 Conflict
```

---

# 30. Audit Logging

Important IAM actions are recorded.

Examples:

```text
LOGIN_SUCCESS
LOGIN_FAILURE
USER_CREATED
USER_UPDATED
ROLE_CHANGED
USER_DISABLED
USER_ENABLED
PASSWORD_RESET_REQUESTED
PASSWORD_RESET_SUCCESS
```

This creates an audit trail.

Example:

```text
Administrator
      │
      ▼
Disables user
      │
      ▼
USER_DISABLED
      │
      ▼
Audit Log
```

This is important for:

* Security monitoring
* Incident investigation
* Accountability
* Compliance
* SOC/SIEM integration

---

# 31. Day 06 Defense in Depth

Day 06 does not depend on one security control.

Instead, multiple controls work together:

```text
                    IAM SECURITY
                         │
       ┌─────────────────┼─────────────────┐
       │                 │                 │
 Password Policy     Email Normalization   RBAC
       │                 │                 │
       ▼                 ▼                 ▼
    BCrypt          Identity Control    Authorization
       │                                   │
       └──────────────┬────────────────────┘
                      ▼
                 Authentication
                      │
                      ▼
                     JWT
                      │
             ┌────────┴────────┐
             │                 │
         Expiration       Account Status
             │                 │
             └────────┬────────┘
                      ▼
                 API Security
                      │
                      ▼
                 Audit Logging
```

This is called:

> **Defense in Depth**

If one security control fails, other controls can still provide protection.

---

# 32. Day 06 Files Added / Modified

Important files created or modified during Day 06 include:

```text
backend/
└── src/main/java/com/secureops/backend/
    ├── controller/
    │   ├── AuthController.java
    │   └── UserController.java
    │
    ├── dto/
    │   ├── CreateUserRequest.java
    │   ├── LoginRequest.java
    │   ├── PasswordResetConfirmRequest.java
    │   ├── PasswordResetRequest.java
    │   ├── RegisterRequest.java
    │   ├── UpdateUserRequest.java
    │   ├── UpdateUserRoleRequest.java
    │   └── UpdateUserStatusRequest.java
    │
    ├── entity/
    │   ├── PasswordResetToken.java
    │   └── User.java
    │
    ├── repository/
    │   ├── PasswordResetTokenRepository.java
    │   └── UserRepository.java
    │
    ├── security/
    │   ├── JwtAuthenticationFilter.java
    │   ├── PasswordConfig.java
    │   └── SecurityConfig.java
    │
    └── service/
        ├── CustomUserDetailsService.java
        ├── EmailNormalizationService.java
        ├── JwtService.java
        ├── PasswordResetService.java
        └── UserService.java
```

Documentation:

```text
docs/
└── security/
    └── iam.md
```

Configuration:

```text
backend/
└── src/main/resources/
    └── application.properties
```

---

# 33. Environment Variables

Day 06 uses environment variables for sensitive configuration.

```text
SECUREOPS_JWT_SECRET
SECUREOPS_DB_PASSWORD
SECUREOPS_PASSWORD_RESET_EXPOSE_TOKEN
```

Optional configuration:

```text
SECUREOPS_JWT_EXPIRATION_MS
SECUREOPS_PASSWORD_RESET_EXPIRATION_MINUTES
```

Example architecture:

```text
PowerShell
    │
    ├── SECUREOPS_JWT_SECRET
    ├── SECUREOPS_DB_PASSWORD
    └── configuration variables
             │
             ▼
       Spring Boot
             │
       ┌─────┴─────┐
       ▼           ▼
    JwtService   PostgreSQL
```

---

# 34. Testing

Day 06 ended with the complete Maven test suite.

Command:

```powershell
.\mvnw.cmd clean test
```

Final result:

```text
Tests run: 3
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

The tests included:

```text
BackendApplicationTests
PasswordEncoderTest
PasswordHashGeneratorTest
```

Therefore:

```text
3 tests
0 failures
0 errors
```

---

# 35. PostgreSQL Verification

During Day 06 troubleshooting, the PostgreSQL Docker container was identified as:

```text
secureops-postgres
```

The database configuration is:

```text
Database: secureops
User:     secureops
Port:     5432
```

The database connection was successfully verified from inside Docker.

Spring Boot subsequently connected successfully:

```text
HikariPool - Added connection
```

Hibernate also successfully detected:

```text
Database dialect: PostgreSQLDialect
Database version: 17.11
```

This confirmed that the backend can successfully communicate with PostgreSQL.

---

# 36. Day 06 Final Checklist

## IAM

* [x] User creation
* [x] User update
* [x] Role management
* [x] Account enable/disable
* [x] Disabled-user login prevention
* [x] Disabled-user JWT prevention
* [x] Last active ADMIN protection

## Password Security

* [x] BCrypt hashing
* [x] Strong password policy
* [x] Password validation
* [x] Password reset
* [x] Secure reset token generation
* [x] Reset token hashing
* [x] Reset token expiration
* [x] One-time reset token

## Authentication

* [x] Email normalization
* [x] Generic authentication errors
* [x] JWT authentication
* [x] JWT expiration
* [x] JWT secret externalization

## Authorization

* [x] RBAC
* [x] Role-based endpoint protection
* [x] Least privilege principle

## Configuration Security

* [x] Database password externalization
* [x] JWT secret externalization
* [x] No hardcoded production secrets
* [x] Local reset-token testing configuration

## Logging

* [x] Login success logging
* [x] Login failure logging
* [x] User creation logging
* [x] User update logging
* [x] Role change logging
* [x] Account status logging
* [x] Password reset logging

## Validation

* [x] Request validation
* [x] Email validation
* [x] Password validation
* [x] Role validation
* [x] Status validation
* [x] Global exception handling

## Testing

* [x] Application context test
* [x] Password encoder test
* [x] Password hash test
* [x] PostgreSQL connection test
* [x] Full Maven test suite
* [x] `BUILD SUCCESS`

---

# 37. What I Learned in Day 06

The most important concepts to remember are:

### 1. IAM

```text
Who are you?
What can you do?
Is your account active?
```

### 2. Authentication

```text
Email + Password
       ↓
Identity verification
```

### 3. Authorization

```text
Authenticated user
       ↓
Role
       ↓
Permission
```

### 4. RBAC

```text
User
 ↓
Role
 ↓
Permissions
```

### 5. Password Hashing

```text
Password
   ↓
BCrypt
   ↓
Hash
   ↓
Database
```

### 6. JWT

```text
Login
 ↓
JWT
 ↓
Request
 ↓
JWT Filter
 ↓
Authentication
```

### 7. Account Lifecycle

```text
ACTIVE
  ↕
DISABLED
```

### 8. Secret Management

```text
Secret
  ↓
Environment Variable
  ↓
Application
```

not:

```text
Secret
  ↓
Java source code
  ↓
GitHub
```

### 9. Defense in Depth

```text
Password Policy
       ↓
BCrypt
       ↓
Authentication
       ↓
JWT
       ↓
Expiration
       ↓
Account Status
       ↓
RBAC
       ↓
Audit Logs
```

---

# 38. Day 06 Mental Model

If you remember only one thing from Day 06, remember this:

```text
              SECUREOPS USER
                    │
                    ▼
             ┌─────────────┐
             │    EMAIL    │
             │ NORMALIZE   │
             └──────┬──────┘
                    │
                    ▼
             ┌─────────────┐
             │  PASSWORD   │
             │   POLICY    │
             └──────┬──────┘
                    │
                    ▼
                BCrypt
                    │
                    ▼
             Authentication
                    │
                    ▼
                  JWT
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
      EXPIRATION         ACCOUNT STATUS
          │                   │
          └─────────┬─────────┘
                    ▼
                   RBAC
                    │
                    ▼
             API AUTHORIZATION
                    │
                    ▼
              AUDIT LOGGING
```

**Day 06 = Make identity and authentication secure.**

---

# 39. Day 06 Completion

```text
DAY 06
IAM HARDENING
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Implementation       ████████████████████ 100%
Security             ████████████████████ 100%
Documentation        ████████████████████ 100%
Testing              ████████████████████ 100%

STATUS: COMPLETE ✅
```

The backend security foundation is now ready for the next stage:

```text
DAY 01 → DevSecOps Foundation
DAY 02 → Application Development
DAY 03 → Security Foundation
DAY 04 → Incident + Audit Logging
DAY 05 → User + Role Management
DAY 06 → IAM Hardening          ✅
DAY 07 → Angular Security Dashboard
```

# 40. Transition to Day 07

Day 07 changes the focus from mainly backend security to the **frontend security dashboard**.

The architecture becomes:

```text
                 Angular
                   │
                   │ HTTP + JWT
                   ▼
             Spring Boot API
                   │
                   ▼
              Spring Security
                   │
                   ▼
               PostgreSQL
```

Day 07 will focus on:

```text
Angular project
     ↓
Login page
     ↓
AuthService
     ↓
JWT storage
     ↓
HTTP interceptor
     ↓
Route guards
     ↓
Role-based UI
     ↓
Security dashboard
```

So the key transition is:

> **Day 06 secured the backend IAM system. Day 07 connects Angular to that security system.**
