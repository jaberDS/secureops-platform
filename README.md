# 🛡️ SecureOps Platform

## Secure Enterprise Platform with End-to-End DevSecOps

SecureOps Platform is a security-focused enterprise web application designed to demonstrate how **security can be integrated throughout the Software Development Lifecycle (SDLC)**.

The project combines:

* Secure software development
* Backend authentication and authorization
* DevSecOps practices
* CI/CD automation
* Application security testing
* Container security
* Security monitoring
* Vulnerability management

The goal is not only to build an application, but to demonstrate how a software engineer can **build, secure, test, monitor, and continuously improve** an application.

---

# 🎯 Project Objectives

The main objectives of SecureOps Platform are:

* Build a secure enterprise web application
* Apply security throughout the SDLC
* Implement authentication and authorization
* Protect APIs using JWT
* Apply Role-Based Access Control (RBAC)
* Secure passwords using BCrypt
* Validate and sanitize application input
* Implement CI/CD with GitHub Actions
* Integrate automated security testing
* Detect vulnerabilities early
* Secure containerized applications
* Implement security monitoring
* Produce security documentation and reports

---

# 🏗️ Application Architecture

The application follows a modern three-layer architecture:

```text
                 SecureOps Platform
                        |
          +-------------+-------------+
          |                           |
       Frontend                    Backend
       Angular                  Spring Boot
          |                           |
          |                      Spring Security
          |                           |
          |                       REST API
          |                           |
          +-------------+-------------+
                        |
                    PostgreSQL
```

### Main Components

**Frontend**

* Angular
* User interface
* Authentication interface
* Dashboards
* API communication

**Backend**

* Spring Boot
* REST APIs
* Business logic
* Validation
* Authentication
* Authorization

**Database**

* PostgreSQL
* User data
* Employee data
* Application data

---

# 🔐 Security Architecture

Security is implemented at multiple layers.

```text
User Input
    ↓
Input Validation
    ↓
Authentication
    ↓
JWT Validation
    ↓
User Identification
    ↓
RBAC Authorization
    ↓
Protected API
    ↓
PostgreSQL
```

The backend is the main security boundary.

The frontend must never be trusted to enforce authorization by itself.

---

# 🔑 Authentication

SecureOps uses **Spring Security** for authentication and authorization.

The authentication flow is:

```text
User
 ↓
Email + Password
 ↓
AuthenticationManager
 ↓
UserDetailsService
 ↓
PostgreSQL
 ↓
BCrypt Verification
 ↓
JWT Generation
 ↓
Authenticated Client
```

Passwords are never stored as plaintext.

They are hashed using **BCrypt** before being stored in PostgreSQL.

---

# 🎫 JWT Authentication

After successful authentication, the backend generates a **JWT (JSON Web Token)**.

The client sends the token with protected requests:

```text
Authorization: Bearer <JWT>
```

The backend then:

1. Extracts the JWT
2. Validates the token
3. Extracts the user identity
4. Loads the user from PostgreSQL
5. Loads the user's role
6. Creates the Spring Security authentication
7. Applies authorization rules

Simplified flow:

```text
Client
  |
  | Bearer JWT
  ↓
JWT Authentication Filter
  |
  ↓
JWT Validation
  |
  ↓
User Lookup
  |
  ↓
Spring Security
  |
  ↓
RBAC
  |
  ↓
Protected Endpoint
```

---

# 👥 Role-Based Access Control

SecureOps implements **RBAC — Role-Based Access Control**.

The current roles are:

| Role               | Responsibility                |
| ------------------ | ----------------------------- |
| `ADMIN`            | Administrative operations     |
| `SECURITY_ANALYST` | Security operations           |
| `MANAGER`          | Management operations         |
| `EMPLOYEE`         | Standard authenticated access |

Example authorization rules:

```text
/api/admin/**       → ADMIN
/api/security/**    → SECURITY_ANALYST
/api/manager/**     → MANAGER
```

Users without the required role are denied access by the backend.

---

# 🛡️ Security Controls Implemented

## Application Security

* Backend input validation
* Global validation exception handling
* BCrypt password hashing
* Secure authentication
* JWT authentication
* Role-Based Access Control
* Protected API endpoints
* Server-side authorization

## Security Testing

* Authentication failure testing
* Authorization failure testing
* Invalid input testing
* Duplicate account testing
* Password storage verification
* Protected endpoint testing
* Sensitive data exposure review
* Log security review

---

# ⚙️ DevSecOps Pipeline

The project is designed around the following DevSecOps workflow:

```text
Developer
    ↓
Git Push
    ↓
GitHub
    ↓
GitHub Actions
    ↓
Security Checks
    |
    +── SAST
    +── SCA
    +── Secret Scanning
    |
    ↓
Build + Tests
    ↓
Docker Build
    ↓
Container Scan
    |
    +── Trivy
    |
    ↓
Test Environment
    ↓
DAST
    |
    +── OWASP ZAP
    |
    ↓
Security Gate
    ↓
Deployment
    ↓
Monitoring
    ↓
SIEM
```

The objective is to detect security problems as early as possible instead of waiting until production.

---

# 🧰 Technology Stack

## Application

