# Day 03 — Application Security Foundation

## 1. Introduction

During Day 3, the SecureOps Platform security foundation was implemented and tested.

The main objective was to protect the backend API using:

* Input validation
* Spring Security
* BCrypt password hashing
* Authentication
* JWT authentication
* Role-Based Access Control (RBAC)
* Security testing
* PostgreSQL persistence

The security architecture follows this principle:

```text
Build security into the application
             ↓
Authenticate users
             ↓
Authorize their actions
             ↓
Validate all input
             ↓
Protect sensitive information
             ↓
Test security controls
```

---

# 2. Authentication Architecture

SecureOps Platform uses a token-based authentication architecture based on:

* Spring Security
* BCrypt password hashing
* JWT (JSON Web Token)
* PostgreSQL
* Role-Based Access Control (RBAC)

The authentication process contains two main stages:

1. User registration
2. User login and authenticated requests

---

## 2.1 Registration Flow

When a new user registers, the backend follows this process:

```text
Client
   |
   | POST /api/auth/register
   v
RegisterRequest
   |
   | Input validation
   v
UserService
   |
   | BCrypt password hashing
   v
PostgreSQL
   |
   | User stored
   v
Registration successful
```

The password is never stored as plaintext.

Instead, BCrypt transforms the password into a password hash before it is stored in PostgreSQL.

---

## 2.2 Registration Security Controls

The registration endpoint validates:

* Email must not be empty
* Email must have a valid format
* Email has a maximum length
* Password must not be empty
* Password must contain at least 8 characters
* Password has a maximum length
* Email must be unique

If the email already exists, the API returns:

```text
409 Conflict
```

If the input is invalid, the API returns:

```text
400 Bad Request
```

If registration succeeds:

```text
201 Created
```

---

# 3. Login Flow

When a user logs in:

```text
Client
   |
   | POST /api/auth/login
   v
LoginRequest
   |
   v
AuthenticationManager
   |
   v
CustomUserDetailsService
   |
   | Find user by email
   v
PostgreSQL
   |
   | Retrieve password hash + role
   v
BCrypt verification
   |
   | Password correct
   v
JwtService
   |
   | Generate JWT
   v
Client receives JWT
```

If the password is incorrect, authentication fails.

If the email does not exist, authentication also fails.

The application does not reveal whether a specific account exists through a successful authentication response.

---

# 4. Password Hashing with BCrypt

SecureOps does not store user passwords directly in the database.

Instead, passwords are processed using BCrypt.

## 4.1 Password Storage

The process is:

```text
User password
      |
      v
BCryptPasswordEncoder
      |
      v
Password hash
      |
      v
PostgreSQL
```

The database therefore contains a password hash rather than the original password.

---

## 4.2 Password Verification

During login:

```text
User enters password
        |
        v
Spring Security
        |
        v
BCrypt verification
        |
     +--+--+
     |     |
  Match   No match
     |     |
     v     v
  Login   Reject
```

The application does not decrypt the password hash.

Instead, BCrypt checks whether the supplied password matches the stored hash.

---

## 4.3 Why BCrypt?

BCrypt is designed specifically for password hashing.

It includes a salt as part of the generated password hash.

A salt is additional random data used during hashing. It helps prevent attackers from efficiently using precomputed password tables against many accounts.

The important security principle is:

```text
Plaintext password in database
             ↓
            ❌

Password hash in database
             ↓
            ✅
```

---

## 4.4 BCrypt Security Test

The BCrypt implementation was tested automatically using Maven.

The tests verified:

* A generated hash is different from the original password.
* The correct password successfully matches the hash.
* An incorrect password does not match the hash.

The Maven test suite completed successfully with:

```text
Tests run: 2
Failures: 0
Errors: 0
```

---

# 5. JWT Authentication

JWT means:

**JSON Web Token**

SecureOps uses JWT to maintain authentication between the client and backend API.

After successful login, the backend generates a JWT.

The client then sends the token with requests to protected endpoints.

---

## 5.1 JWT Flow

```text
Login
  |
  v
Verify credentials
  |
  v
Generate JWT
  |
  v
Client
  |
  | Authorization: Bearer <JWT>
  v
Backend
  |
  v
JWT Authentication Filter
  |
  v
Validate JWT
  |
  v
Load user
  |
  v
Spring Security
  |
  v
Protected endpoint
```

---

## 5.2 JWT Structure

A JWT contains three logical parts:

```text
Header.Payload.Signature
```

