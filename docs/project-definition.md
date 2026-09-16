# SecureOps Platform — Project Definition

## 1. Project Overview

**SecureOps Platform** is a secure enterprise web application designed to manage employees, security incidents, incident investigations, reports, and security-related activities.

The project is designed as a practical **DevSecOps** project. Security is integrated throughout the software development lifecycle rather than being added only after the application is finished.

The platform combines:

* Secure web application development
* Authentication and authorization
* Role-Based Access Control (RBAC)
* Security incident management
* Audit logging
* Automated security testing
* CI/CD
* Containerization
* Vulnerability scanning
* Application security monitoring

The project is also designed as a realistic engineering laboratory where security vulnerabilities can be intentionally introduced, detected, fixed, and retested.

---

# 2. Problem Statement

Modern organizations manage large amounts of employee and security-related information through web applications.

A poorly secured application can expose sensitive information or allow unauthorized users to perform actions they should not be allowed to perform.

Traditional development approaches often treat security as a final testing activity.

This creates several problems:

* Vulnerabilities may be discovered too late.
* Security problems become more expensive to fix.
* Vulnerable dependencies may remain unnoticed.
* Secrets may accidentally be committed to source control.
* Container images may contain vulnerable components.
* Security testing may not be repeated automatically.
* Production activity may not be properly monitored.

The project addresses these problems by integrating security into the complete development and deployment process.

---

# 3. Proposed Solution

SecureOps Platform provides a centralized web application for employee and security incident management while integrating security controls throughout the DevSecOps lifecycle.

The application will provide different capabilities depending on the user's role.

At the same time, the software delivery pipeline will automatically perform security checks before the application can be deployed.

The general approach is:

```text
Secure Development
        ↓
Automated Testing
        ↓
Security Analysis
        ↓
Container Security
        ↓
Dynamic Security Testing
        ↓
Security Gate
        ↓
Deployment
        ↓
Monitoring
        ↓
Incident Detection
        ↓
Continuous Improvement
```

The objective is not only to build a functional application, but to demonstrate how a modern engineering team can continuously develop and secure software.

---

# 4. Project Objectives

## 4.1 Main Objective

The main objective is to design and implement a secure enterprise web application supported by a complete DevSecOps pipeline.

## 4.2 Technical Objectives

The project aims to:

1. Develop a modern web application using Angular and Spring Boot.
2. Implement secure authentication.
3. Implement role-based authorization.
4. Protect sensitive application resources.
5. Store application data securely in PostgreSQL.
6. Implement audit logging.
7. Containerize the application using Docker.
8. Automate the build and testing process.
9. Integrate security tools into the CI/CD pipeline.
10. Detect vulnerable source code and dependencies.
11. Detect accidentally exposed secrets.
12. Scan container images for vulnerabilities.
13. Perform dynamic security testing against the running application.
14. Implement automated security gates.
15. Monitor application and infrastructure activity.
16. Integrate security monitoring with a SIEM when feasible.

---

# 5. Target Users

The application will support four main user categories.

## 5.1 Administrator

The administrator manages the platform.

Main responsibilities:

* Manage users
* Manage roles
* Manage system configuration
* View audit logs
* Monitor important platform activities

---

## 5.2 Security Analyst

The security analyst is responsible for investigating security incidents.

Main responsibilities:

* View security incidents
* Analyze incidents
* Assign severity
* Add investigation notes
* Update incident status
* Close incidents
* Review security-related information

---

## 5.3 Manager

The manager can monitor information related to their team.

Main responsibilities:

* View team information
* View relevant incidents
* Access reports
* Monitor employee-related information

---

## 5.4 Employee

The employee has access to personal and operational information.

Main responsibilities:

* View personal profile
* Report a security incident
* View assigned information
* View relevant incident status

---

# 6. Role-Based Access Control

The application will use **RBAC — Role-Based Access Control**.

RBAC means that access permissions are associated with roles rather than being individually configured for every user.

The main roles are:

| Role             | Main Responsibilities                             |
| ---------------- | ------------------------------------------------- |
| ADMIN            | User management, roles, configuration, audit logs |
| SECURITY_ANALYST | Incident investigation and security analysis      |
| MANAGER          | Team information and reports                      |
| EMPLOYEE         | Personal information and incident reporting       |