* Angular
* Spring Boot
* PostgreSQL

## Development

* Java
* Maven
* TypeScript
* REST API

## DevOps

* Git
* GitHub
* GitHub Actions
* Docker
* Docker Compose

## Security

* Spring Security
* BCrypt
* JWT
* SonarQube
* OWASP Dependency-Check
* Gitleaks
* Trivy
* OWASP ZAP

## Monitoring

* Application logs
* Security monitoring
* SIEM
* Wazuh

---

# 📂 Project Structure

```text
secureops-platform/
│
├── backend/
│   └── Spring Boot application
│
├── frontend/
│   └── Angular application
│
├── docs/
│   ├── architecture/
│   │   ├── application-architecture.md
│   │   └── devsecops-pipeline.md
│   │
│   ├── security/
│   │   └── tool-selection.md
│   │
│   ├── glossary.md
│   ├── project-definition.md
│   ├── day-01-devsecops-foundation.md
│   ├── day-02-development.md
│   └── day-03-security.md
│
├── docker-compose.yml
├── .gitignore
├── LICENSE
└── README.md
```

---

# 🧪 Security Testing

Security testing is treated as part of the development process.

Current tests include:

| Security Test                  | Result |
| ------------------------------ | ------ |
| Successful registration        | ✅ PASS |
| Duplicate registration         | ✅ PASS |
| Invalid registration           | ✅ PASS |
| BCrypt password verification   | ✅ PASS |
| Successful login               | ✅ PASS |
| Wrong password                 | ✅ PASS |
| Unknown user                   | ✅ PASS |
| ADMIN authorization            | ✅ PASS |
| SECURITY_ANALYST authorization | ✅ PASS |
| MANAGER authorization          | ✅ PASS |
| EMPLOYEE authentication        | ✅ PASS |
| Unauthorized role access       | ✅ PASS |
| Protected endpoint without JWT | ✅ PASS |
| Protected endpoint with JWT    | ✅ PASS |
| Sensitive data review          | ✅ PASS |
| PostgreSQL log review          | ✅ PASS |

---

# 📊 Current Project Status

### Completed

* [x] Project definition
* [x] Application architecture
* [x] DevSecOps architecture
* [x] Development environment
* [x] PostgreSQL integration
* [x] Employee API
* [x] Backend validation
* [x] Global exception handling
* [x] User entity
* [x] User registration
* [x] BCrypt password hashing
* [x] Spring Security
* [x] AuthenticationManager
* [x] JWT authentication
* [x] JWT request filtering
* [x] Role-Based Access Control
* [x] Authentication security tests
* [x] Authorization security tests
* [x] Sensitive data review
* [x] Log security review
* [x] Day 3 security documentation

### In Progress

* [ ] Angular frontend
* [ ] Complete employee management
* [ ] Security incident management
* [ ] Audit logging
* [ ] Security dashboard
* [ ] GitHub Actions CI/CD
* [ ] SAST integration
* [ ] SCA integration
* [ ] Gitleaks
* [ ] Trivy
* [ ] OWASP ZAP
* [ ] Security gates
* [ ] Production-style deployment
* [ ] SIEM integration

---

# 📚 Documentation

The project documentation is organized by topic and development phase.

### Foundations

* `docs/glossary.md`
* `docs/project-definition.md`

### Architecture

* `docs/architecture/application-architecture.md`
* `docs/architecture/devsecops-pipeline.md`

### Security

* `docs/security/tool-selection.md`
* `docs/day-03-security.md`

### Development

* `docs/day-01-devsecops-foundation.md`
* `docs/day-02-development.md`

The documentation explains not only **what was implemented**, but also **why each technology and security control was selected**.

---

# 🚀 Development Philosophy

SecureOps follows a practical security engineering approach:

```text
BUILD
  ↓
UNDERSTAND
  ↓
TEST
  ↓
BREAK
  ↓
FIX
  ↓
DOCUMENT
  ↓
AUTOMATE
  ↓
MONITOR
  ↓
IMPROVE
```

Security is therefore treated as a continuous process rather than a final step before deployment.

---

# 🔮 Future Security Improvements

The current implementation is designed for a development and learning environment.

Future improvements include:

* Move JWT secrets to secure environment variables
* Add refresh-token management
* Improve authentication error handling
* Add rate limiting
* Add brute-force protection
* Introduce response DTOs
* Improve security logging
* Add audit trails
* Add automated SAST
* Add dependency vulnerability scanning
* Add secret scanning
* Add container vulnerability scanning
* Add DAST
* Add automated security gates
* Integrate SIEM monitoring
* Implement production deployment security

---

# 🎓 Project Goal

SecureOps Platform is being developed as a practical **DevSecOps and cybersecurity engineering project**.

The final objective is to demonstrate the complete lifecycle:

```text
PLAN
 ↓
DESIGN
 ↓
DEVELOP
 ↓
SECURE
 ↓
TEST
 ↓
SCAN
 ↓
BUILD
 ↓
DEPLOY
 ↓
MONITOR
 ↓
RESPOND
 ↓
IMPROVE
```

The project combines **software engineering, cybersecurity, DevOps, application security, and security operations** into one practical platform.