The current SecureOps JWT contains information such as:

* Subject
* Issued-at time
* Expiration time
* Cryptographic signature

The subject identifies the authenticated user's email.

The token currently has a one-hour expiration time.

---

## 5.3 JWT Authentication Filter

The `JwtAuthenticationFilter` is responsible for processing incoming requests containing a JWT.

The filter:

1. Reads the `Authorization` HTTP header.
2. Checks whether it starts with `Bearer`.
3. Extracts the token.
4. Extracts the user's email from the token.
5. Loads the user from PostgreSQL.
6. Validates the token.
7. Creates a Spring Security authentication object.
8. Places the authentication into the Security Context.
9. Allows the request to continue.

Simplified architecture:

```text
HTTP Request
     |
     v
Authorization Header
     |
     v
Bearer JWT
     |
     v
JwtAuthenticationFilter
     |
     v
JwtService
     |
     v
Token validation
     |
     v
User lookup
     |
     v
SecurityContext
     |
     v
Authorization
```

---

## 5.4 JWT Security Principle

The application does not trust the frontend to decide whether a user is authenticated.

The backend validates the JWT.

The backend also reloads the user from the database and obtains the user's current role.

This means authorization decisions are performed server-side.

---

## 5.5 JWT Secret

The JWT is cryptographically signed using a secret key.

The current implementation contains a development secret directly in the source code.

This is acceptable only for the current learning environment.

For production, the secret must be moved to a secure configuration mechanism such as:

* Environment variables
* Docker secrets
* CI/CD secrets
* A dedicated secrets manager

A real production secret must never be committed to GitHub.

---

# 6. Authentication vs Authorization

Authentication and authorization are different security concepts.

## Authentication

Authentication answers:

> Who are you?

Example:

```text
Email + Password
       ↓
Authentication
       ↓
JWT
```

---

## Authorization

Authorization answers:

> What are you allowed to do?

Example:

```text
Authenticated user
       ↓
Role
       ↓
Permission
       ↓
Allow / Deny
```

The two concepts work together:

```text
Authentication
      ↓
Who is the user?
      ↓
Authorization
      ↓
What can the user access?
```

---

# 7. Role-Based Access Control

SecureOps uses:

**RBAC — Role-Based Access Control**

RBAC means that permissions are associated with roles rather than individual users.

The current roles are:

| Role             | Purpose                     |
| ---------------- | --------------------------- |
| ADMIN            | Administrative operations   |
| SECURITY_ANALYST | Security operations         |
| MANAGER          | Management operations       |
| EMPLOYEE         | Normal authenticated access |

---

## 7.1 Role Architecture

```text
                    User
                      |
                      v
                     Role
                      |
       +--------------+--------------+
       |              |              |
      ADMIN       SECURITY_ANALYST  MANAGER
       |              |              |
       v              v              v
 Admin APIs      Security APIs    Manager APIs

                      |
                      v
                  EMPLOYEE
                      |
                      v
             Authenticated APIs
```

---

## 7.2 Endpoint Protection

Spring Security protects API areas according to the user's role.

```text
/api/admin/**
        ↓
     ADMIN

/api/security/**
        ↓
 SECURITY_ANALYST

/api/manager/**
        ↓
     MANAGER
```

Other protected endpoints require authentication.

---

## 7.3 RBAC Testing

The authorization system was tested using different accounts.

### ADMIN

An ADMIN user successfully accessed:

```text
/api/admin/test
```

Result:

```text
ADMIN access granted
```

---

### SECURITY_ANALYST

A SECURITY_ANALYST user successfully accessed:

```text
/api/security/test
```

Result:

```text
SECURITY_ANALYST access granted
```

The same user was denied access to the ADMIN endpoint.

Result:

```text
403 Forbidden
```

---

### MANAGER

A MANAGER user successfully accessed:

```text
/api/manager/test
```

Result:

```text
MANAGER access granted
```

The MANAGER was denied access to the ADMIN endpoint.

Result:

```text
403 Forbidden
```

---

### EMPLOYEE

An EMPLOYEE user successfully accessed:

```text
/api/employee/test
```

Result:

```text
Authenticated user access granted
```

---

# 8. Security Testing

Security was tested using positive and negative test cases.

A positive test verifies that a legitimate action works.

A negative test verifies that an invalid or unauthorized action is rejected.

---

## 8.1 Authentication Failure

A login attempt using an incorrect password was tested.

Result:

```text
403 Forbidden
```

This confirms that incorrect credentials do not provide access.

