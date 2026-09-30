# Day 09 — Security Testing Automation

## 1. Introduction

Day 09 is about testing the security of the SecureOps Platform.

The goal is to make sure that:

* users can log in correctly;
* wrong passwords are rejected;
* disabled users cannot log in;
* JWT authentication works correctly;
* protected endpoints are protected;
* users can only access the resources allowed by their role;
* unauthorized users are rejected;
* incident security rules work correctly;
* investigation-note security rules work correctly;
* all security tests can run automatically.

The tests are executed with Maven.

---

# 2. Security Testing Goal

The main question for Day 09 is:

> **Can the application correctly allow authorized users and block unauthorized users?**

We test both situations.

### Authorized access

A user has the correct role and should be allowed to access the endpoint.

Example:

```text
ADMIN → /api/admin/users → ALLOWED
```

### Unauthorized access

A user does not have the required role and should be blocked.

Example:

```text
EMPLOYEE → /api/admin/users → FORBIDDEN
```

This is important because a secure application must not only authenticate users.

It must also control what each user can do.

---

# 3. Security Technologies

The project uses:

* Spring Boot
* Spring Security
* JWT
* BCrypt password hashing
* Role-Based Access Control (RBAC)
* JUnit
* MockMvc
* Maven

### Spring Security

Spring Security protects the application's endpoints.

### JWT

JWT is used to authenticate users after login.

### RBAC

RBAC means:

> **Role-Based Access Control**

It means that access is based on the user's role.

The main roles are:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

### JUnit

JUnit is used to write automated tests.

### MockMvc

MockMvc allows us to test HTTP endpoints without manually using a browser.

### Maven

Maven compiles the project and runs the automated tests.

---

# 4. Security Test Architecture

The security flow is:

```text
User
  |
  v
Login
  |
  v
Username + Password
  |
  v
Authentication
  |
  v
JWT Token
  |
  v
Request with JWT
  |
  v
JWT Filter
  |
  v
Spring Security
  |
  v
Check User Role
  |
  v
Allow or Reject Request
```

If the user has the correct permission:

```text
Request → ALLOWED
```

If the user does not have the correct permission:

```text
Request → FORBIDDEN
```

---

# 5. Test Files

The Day 09 security tests are located in:

```text
backend/src/test/java/com/secureops/backend/security/
```

The test classes are:

```text
AuthenticationSecurityTest.java
AuthenticationNegativeSecurityTest.java
AuthorizationSecurityTest.java
DisabledJwtSecurityTest.java
IncidentSecurityTest.java
InvestigationNoteSecurityTest.java
JwtSecurityTest.java
PasswordEncoderTest.java
PasswordHashGeneratorTest.java
RbacSecurityTest.java
```

These tests cover different parts of application security.

---

# 6. Authentication Testing

Authentication means verifying the identity of a user.

The application uses:

```text
Email
+
Password
```

for login.

---

## 6.1 Successful Login

We test that a user with correct credentials can log in.

Example:

```text
Email: test@secureops.local
Password: correct password
```

Expected result:

```text
Login successful
JWT token returned
```

---

## 6.2 Wrong Password

We also test an incorrect password.

Example:

```text
Email: user@secureops.local
Password: wrong password
```

Expected result:

```text
401 Unauthorized
```

The user must not receive a valid JWT.

---

## 6.3 Disabled User

A disabled account must not be able to log in.

Even if the password is correct:

```text
Account = DISABLED
Password = Correct
```

Expected result:

```text
401 Unauthorized
```

This prevents disabled accounts from using the application.

---

# 7. Password Security

The application uses BCrypt to protect passwords.

Passwords should never be stored as plain text.

Example:

```text
Password:
MyPassword123
```

is stored as a BCrypt hash similar to:

```text
$2a$...
```

The tests verify that:

* the password is hashed;
* the hash is different from the original password;
* the correct password matches the hash;
* the wrong password does not match the hash.

---

# 8. JWT Security

JWT means:

> **JSON Web Token**

After successful login, the application gives the user a JWT.

The client then sends the JWT with protected requests.

Example:

```text
Authorization: Bearer <JWT>
```

The application uses the JWT filter to validate the token.

---

## 8.1 Request Without JWT

We test protected endpoints without authentication.

Example:

```text
GET /api/admin/users
```