The authorization model will follow the principle of **least privilege**.

This means that users should receive only the permissions required for their responsibilities.

Example:

```text
EMPLOYEE
   │
   ├── View own profile
   ├── Report incident
   └── View own information

SECURITY_ANALYST
   │
   ├── View incidents
   ├── Investigate incidents
   ├── Add notes
   └── Close incidents

ADMIN
   │
   ├── Manage users
   ├── Manage roles
   ├── View audit logs
   └── Configure system
```

---

# 7. Functional Requirements

## 7.1 Authentication

The system shall provide secure user authentication.

Expected capabilities include:

* Login
* Logout
* Password protection
* Token-based authentication
* Session/token expiration
* Authentication error handling

Future authentication improvements may include stronger multi-factor authentication.

---

## 7.2 User Management

Administrators shall be able to:

* Create users
* Update users
* Activate/deactivate users
* Assign roles
* View user information

---

## 7.3 Employee Management

The platform shall provide employee information management.

Employees shall be able to view their own information.

Authorized users shall be able to access employee information according to their permissions.

---

## 7.4 Security Incident Management

Users shall be able to report security incidents according to their role.

Security analysts shall be able to:

* View incidents
* Analyze incidents
* Set severity
* Update status
* Add investigation notes
* Close incidents

Example incident lifecycle:

```text
REPORTED
    ↓
ASSIGNED
    ↓
UNDER INVESTIGATION
    ↓
RESOLVED
    ↓
CLOSED
```

---

## 7.5 Incident Severity

Incidents may use different severity levels.

Example:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

Severity helps security analysts prioritize investigation activities.

---

## 7.6 Audit Logging

The application shall record important security and administrative activities.

Examples include:

* Login attempts
* Logout events
* User creation
* Role changes
* Incident creation
* Incident updates
* Administrative actions
* Security-related events

Example:

```text
2026-09-16 14:20
User: analyst01
Action: INCIDENT_UPDATED
Resource: INC-001
Result: SUCCESS
```

Audit logs help with:

* Accountability
* Investigation
* Compliance
* Troubleshooting
* Security monitoring

---

# 8. Security Requirements

Security is a central requirement of the project.

## 8.1 Authentication Security

The application shall:

* Protect authentication endpoints.
* Use secure password storage.
* Validate authentication input.
* Handle authentication failures safely.
* Expire authentication tokens appropriately.

---

## 8.2 Authorization Security

The application shall enforce authorization on protected resources.

Authorization must be checked on the backend and not only in the frontend.

Example:

```text
Angular UI
    ↓
Spring Boot API
    ↓
Authentication
    ↓
Authorization
    ↓
Business Logic
```

The backend remains the final security enforcement point.

---

## 8.3 Input Validation

User-controlled input shall be validated before processing.

This reduces the risk of vulnerabilities such as:

* Injection
* Invalid data
* Malformed requests
* Unexpected application behavior

---

## 8.4 Data Protection

Sensitive information shall be protected during storage and transmission.

The application architecture will use HTTPS for communication in deployment environments where TLS is configured.

Database access shall use controlled credentials and appropriate permissions.

---

## 8.5 Least Privilege

Users, applications, containers, and services should receive only the permissions they require.

This principle will be applied to:

* Application roles
* Database access
* CI/CD credentials
* Containers
* Deployment environments

---

## 8.6 Secrets Management

Sensitive credentials shall not be stored directly in source code.

Examples include:

* Database passwords
* API keys
* Authentication secrets
* CI/CD credentials

Development and CI/CD environments will use environment variables or GitHub Actions Secrets.

HashiCorp Vault may be introduced later as an advanced improvement.

---

## 8.7 Dependency Security

Third-party libraries will be analyzed for known vulnerabilities.

The project will use:

**OWASP Dependency-Check**

to identify vulnerable dependencies.

---

## 8.8 Source Code Security

The source code will be analyzed automatically using:

**SonarQube**

The objective is to identify potential:

* Bugs
* Vulnerabilities
* Security weaknesses
* Code quality problems

---

## 8.9 Secret Detection

The repository will be scanned using:

**Gitleaks**

The purpose is to detect accidentally committed secrets.

---

## 8.10 Container Security

Docker images will be scanned using:

**Trivy**

The scan will identify known vulnerabilities in:

* Operating system packages
* Application dependencies
* Container components

---

## 8.11 Dynamic Application Security

The running application will be tested using:

**OWASP ZAP**

This allows the project to test the application from an attacker's perspective after deployment to a test environment.

---

# 9. DevSecOps Requirements

Security controls will be integrated into the CI/CD pipeline.

The pipeline will approximately follow:

```text
Developer
    ↓
Git Push
    ↓
GitHub Repository
    ↓
GitHub Actions
    ↓
SAST
    ↓
SCA
    ↓
Secret Scan
    ↓
Automated Tests
    ↓
Build
    ↓
Docker Build
    ↓
Container Scan
    ↓
Test Environment
    ↓
DAST
    ↓
Security Gate
    ↓
Deployment
    ↓
Monitoring
```

The pipeline should automatically stop when critical security conditions are not satisfied.

---

# 10. Security Tools

| Security Area      | Tool                   | Purpose                        |
| ------------------ | ---------------------- | ------------------------------ |
| SAST               | SonarQube              | Analyze source code            |
| SCA                | OWASP Dependency-Check | Detect vulnerable dependencies |
| Secret Scanning    | Gitleaks               | Detect exposed secrets         |
| Container Security | Trivy                  | Scan container images          |
| DAST               | OWASP ZAP              | Test running application       |
| CI/CD              | GitHub Actions         | Automate pipeline              |
| SIEM               | Wazuh                  | Security monitoring            |
| Containerization   | Docker                 | Package applications           |

---

# 11. Technology Stack

## Frontend

* Angular
* TypeScript
* HTML
* CSS

## Backend

* Java
* Spring Boot
* Spring Security
* REST API

## Database

* PostgreSQL

## DevOps

* Git
* GitHub
* GitHub Actions
* Docker
* Docker Compose

## Security

* SonarQube
* OWASP Dependency-Check
* Gitleaks
* Trivy
* OWASP ZAP
* Wazuh

---

# 12. Project Architecture

The main application architecture is:

```text
                    ┌─────────────────────┐
                    │       Users         │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Angular Frontend    │
                    └──────────┬──────────┘
                               │
                         HTTPS / REST
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Spring Boot API     │
                    │                     │
                    │ Authentication      │
                    │ Authorization       │
                    │ Business Logic      │
                    │ Audit Logging        │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ PostgreSQL          │
                    │ Database            │
                    └─────────────────────┘
```

Security testing and deployment are handled by the DevSecOps pipeline.

---

# 13. Project Scope

## 13.1 In Scope

The project includes:

* Web application development
* Authentication
* Authorization
* RBAC
* Employee management
* Security incident management
* Incident investigation
* Audit logging
* REST API
* PostgreSQL database
* Docker containerization
* CI/CD
* SAST
* SCA
* Secret scanning
* Container scanning
* DAST
* Security gates
* Security documentation
* Security testing
* Monitoring

---

## 13.2 Out of Scope

The following features are not part of the initial implementation:

* Full enterprise Active Directory integration
* Large-scale cloud infrastructure
* Production-grade Kubernetes cluster
* Full enterprise SIEM deployment
* Advanced machine-learning threat detection
* Mobile application
* Large-scale distributed architecture

These features may be considered future improvements.

---

# 14. Testing Strategy

Testing will be performed at multiple levels.

## Functional Testing

Verifies that application features work correctly.

Examples:

* Login
* User creation
* Incident creation
* Incident updates
* Role management

## Security Testing

Verifies that security controls work correctly.

Examples:

* Unauthorized access
* Invalid authentication
* Privilege escalation attempts
* Injection testing
* Exposed secrets
* Vulnerable dependencies

## Pipeline Testing

Verifies that security tools correctly detect controlled security problems.

Example:

```text
Introduce controlled vulnerability
            ↓
Run pipeline
            ↓
Security tool detects problem
            ↓
Pipeline fails
            ↓
Fix vulnerability
            ↓
Run pipeline again
            ↓
Security check passes
```

This demonstrates that the DevSecOps pipeline is not only installed but actually working.

---

# 15. Security Experimentation

The project will include controlled security experiments inside an isolated development environment.

The purpose is to understand the complete security lifecycle:

```text
Introduce Vulnerability
        ↓
Detect Vulnerability
        ↓
Analyze Vulnerability
        ↓
Fix Vulnerability
        ↓
Run Security Test Again
        ↓
Verify Remediation
```