---

## 8.2 Unknown User

A login attempt using an unknown email was tested.

Result:

```text
403 Forbidden
```

The user was not authenticated.

---

## 8.3 Authorization Failure

A MANAGER attempted to access an ADMIN endpoint.

Result:

```text
403 Forbidden
```

This confirms that authentication alone is not enough to access role-restricted resources.

---

## 8.4 Invalid Input

An invalid registration request was tested with:

* Empty email
* Short password

Result:

```text
400 Bad Request
```

The backend validation system rejected the request.

---

## 8.5 Duplicate Account

An attempt was made to register an email that already existed.

Result:

```text
409 Conflict
```

This prevents duplicate user accounts.

---

## 8.6 Password Security

The PostgreSQL database was inspected.

The database contains a BCrypt password hash instead of the original password.

Result:

```text
Plaintext password
       ↓
      ❌

BCrypt hash
       ↓
      ✅
```

---

## 8.7 Protected Endpoint Without JWT

The following endpoint was requested without an authentication token:

```text
GET /api/employee/test
```

Result:

```text
403 Forbidden
```

This confirms that protected endpoints cannot be accessed anonymously.

---

## 8.8 Protected Endpoint With JWT

The same endpoint was requested with a valid JWT:

```text
GET /api/employee/test
Authorization: Bearer <JWT>
```

Result:

```text
Authenticated user access granted
```

This confirms that a valid JWT allows an authenticated user to access protected resources.

---

# 9. Sensitive Data Exposure Review

The `User` entity contains sensitive information:

```text
User
├── id
├── email
├── passwordHash
└── role
```

The `passwordHash` field must never be exposed through a public API response.

At the current stage, the application does not have a public `UserController`.

Therefore, there is currently no `/api/users` endpoint returning complete User entities to clients.

The authentication endpoints return only the information required for the authentication workflow:

```text
Registration
    ↓
Success message

Login
    ↓
JWT
```

The password hash is not returned to the client.

---

## 9.1 Future Improvement

The application should eventually use dedicated DTOs for API responses.

Instead of returning database entities directly:

```text
Database Entity
      ↓
      API
```

the preferred architecture is:

```text
Database Entity
      ↓
Service
      ↓
Response DTO
      ↓
API
```

This provides better control over which fields are exposed.

---

# 10. Log Security Review

Application and PostgreSQL logs were reviewed for sensitive information.

The PostgreSQL logs contained operational information such as:

```text
database system is ready to accept connections
checkpoint starting
checkpoint complete
database system is shut down
```

The reviewed logs did not contain:

* Plaintext passwords
* Password hashes
* JWT tokens
* JWT secrets
* Authentication credentials

This demonstrates that the current PostgreSQL logging output does not expose the tested authentication secrets.

---

## 10.1 Logging Security Principle

Logs are useful for:

* Debugging
* Monitoring
* Incident investigation
* Security analysis

However, logs must not contain secrets.

The following information should never be logged:

```text
Passwords
JWT tokens
Password hashes
Private keys
Database credentials
API secrets
```

---

# 11. Global Exception Handling

SecureOps uses a global exception handler to provide structured validation errors.

The handler processes:

```text
MethodArgumentNotValidException
```

and returns a structured response.

Example:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid",
    "password": "Password must be between 8 and 100 characters"
  }
}
```

This provides a consistent API error structure.

It also prevents the backend from returning uncontrolled validation information.

---

# 12. Security Architecture Overview

The complete security architecture can be represented as:

```text
                         CLIENT
                           |
                           |
                +----------+----------+
                |                     |
             Register                Login
                |                     |
                v                     v
        RegisterRequest          LoginRequest
                |                     |
                v                     v
           Validation          AuthenticationManager
                |                     |
                v                     v
           UserService        CustomUserDetailsService
                |                     |
                |                     v
                |                PostgreSQL
                |                     |
                v                     v
             BCrypt              BCrypt verify
                |                     |
                v                     v
           PostgreSQL             JwtService
                                      |
                                      v
                                     JWT
                                      |
                                      v
                                   CLIENT
                                      |
                                      |
                         Authorization Header
                                      |
                                      v
                           JwtAuthenticationFilter
                                      |
                                      v
                                Validate JWT
                                      |
                                      v
                           Load user from DB
                                      |
                                      v
                              Spring Security
                                      |
                                      v
                                    RBAC
                                      |
                   +------------------+------------------+
                   |                  |                  |
                 ADMIN            ANALYST             MANAGER
                   |                  |                  |
                   v                  v                  v
              Admin APIs        Security APIs       Manager APIs
                                      |
                                      v
                                  EMPLOYEE
                                      |
                                      v
                              Protected APIs