without:

```text
Authorization: Bearer <JWT>
```

Expected result:

```text
403 Forbidden
```

The request must not access the protected resource.

---

## 8.2 Invalid JWT

We also test an invalid JWT.

Example:

```text
Bearer this.is.not.a.valid.jwt
```

Expected result:

```text
403 Forbidden
```

The application must reject the invalid token.

---

# 9. Role-Based Access Control

RBAC means:

> **Role-Based Access Control**

Each user has a role.

The application currently uses:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

Each role has different permissions.

For example:

```text
ADMIN
    ↓
Administrative operations

SECURITY_ANALYST
    ↓
Security and investigation operations

MANAGER
    ↓
Management and incident viewing

EMPLOYEE
    ↓
Basic application operations
```

The exact permissions are controlled by Spring Security.

---

# 10. Admin Authorization

Administrative endpoints are protected.

Example:

```text
GET /api/admin/users
```

The expected authorization is:

| Role             | Access    |
| ---------------- | --------- |
| ADMIN            | Allowed   |
| SECURITY_ANALYST | Forbidden |
| MANAGER          | Forbidden |
| EMPLOYEE         | Forbidden |

The tests verify that an ADMIN can access the endpoint.

They also verify that other roles cannot access it.

---

# 11. Incident Security

The application contains an incident management system.

Users can work with incidents through REST endpoints.

The main operations tested are:

```text
GET
POST
PATCH
```

---

## 11.1 Read Incidents

Endpoint:

```text
GET /api/incidents/**
```

Permissions:

| Role             | Access    |
| ---------------- | --------- |
| ADMIN            | Allowed   |
| SECURITY_ANALYST | Allowed   |
| MANAGER          | Allowed   |
| EMPLOYEE         | Forbidden |

This means employees cannot read the protected incident list.

---

## 11.2 Create Incidents

Endpoint:

```text
POST /api/incidents/**
```

Permissions:

| Role             | Access  |
| ---------------- | ------- |
| ADMIN            | Allowed |
| SECURITY_ANALYST | Allowed |
| MANAGER          | Allowed |
| EMPLOYEE         | Allowed |

The application allows all four roles to create an incident according to the current security configuration.

---

## 11.3 Update Incidents

Endpoint:

```text
PATCH /api/incidents/**
```

Permissions:

| Role             | Access    |
| ---------------- | --------- |
| ADMIN            | Allowed   |
| SECURITY_ANALYST | Allowed   |
| MANAGER          | Forbidden |
| EMPLOYEE         | Forbidden |

Only administrators and security analysts can update incidents.

---

# 12. Investigation Notes

Investigation notes are connected to incidents.

The endpoint is:

```text
/api/incidents/{incidentId}/notes
```

There are two main operations:

```text
GET
POST
```

---

## 12.1 Read Investigation Notes

Endpoint:

```text
GET /api/incidents/{incidentId}/notes
```

Permissions:

| Role             | Access    |
| ---------------- | --------- |
| ADMIN            | Allowed   |
| SECURITY_ANALYST | Allowed   |
| MANAGER          | Allowed   |
| EMPLOYEE         | Forbidden |

Managers can read investigation notes, but employees cannot.

---

## 12.2 Create Investigation Notes

Endpoint:

```text
POST /api/incidents/{incidentId}/notes
```

Permissions:

| Role             | Access    |
| ---------------- | --------- |
| ADMIN            | Allowed   |
| SECURITY_ANALYST | Allowed   |
| MANAGER          | Forbidden |
| EMPLOYEE         | Forbidden |

Only administrators and security analysts can create investigation notes.

---

## 12.3 Investigation Note Request

The request body is:

```json
{
  "content": "Investigation note content"
}
```

The application validates the note content before creating the note.

---

# 13. Negative Security Testing

Negative testing means testing actions that **should fail**.

This is very important for security.

We do not only test:

```text
Can the user access the endpoint?
```

We also test:

```text
Can an unauthorized user access the endpoint?
```

Examples:

```text
No JWT
        → Access rejected

Invalid JWT
        → Access rejected

Disabled account
        → Login rejected

Employee → Admin endpoint
        → Access rejected

Employee → Investigation notes
        → Access rejected

Manager → Create investigation note
        → Access rejected

Employee → Create investigation note
        → Access rejected
```

