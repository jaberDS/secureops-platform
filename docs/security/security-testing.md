# Security Testing

## Purpose

This document records the security tests performed on the SecureOps Platform backend.

The goal is to verify that authentication, authorization, input validation, and incident lifecycle controls work as designed.

---

# 1. Authentication Testing

## 1.1 Unauthenticated Request

### Test

Request the protected incident endpoint without a JWT.

```http
GET /api/incidents
Expected Result

The API must deny access.

Observed Result
HTTP 403 Forbidden
Status

PASS

2. Invalid JWT Testing
2.1 Fake JWT
Test

Send a request using an invalid Bearer token.

GET /api/incidents
Authorization: Bearer <invalid-token>
Expected Result

The API must reject the invalid token and must not return incident data.

Observed Result
HTTP 403 Forbidden
Status

PASS

3. Role-Based Access Control Testing
3.1 Employee
Allowed Action

An authenticated employee can report an incident.

POST /api/incidents
Observed Result
2xx Success
Status

PASS

Restricted Action

An employee cannot change an incident status.

PATCH /api/incidents/{id}/status
Observed Result
HTTP 403 Forbidden
Status

PASS

4. Security Analyst Authorization

A SECURITY_ANALYST can manage the investigation workflow.

Test

Change an incident from:

ASSIGNED → INVESTIGATING
Observed Result
2xx Success
Status

PASS

5. Manager Authorization

A MANAGER can view incidents.

Test
GET /api/incidents
Observed Result
2xx Success
Status

PASS

Restricted Action

A MANAGER cannot change an incident status.

Test
PATCH /api/incidents/{id}/status
Observed Result
HTTP 403 Forbidden
Status

PASS

6. Investigation Notes Authorization

Investigation notes contain potentially sensitive investigation information.

Security Analyst

A SECURITY_ANALYST can create and read investigation notes.

Result
POST /api/incidents/{id}/notes → 2xx
GET  /api/incidents/{id}/notes → 2xx
Status

PASS

Manager

A MANAGER can read investigation notes.

GET /api/incidents/{id}/notes → 2xx

However, a MANAGER cannot create investigation notes.

POST /api/incidents/{id}/notes → 403
Status

PASS

Employee

An EMPLOYEE cannot create investigation notes.

POST /api/incidents/{id}/notes → 403
Status

PASS

7. Input Validation Testing

The API validates incoming data using Jakarta Bean Validation.

Tested Cases
Test	Expected	Result
Blank note	400	PASS
Note longer than 5000 characters	400	PASS
Blank incident title	400	PASS
Blank incident description	400	PASS
Missing severity	400	PASS
Missing category	400	PASS
Title longer than 150 characters	400	PASS
Description longer than 2000 characters	400	PASS
Non-existent incident	400	PASS
Invalid analyst email	400	PASS
Non-existent analyst	400	PASS
Assign incident to Manager	400	PASS
8. Incident Lifecycle Protection

The backend implements a controlled incident lifecycle.

OPEN
  ↓
ASSIGNED
  ↓
INVESTIGATING
  ↓
CONTAINED
  ↓
RESOLVED
  ↓
CLOSED

Invalid transitions are rejected by the service layer.

Test

Attempt:

ASSIGNED → CLOSED
Expected Result

The transition must be rejected.

Observed Result
HTTP 400 Bad Request
Status

PASS

9. Password Protection

Passwords are not stored as plaintext.

The application uses:

BCryptPasswordEncoder

Authentication verifies the submitted password against the stored BCrypt hash.

Test

Authenticate a valid user.

Observed Result

A JWT was returned successfully.

The authentication response does not expose the user's passwordHash.

Status

PASS

10. IDOR Testing
Status

NOT APPLICABLE YET

The current API does not expose an endpoint such as:

GET /api/incidents/{id}

Therefore, a meaningful object-level authorization test cannot currently be performed against an individual incident resource.

The current:

GET /api/incidents

endpoint returns the collection available to the authenticated role.

An individual-resource IDOR test will be added when an individual incident endpoint is implemented.

11. Security Test Summary
Security Control	Status
Authentication	PASS
Invalid JWT rejection	PASS
Employee RBAC	PASS
Security Analyst RBAC	PASS
Manager RBAC	PASS
Investigation Notes RBAC	PASS
Input validation	PASS
Incident lifecycle protection	PASS
Password hashing/protection	PASS
IDOR	NOT APPLICABLE YET
12. Security Lessons Learned

During testing, several important security concepts were demonstrated:

Authentication

Authentication answers:

"Who are you?"

The application uses JWT-based authentication.

Authorization

Authorization answers:

"What are you allowed to do?"

The application uses role-based access control.

Input Validation

User-controlled input is validated before being processed.

Least Privilege

Different roles receive different permissions.

For example:

EMPLOYEE
    ↓
Report incidents

MANAGER
    ↓
View incidents
Review investigation information

SECURITY_ANALYST
    ↓
Investigate
Assign
Update status
Create investigation notes

ADMIN
    ↓
Administrative operations
Defense in Depth

Security is implemented at multiple layers:

Client
  ↓
Validation
  ↓
Authentication
  ↓
Authorization
  ↓
Service-layer business rules
  ↓
Database

This means security does not depend on only one mechanism.

13. Conclusion

The security testing performed so far demonstrates that the SecureOps Platform backend implements important security controls including:

JWT authentication
Invalid token rejection
Role-based access control
Input validation
BCrypt password protection
Incident lifecycle enforcement
Investigation-note authorization
Business-rule validation