```

---

# 13. Security Principles Applied

The following security principles were applied during Day 3.

## 13.1 Never Trust User Input

All external input must be considered untrusted.

The backend validates incoming data before processing it.

---

## 13.2 Defense in Depth

Security is implemented at multiple layers:

```text
Input validation
      +
Password hashing
      +
Authentication
      +
JWT validation
      +
Authorization
      +
Database
      +
Security testing
```

This is called **defense in depth**.

It means that the application does not depend on only one security control.

---

## 13.3 Least Privilege

Users should receive only the permissions required for their role.

For example:

```text
MANAGER
   ↓
Manager permissions

not

ADMIN permissions
```

---

## 13.4 Server-Side Security

The frontend must never be considered the final security boundary.

The backend verifies:

* Authentication
* JWT validity
* User identity
* User role
* Authorization

Therefore:

```text
Frontend security
       ≠
Backend security
```

The backend must enforce the real security rules.

---

# 14. Day 3 Security Test Summary

| Test                           | Result |
| ------------------------------ | ------ |
| Successful registration        | PASS   |
| Duplicate registration         | PASS   |
| Invalid registration           | PASS   |
| Password hashing               | PASS   |
| Successful login               | PASS   |
| Wrong password                 | PASS   |
| Unknown user                   | PASS   |
| ADMIN authorization            | PASS   |
| SECURITY_ANALYST authorization | PASS   |
| MANAGER authorization          | PASS   |
| EMPLOYEE authentication        | PASS   |
| Unauthorized role access       | PASS   |
| Protected endpoint without JWT | PASS   |
| Protected endpoint with JWT    | PASS   |
| Sensitive data review          | PASS   |
| PostgreSQL log review          | PASS   |

---

# 15. Day 3 Result

Day 3 established the first complete security foundation for the SecureOps Platform.

The application now has:

```text
                 SECUREOPS PLATFORM
                        |
          +-------------+-------------+
          |             |             |
       Validation   Authentication Authorization
          |             |             |
          |          BCrypt + JWT      RBAC
          |             |             |
          +-------------+-------------+
                        |
                        v
                   PostgreSQL
```

The backend is no longer simply an application that accepts requests.

It now verifies:

```text
Is the input valid?
        ↓
Is the user authenticated?
        ↓
Is the JWT valid?
        ↓
What role does the user have?
        ↓
Is this role allowed to access this resource?
        ↓
ALLOW or DENY
```

This provides the foundation for the next security layers of the project.

---

# 16. Known Security Improvements

The current implementation is designed for a learning and development environment.

Before production deployment, the following improvements are required:

1. Move the JWT secret outside the source code.
2. Use environment variables or a secrets manager.
3. Improve authentication failure responses with consistent HTTP status handling.
4. Normalize email addresses.
5. Handle database uniqueness race conditions.
6. Use DTOs instead of exposing JPA entities directly.
7. Improve structured API responses.
8. Add centralized security logging.
9. Add rate limiting / brute-force protection.
10. Add HTTPS in deployed environments.
11. Add refresh-token strategy if long-lived sessions are required.
12. Add automated security tests to the CI/CD pipeline.
13. Add secret scanning with Gitleaks.
14. Add dependency scanning.
15. Add SAST and DAST security gates.

These improvements will be progressively implemented during the DevSecOps phases of the project.

---

# 17. Conclusion

Day 3 focused on building the security foundation of SecureOps Platform.

The main security mechanisms implemented were:

```text
Input Validation
       ↓
BCrypt
       ↓
Spring Security
       ↓
AuthenticationManager
       ↓
JWT
       ↓
JWT Authentication Filter
       ↓
RBAC
       ↓
Protected API
```

The authentication and authorization mechanisms were tested using both successful and failed scenarios.

The tests confirmed that:

* Invalid input is rejected.
* Passwords are hashed.
* Valid users can authenticate.
* Invalid credentials are rejected.
* JWTs protect authenticated requests.
* Users cannot access role-restricted endpoints without the required role.
* Sensitive authentication information is not returned by the current authentication APIs.
* Reviewed PostgreSQL logs do not expose authentication secrets.

Therefore, the SecureOps backend now has a functional security foundation that can be extended with additional DevSecOps security controls.