Examples may include:

* Vulnerable dependency
* Hardcoded secret
* Insecure configuration
* Weak authorization rule
* Vulnerable container package
* Web application security issue

Only controlled and isolated test cases will be used.

---

# 16. Expected Results

At the end of the project, the expected result is a functional secure web application supported by an automated DevSecOps pipeline.

The project should demonstrate that:

1. The application works correctly.
2. Users have controlled access based on their roles.
3. Security vulnerabilities can be detected automatically.
4. Vulnerable dependencies can be identified.
5. Secrets can be detected before deployment.
6. Docker images can be scanned.
7. The running application can be dynamically tested.
8. Security failures can stop the deployment process.
9. Security events can be monitored.
10. Security findings can be documented and remediated.

---

# 17. Success Criteria

The project will be considered successful when the following conditions are achieved:

### Application

* [ ] Angular frontend implemented
* [ ] Spring Boot backend implemented
* [ ] PostgreSQL database connected
* [ ] Authentication implemented
* [ ] RBAC implemented
* [ ] Employee management implemented
* [ ] Incident management implemented
* [ ] Audit logging implemented

### DevSecOps

* [ ] Git repository configured
* [ ] GitHub Actions pipeline implemented
* [ ] Automated tests implemented
* [ ] SonarQube integrated
* [ ] OWASP Dependency-Check integrated
* [ ] Gitleaks integrated
* [ ] Docker implemented
* [ ] Trivy integrated
* [ ] OWASP ZAP integrated
* [ ] Security gate implemented

### Operations

* [ ] Application deployed in a controlled environment
* [ ] Logs collected
* [ ] Security monitoring implemented
* [ ] Wazuh integration evaluated or implemented

### Documentation

* [ ] Architecture documented
* [ ] Security tools documented
* [ ] Security experiments documented
* [ ] Vulnerabilities documented
* [ ] Remediation steps documented
* [ ] Pipeline results documented
* [ ] Final project report completed

---

# 18. Project Deliverables

The project will produce the following deliverables:

### 1. Source Code

Complete Angular and Spring Boot application.

### 2. Database

PostgreSQL database schema and configuration.

### 3. Docker Configuration

Dockerfiles and Docker Compose configuration.

### 4. CI/CD Pipeline

GitHub Actions workflows implementing automated build, testing, security analysis, and deployment.

### 5. Security Configuration

Security controls and configurations used by the application and pipeline.

### 6. Security Reports

Reports generated by security tools.

Examples:

* SAST results
* Dependency vulnerability results
* Secret scanning results
* Container vulnerability results
* DAST results

### 7. Documentation

Technical and security documentation explaining:

* Architecture
* Security model
* DevSecOps pipeline
* Tools
* Vulnerabilities
* Remediation
* Testing
* Lessons learned

---

# 19. Learning Objectives

This project is also a practical learning environment.

By completing the project, the developer should gain practical experience in:

* Secure software development
* DevSecOps
* CI/CD
* Application security
* Authentication
* Authorization
* RBAC
* Vulnerability management
* SAST
* SCA
* DAST
* Container security
* Secret management
* Security automation
* Security monitoring
* Incident management
* Technical documentation

The objective is to understand not only **how to use security tools**, but also **why they are used, what they detect, how to interpret their results, and how to remediate the identified problems**.

---

# 20. Final Project Vision

SecureOps Platform is intended to demonstrate a complete security-oriented software engineering workflow.

The project connects software development, security, deployment, and operations into one continuous process:

```text
                 SECUREOPS PLATFORM

                         │
                         ▼
                 Secure Development
                         │
                         ▼
                  Automated Testing
                         │
                         ▼
                  Security Analysis
                         │
                         ▼
                  Secure Packaging
                         │
                         ▼
                   Security Testing
                         │
                         ▼
                    Security Gate
                         │
                         ▼
                     Deployment
                         │
                         ▼
                    Monitoring
                         │
                         ▼
                Security Incidents
                         │
                         ▼
                 Continuous Improvement
                         │
                         └──────────────► Development
```

The final goal is to demonstrate a practical implementation of **DevSecOps from development to operations**, while building a project that reflects real-world software engineering and cybersecurity practices.