These tests confirm that unauthorized access is blocked.

---

# 14. HTTP Status Codes

The tests use HTTP status codes to verify security behavior.

## 200 OK

The request was successful.

```text
200 OK
```

Example:

```text
ADMIN → allowed endpoint
```

---

## 401 Unauthorized

Authentication failed.

Examples:

```text
Wrong password
Disabled account
```

Result:

```text
401 Unauthorized
```

---

## 403 Forbidden

The user is not allowed to perform the requested action.

Example:

```text
EMPLOYEE → ADMIN endpoint
```

Result:

```text
403 Forbidden
```

A `403` message in the test logs does **not always mean the test failed**.

For example:

```text
AccessDeniedHandlerImpl
Responding with 403 status code
```

This can be exactly what the security test expects.

The important final result is:

```text
Failures: 0
Errors: 0
BUILD SUCCESS
```

---

# 15. Automated Testing

The complete test suite can be executed with one Maven command:

```text
mvn clean test
```

This command:

1. Removes old build files.
2. Compiles the application.
3. Compiles the tests.
4. Finds the test classes.
5. Runs all tests.
6. Reports the results.
7. Fails the build if a test fails.

The process is:

```text
mvn clean test
       |
       v
Clean
       |
       v
Compile
       |
       v
Test discovery
       |
       v
Run tests
       |
       v
Test results
       |
       v
BUILD SUCCESS
```

---

# 16. Automation Result

The complete automated test suite was executed successfully.

Command:

```text
mvn clean test
```

Result:

```text
Tests run: 23
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Maven exit code:

```text
0
```

This means the complete test execution was successful.

---

# 17. Test Summary

The project currently has:

```text
23 automated tests
```

Final result:

```text
23 tests
0 failures
0 errors
0 skipped
```

Security areas tested:

| Security Area                    | Result |
| -------------------------------- | ------ |
| Authentication                   | PASS   |
| Wrong password                   | PASS   |
| Disabled account                 | PASS   |
| Password hashing                 | PASS   |
| JWT security                     | PASS   |
| Protected endpoints              | PASS   |
| RBAC                             | PASS   |
| Admin authorization              | PASS   |
| Incident authorization           | PASS   |
| Investigation-note authorization | PASS   |
| Negative security tests          | PASS   |
| Maven automation                 | PASS   |

---

# 18. Day 09 Checklist

```text
Security architecture inspection          [x]
Existing test inspection                  [x]
Maven test configuration inspection       [x]
SecurityConfig inspection                 [x]
JWT filter inspection                     [x]
Authentication implementation inspection  [x]

AuthenticationSecurityTest                [x]
JWT security tests                        [x]
RBAC tests                                [x]
Authorization tests                       [x]
Negative tests                            [x]
Incident security tests                   [x]
Investigation-note tests                  [x]

Automation                                [x]
Documentation                             [x]

Git                                       [ ]
```

At this point, only the Git step remains.

---

# 19. What We Learned

Day 09 helped validate the security model of the application.

The main lessons are:

### Authentication

Authentication answers:

> **Who are you?**

Example:

```text
Login with email and password
```

### Authorization

Authorization answers:

> **What are you allowed to do?**

Example:

```text
ADMIN → can access admin endpoints
EMPLOYEE → cannot access admin endpoints
```

### RBAC

RBAC connects permissions to user roles.

```text
User
  |
  v
Role
  |
  v
Permissions
  |
  v
Endpoint access
```

### Negative Testing

Security testing must also verify that unauthorized actions are blocked.

```text
Allowed action → PASS
Forbidden action → PASS when rejected
```

---

# 20. Final Result

Day 09 successfully created an automated security testing foundation for the SecureOps Platform.

The application now tests:

* Authentication
* Password security
* Disabled accounts
* JWT authentication
* RBAC
* Authorization
* Admin access
* Incident access
* Investigation-note access
* Negative security cases
* Automated Maven execution

The latest complete test result is:

```text
Tests run: 23
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Therefore, the security test suite is currently passing successfully.

---

# 21. Next Step

After the Day 09 documentation is committed to Git, the next task is:

```text
DAY 10 — CI/CD Security Automation
```

Day 10 will focus on automatically running security checks during the development and CI/CD process.