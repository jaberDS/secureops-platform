# SecureOps Platform — DevSecOps & Security Glossary

## 1. Purpose

This glossary provides simple but technically accurate definitions of the main concepts used in the SecureOps Platform project.

Each concept includes:

* **Simple definition**
* **Engineer mental model**
* **SecureOps example**

The goal is not only to memorize terminology, but to understand how the concepts are connected in a real DevSecOps project.

---

# 2. Application Security Concepts

## Application Security

### Simple definition

Application Security means protecting software against vulnerabilities, attacks, unauthorized access, and misuse.

### Engineer mental model

Think about security at every layer:

```text
User
 ↓
Frontend
 ↓
API
 ↓
Authentication
 ↓
Authorization
 ↓
Business Logic
 ↓
Database
 ↓
Infrastructure
```

Each layer can contain security risks.

### SecureOps example

SecureOps protects:

* User authentication
* API endpoints
* User permissions
* Database access
* Secrets
* Containers
* Application dependencies

---

# 3. DevOps

## DevOps

### Simple definition

DevOps is a way of developing and operating software using collaboration, automation, and continuous delivery.

### Engineer mental model

Instead of:

```text
Developer → Finish → Give to Operations
```

DevOps aims for:

```text
Develop → Test → Build → Deploy → Monitor
             ↑                 ↓
             └─────────────────┘
```

Development and operations continuously work together.

### SecureOps example

GitHub Actions automatically builds and tests SecureOps after code changes.

---

# 4. DevSecOps

## DevSecOps

### Simple definition

DevSecOps means integrating security into the DevOps lifecycle.

### Engineer mental model

```text
DevOps
Development + Operations

        +

Security

        ↓

DevSecOps
```

Security is not a final step.

It is part of:

```text
Development
     ↓
Testing
     ↓
Build
     ↓
Deployment
     ↓
Operations
```

### SecureOps example

SecureOps runs:

```text
SAST
SCA
Secret Scan
Container Scan
DAST
Security Gate
```

inside the delivery process.

---

# 5. SDLC

## SDLC — Software Development Life Cycle

### Simple definition

SDLC is the complete lifecycle of software development.

### Typical lifecycle

```text
Requirements
     ↓
Design
     ↓
Development
     ↓
Testing
     ↓
Deployment
     ↓
Operations
     ↓
Maintenance
```

### SecureOps example

Security is considered during each phase instead of only during testing.

---

# 6. Shift Left

## Shift Left

### Simple definition

Shift Left means moving security and testing activities earlier in the software lifecycle.

### Engineer mental model

Traditional approach:

```text
Develop → Deploy → Security Test
```

Shift Left:

```text
Develop
  ↓
Security Check
  ↓
Test
  ↓
Build
  ↓
Deploy
```

The earlier a problem is detected, the sooner it can be fixed.

### SecureOps example

Gitleaks and SonarQube run during CI before deployment.

---

# 7. CI

## Continuous Integration

### Simple definition

Continuous Integration means automatically building and testing code when developers integrate changes into a shared repository.

### Engineer mental model

```text
Developer
   ↓
Git Push
   ↓
CI Pipeline
   ↓
Build
   ↓
Tests
   ↓
Security Checks
```

### SecureOps example

A GitHub Actions workflow starts when code is pushed to GitHub.

---

# 8. CD

## Continuous Delivery / Continuous Deployment

### Simple definition

CD is the automated process used to prepare or deploy software after it passes the required checks.

### Important distinction

**Continuous Delivery** means the software is automatically prepared for deployment.

**Continuous Deployment** means the software is automatically deployed after passing the required checks.

### SecureOps example

After security checks pass:

```text
Security Gate
     ↓
PASS
     ↓
Deployment
```

---

# 9. Pipeline

## DevSecOps Pipeline

### Simple definition

A pipeline is an automated sequence of steps used to build, test, secure, and deploy software.

### SecureOps pipeline

```text
Git Push
   ↓
Gitleaks
   ↓
SonarQube
   ↓
Dependency-Check
   ↓
Tests
   ↓
Docker Build
   ↓
Trivy
   ↓
Test Environment
   ↓
OWASP ZAP
   ↓
Security Gate
   ↓
Deployment
```

---

# 10. Security Gate

## Security Gate

### Simple definition

A security gate is an automated decision point that determines whether the pipeline can continue.

### Engineer mental model

```text
Security Checks
      ↓
 Security Gate
      ↓
 ┌────┴────┐
PASS      FAIL
 ↓          ↓
Continue   Stop
```

### SecureOps example

If a configured critical vulnerability threshold is exceeded, the pipeline can fail.

---

# 11. SAST

## Static Application Security Testing

### Simple definition

SAST analyzes source code to find potential security problems without running the application.

### Engineer mental model

```text
Source Code
     ↓
SAST Tool
     ↓
Security Analysis
     ↓
Findings
```

### SecureOps tool

**SonarQube**

### SecureOps example

SonarQube analyzes Java/Spring Boot code before deployment.

---

# 12. SCA

## Software Composition Analysis

### Simple definition

SCA analyzes third-party libraries and dependencies for known vulnerabilities.

### Engineer mental model

Modern applications rarely contain only code written by the development team.

They depend on many external components.

```text
Application
     ↓
Dependencies
     ↓
Known Vulnerabilities?
```

### SecureOps tool

**OWASP Dependency-Check**

### SecureOps example

A vulnerable Spring dependency can be detected before deployment.

---

# 13. DAST

## Dynamic Application Security Testing

### Simple definition

DAST tests a running application for security weaknesses.

### Engineer mental model

```text
Running Application
        ↓
Security Scanner
        ↓
HTTP Requests
        ↓
Responses
        ↓
Security Findings
```

### SecureOps tool

**OWASP ZAP**

### Difference from SAST

```text
SAST → Source Code

DAST → Running Application
```

---

# 14. CVE

## Common Vulnerabilities and Exposures

### Simple definition

A CVE is a standardized identifier for a publicly known cybersecurity vulnerability.

### Example

A dependency may have a vulnerability identified by a CVE such as:

```text
CVE-YYYY-NNNNN
```

### Engineer mental model

Think of CVE as a unique reference number for a known vulnerability.

### SecureOps example

Dependency-Check may report that a project dependency is associated with a known CVE.

---

# 15. Vulnerability

## Vulnerability

### Simple definition

A vulnerability is a weakness that could be exploited to compromise security.

### Engineer mental model

```text
Weakness
   ↓
Can be exploited
   ↓
Potential security impact
```

### SecureOps example

An endpoint that allows unauthorized users to access administrative data may contain an authorization vulnerability.

---

# 16. Threat

## Threat

### Simple definition

A threat is a potential source of harm to a system or organization.

### Examples

* Attacker
* Malware
* Insider misuse
* Phishing
* Network attacker

### Engineer mental model

```text
Threat
   ↓
May exploit
   ↓
Vulnerability
```

---

# 17. Risk

## Risk

### Simple definition

Risk represents the possibility of a harmful event and its potential impact.

A simplified model is:

```text
Risk ≈ Likelihood × Impact
```

### Engineer mental model

A vulnerability does not automatically mean that the same level of risk exists everywhere.

You consider:

* How likely exploitation is
* What could happen
* Which assets are affected

### SecureOps example

A vulnerability affecting an administrative API may have greater impact than the same type of weakness in a non-sensitive test feature.

---

# 18. Exploit

## Exploit

### Simple definition

An exploit is a technique, code, or method used to take advantage of a vulnerability.

### Relationship

```text
Threat Actor
     ↓
Exploit
     ↓
Vulnerability
     ↓
Impact
```

### SecureOps example

A controlled test may demonstrate how an insecure authorization rule could allow access to a protected endpoint.

---

# 19. Remediation

## Remediation

### Simple definition

Remediation means fixing or reducing a security problem.

### Typical process

```text
Find Vulnerability
       ↓
Understand Cause
       ↓
Fix
       ↓
Test
       ↓
Verify
```

### SecureOps example

If a vulnerable dependency is discovered:

```text
Old Dependency
      ↓
Update Dependency
      ↓
Run Tests
      ↓
Run Security Scan
      ↓
Verify Finding Is Resolved
```

---

# 20. Authentication

## Authentication

### Simple definition

Authentication answers:

> **Who are you?**

### Examples

* Username + password
* MFA
* Passkey
* Certificate

### SecureOps example

A user logs into SecureOps with valid credentials.

---

# 21. Authorization

## Authorization

### Simple definition

Authorization answers:

> **What are you allowed to do?**

### Example

```text
Authentication
      ↓
Who are you?
      ↓
Authorization
      ↓
What can you access?
```

### SecureOps example

An EMPLOYEE may report an incident but should not manage system users.

---

# 22. Authentication vs Authorization

This distinction is fundamental.

```text
Authentication
     =
Who are you?

Authorization
     =
What can you do?
```

Example:

```text
User logs in
    ↓
Authentication
    ↓
User = employee01
    ↓
Authorization
    ↓
Role = EMPLOYEE
    ↓
Allowed actions are determined
```

---

# 23. RBAC

## Role-Based Access Control

### Simple definition

RBAC controls access based on user roles.

### SecureOps roles

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

### Engineer mental model

```text
User
 ↓
Role
 ↓
Permissions
 ↓
Resource
```

### SecureOps example

A SECURITY_ANALYST can investigate incidents while an EMPLOYEE cannot access administrative functions.

---

# 24. Least Privilege

## Least Privilege

### Simple definition

Least privilege means giving a user, application, or service only the permissions it needs.

### Example

Bad:

```text
Employee → Administrator permissions
```

Better:

```text
Employee
   ↓
Only employee-related permissions
```

### SecureOps example

A normal employee should not have permissions to modify system roles.

---

# 25. Defense in Depth

## Defense in Depth

### Simple definition

Defense in depth means using multiple security controls at different layers.

### Engineer mental model

```text
Source Code
    ↓
Dependencies
    ↓
Secrets
    ↓
Containers
    ↓
Application
    ↓
Network
    ↓
Monitoring
```

If one control misses a problem, another may detect it.

### SecureOps example

SecureOps uses:

```text
SonarQube
Dependency-Check
Gitleaks
Trivy
OWASP ZAP
Wazuh
```

---

# 26. Hardening

## Hardening

### Simple definition

Hardening means reducing unnecessary weaknesses and attack opportunities in a system.

### Examples

* Disable unnecessary services.
* Remove unused packages.
* Restrict permissions.
* Secure configurations.
* Close unnecessary ports.
* Use secure protocols.

### SecureOps example

A production container should contain only what the application needs.

---

# 27. Attack Surface

## Attack Surface

### Simple definition

The attack surface is the collection of points where an attacker could potentially interact with or attack a system.

### Examples

* Open ports
* APIs
* Login pages
* Network services
* Dependencies
* Containers
* Exposed credentials

### Engineer mental model

```text
More unnecessary exposure
        ↓
Larger attack surface
```

Hardening aims to reduce unnecessary attack surface.

---

# 28. Secret

## Secret

### Simple definition

A secret is sensitive information that should not be publicly exposed.

### Examples

* Password
* API key
* Access token
* Private key
* Database credential

### SecureOps example

Secrets should not be written directly into Git-tracked source code.

Instead:

```text
Application
     ↓
Environment Variable / Secret Store
     ↓
Secret
```

---

# 29. Secret Scanning

## Secret Scanning

### Simple definition

Secret scanning searches source code and Git history for accidentally exposed credentials.

### SecureOps tool

**Gitleaks**

### Example

```text
Git Push
   ↓
Gitleaks
   ↓
Secret detected
   ↓
Pipeline fails
```

---

# 30. Docker

## Docker

### Simple definition

Docker is a platform used to package and run applications in containers.

### Engineer mental model

A container packages:

```text
Application
+
Dependencies
+
Configuration
```

into a standardized runtime environment.

### SecureOps example

SecureOps components can be packaged into Docker containers.

---

# 31. Container

## Container

### Simple definition

A container is an isolated process environment used to run an application and its dependencies.

### Container vs Virtual Machine

Simplified:

```text
Virtual Machine
Application
Libraries
Guest OS
Virtual Hardware

Container
Application
Libraries
Container Runtime
Host OS
```

Containers are generally lighter than full virtual machines because they share the host operating system kernel.

---

# 32. Container Image

## Container Image

### Simple definition

A container image is a packaged template used to create containers.

### Example

```text
Dockerfile
    ↓
Docker Build
    ↓
Docker Image
    ↓
Container
```

### SecureOps example

Trivy scans the Docker image before it is deployed.

---

# 33. Trivy

## Trivy

### Simple definition

Trivy is a security scanner that can identify vulnerabilities in container images and other supported targets.

### SecureOps flow

```text
Docker Build
     ↓
Trivy
     ↓
Vulnerability Scan
     ↓
Security Decision
```

---

# 34. OWASP

## OWASP

### Simple definition

OWASP stands for:

**Open Worldwide Application Security Project**

It is a well-known organization and community focused on application security.

### SecureOps example

The project uses:

* OWASP ZAP
* OWASP Dependency-Check

OWASP also publishes application security guidance such as the OWASP Top 10.

---

# 35. Git

## Git

### Simple definition

Git is a distributed version control system.

It tracks changes to source code and allows developers to work with different versions of a project.

### SecureOps example

```text
Developer
    ↓
Git Commit
    ↓
Git Push
    ↓
GitHub
```

---

# 36. GitHub

## GitHub

### Simple definition

GitHub is a platform for hosting Git repositories and collaborating on software projects.

### SecureOps example

The SecureOps source code repository is hosted on GitHub.

GitHub also provides GitHub Actions for automation.

---

# 37. GitHub Actions

## GitHub Actions

### Simple definition

GitHub Actions is a workflow automation platform integrated with GitHub.

### SecureOps example

It can execute:

```text
Build
 ↓
Tests
 ↓
SAST
 ↓
SCA
 ↓
Secret Scan
 ↓
Docker Build
 ↓
Trivy
 ↓
DAST
```

---

# 38. PostgreSQL

## PostgreSQL

### Simple definition

PostgreSQL is a relational database management system.

### SecureOps example

SecureOps uses PostgreSQL to store information such as:

* Users
* Roles
* Employees
* Incidents
* Audit records

---

# 39. API

## API — Application Programming Interface

### Simple definition

An API is an interface that allows software components to communicate.

### SecureOps example

Angular communicates with Spring Boot through REST APIs.

```text
Angular
   ↓
HTTP Request
   ↓
Spring Boot REST API
   ↓
PostgreSQL
```

---

# 40. REST API

## REST API

### Simple definition

A REST API is an API architecture commonly used for communication over HTTP.

### Common HTTP methods

```text
GET     → Read
POST    → Create
PUT     → Update
PATCH   → Partial Update
DELETE  → Delete
```

### SecureOps example

```text
POST /api/incidents
```

may create a security incident.

---

# 41. HTTPS

## HTTPS

### Simple definition

HTTPS is HTTP protected using TLS.

It helps protect communication between clients and servers.

### Engineer mental model

```text
Client
   │
   │ Encrypted communication
   │
   ▼
Server
```

### SecureOps example

Production API communication should use HTTPS.

---

# 42. TLS

## TLS — Transport Layer Security

### Simple definition

TLS is a cryptographic protocol used to protect data transmitted over a network.

It provides important security properties such as:

* Confidentiality
* Integrity
* Authentication of the server through certificates

### SecureOps example

HTTPS uses TLS to protect web traffic.

---

# 43. Audit Log

## Audit Logging

### Simple definition

An audit log records important actions performed in a system.

### Example

```text
User: analyst01
Action: INCIDENT_UPDATED
Resource: INC-001
Time: 2026-09-16 14:20
Result: SUCCESS
```

### Why it matters

Audit logs help with:

* Investigation
* Accountability
* Troubleshooting
* Security monitoring
* Compliance

---

# 44. SIEM

## Security Information and Event Management

### Simple definition

A SIEM collects and analyzes security-related logs and events from different systems.

### Engineer mental model

```text
Servers
   +
Applications
   +
Network Devices
   +
Security Tools
      ↓
     SIEM
      ↓
Correlation / Analysis
      ↓
Alerts
      ↓
Investigation
```

### SecureOps example

Wazuh can be used to provide security monitoring and event analysis.

---

# 45. Monitoring

## Monitoring

### Simple definition

Monitoring means continuously observing systems and applications to understand their state and detect problems.

### SecureOps example

Monitoring may include:

* Application logs
* Authentication events
* System events
* Security alerts
* Container activity

---

# 46. Alert

## Security Alert

### Simple definition

An alert is a notification that a potentially important security event has been detected.

### Example

```text
Multiple failed logins
        ↓
Detection
        ↓
Alert
        ↓
Security Analyst
        ↓
Investigation
```

An alert is not automatically proof of an attack. It requires analysis.

---

# 47. Incident

## Security Incident

### Simple definition

A security incident is an event that may compromise or threaten the confidentiality, integrity, or availability of information or systems.

### SecureOps example

A suspicious authentication event may be recorded and investigated as a security incident.

---

# 48. Incident Response

## Incident Response

### Simple definition

Incident response is the structured process used to detect, investigate, contain, and recover from security incidents.

### Simplified lifecycle

```text
Detect
  ↓
Analyze
  ↓
Contain
  ↓
Eradicate
  ↓
Recover
  ↓
Learn
```

### SecureOps example

The SECURITY_ANALYST investigates reported incidents and records investigation information.

---

# 49. Logging

## Logging

### Simple definition

Logging means recording events generated by an application or system.

### Examples

```text
INFO
WARNING
ERROR
SECURITY EVENT
```

### Difference from audit logging

General logs help understand application behavior.

Audit logs specifically record important user and administrative actions for accountability and investigation.

---

# 50. False Positive

## False Positive

### Simple definition

A false positive occurs when a security tool reports a problem that is not actually a security issue in the given context.

### Example

```text
Security Scanner
      ↓
Reports Finding
      ↓
Investigation
      ↓
Not actually exploitable
```

Security analysts must validate findings instead of blindly treating every alert as a confirmed attack.

---

# 51. False Negative

## False Negative

### Simple definition

A false negative occurs when a real security problem exists but a security tool fails to detect it.

### Engineer mental model

```text
Real Vulnerability
       ↓
Scanner
       ↓
No Finding
```

This is one reason why multiple security controls are useful.

---

# 52. Security Finding

## Security Finding

### Simple definition

A security finding is a result reported by a security test or analysis.

A finding may represent:

* Vulnerability
* Misconfiguration
* Exposed secret
* Vulnerable dependency
* Suspicious behavior

### SecureOps example

```text
Trivy
 ↓
Critical vulnerability
 ↓
Finding
 ↓
Investigation
 ↓
Remediation
```

---

# 53. Severity

## Severity

### Simple definition

Severity describes how serious a security finding could be.

A simplified classification may be:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

Severity is not exactly the same as risk.

Risk depends on the specific environment, likelihood, exposure, and impact.

---

# 54. Remediation Verification

## Remediation Verification

### Simple definition

Remediation verification means checking that a security fix actually solved the original problem.

### Engineer mental model

```text
Finding
   ↓
Fix
   ↓
Test
   ↓
Rescan
   ↓
Finding Resolved?
```

### SecureOps example

After updating a vulnerable dependency:

```text
Dependency-Check
      ↓
Before → Vulnerable
      ↓
Update Dependency
      ↓
After → No vulnerable finding
```

---

# 55. Security Baseline

## Security Baseline

### Simple definition

A security baseline is a defined minimum security configuration or standard that a system should satisfy.

### Example

A baseline may require:

* No hardcoded secrets
* No critical vulnerabilities
* Required security tests passing
* Restricted permissions
* Secure configuration

---

# 56. Supply Chain Security

## Software Supply Chain Security

### Simple definition

Software supply chain security protects the external components, dependencies, tools, and processes used to build software.

### Engineer mental model

Your application is not only your own code.

```text
Your Code
   +
Dependencies
   +
Build Tools
   +
Container Base Image
   +
CI/CD
   =
Software Supply Chain
```

### SecureOps example

Dependency-Check and Trivy help identify vulnerabilities in external components.

---

# 57. Security Automation

## Security Automation

### Simple definition

Security automation means using software to automatically perform security checks or security actions.

### SecureOps example

Instead of manually checking every commit:

```text
Developer Push
      ↓
GitHub Actions
      ↓
Security Tools
      ↓
Automated Results
```

This makes security repeatable and scalable.

---

# 58. Continuous Security

## Continuous Security

### Simple definition

Continuous Security means continuously applying security checks and monitoring throughout the software lifecycle.

### Engineer mental model

Security is not:

```text
Security Test → Done
```

Instead:

```text
Develop
 ↓
Test
 ↓
Secure
 ↓
Deploy
 ↓
Monitor
 ↓
Detect
 ↓
Improve
 ↓
Develop Again
```

---

# 59. Secure Coding

## Secure Coding

### Simple definition

Secure coding means writing software in a way that reduces security vulnerabilities.

### Examples

* Validate input
* Use parameterized queries
* Protect authentication
* Enforce authorization
* Avoid hardcoded secrets
* Handle errors safely
* Apply least privilege

### SecureOps example

The backend validates authorization before allowing access to protected resources.

---

# 60. Final Mental Model

The most important concepts in this project can be connected into one model:

```text
                    SECURE SOFTWARE

                         │
                         ▼
                  Secure Development
                         │
                         ▼
                     Git / GitHub
                         │
                         ▼
                    CI/CD Pipeline
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
        SAST             SCA        Secret Scan
     SonarQube     Dependency-Check   Gitleaks
          │              │              │
          └──────────────┼──────────────┘
                         ▼
                  Automated Tests
                         │
                         ▼
                    Docker Build
                         │
                         ▼
                       Trivy
                         │
                         ▼
                  Test Environment
                         │
                         ▼
                     OWASP ZAP
                         │
                         ▼
                   Security Gate
                         │
                  ┌──────┴──────┐
                  ▼             ▼
                FAIL           PASS
                  │             │
                  ▼             ▼
                 STOP        DEPLOY
                                │
                                ▼
                            Monitoring
                                │
                                ▼
                              Wazuh
                                │
                                ▼
                         Security Events
                                │
                                ▼
                         Investigation
                                │
                                ▼
                            Remediation
                                │
                                └──────────► Continuous Improvement
```

The central idea to remember is:

> **DevSecOps is not about adding security tools to a pipeline. It is about making security a continuous part of how software is developed, tested, delivered, deployed, and operated.**

---

# 61. Quick Revision Table

| Concept               | Remember It As                                  |
| --------------------- | ----------------------------------------------- |
| SDLC                  | Complete software lifecycle                     |
| DevOps                | Development + Operations                        |
| DevSecOps             | Development + Security + Operations             |
| Shift Left            | Security earlier                                |
| CI                    | Automatically build and test changes            |
| CD                    | Automatically deliver/deploy software           |
| SAST                  | Test source code                                |
| SCA                   | Test dependencies                               |
| DAST                  | Test running application                        |
| CVE                   | Identifier for known vulnerability              |
| Vulnerability         | Security weakness                               |
| Threat                | Potential source of harm                        |
| Risk                  | Likelihood + potential impact                   |
| Exploit               | Method used to exploit a vulnerability          |
| Remediation           | Fix the problem                                 |
| Authentication        | Who are you?                                    |
| Authorization         | What can you do?                                |
| RBAC                  | Access based on roles                           |
| Least Privilege       | Only required permissions                       |
| Defense in Depth      | Multiple security layers                        |
| Hardening             | Reduce unnecessary weaknesses                   |
| Secret                | Sensitive credential/information                |
| Docker                | Container platform                              |
| Trivy                 | Container/security scanner                      |
| OWASP ZAP             | DAST/web security testing                       |
| Security Gate         | Allow or stop pipeline                          |
| Audit Log             | Record important actions                        |
| SIEM                  | Collect/analyze security events                 |
| Monitoring            | Continuously observe systems                    |
| Incident              | Security event requiring investigation/response |
| False Positive        | Reported issue that isn't actually a problem    |
| False Negative        | Real problem not detected                       |
| Security Finding      | Result from security analysis                   |
| Supply Chain Security | Protect software components/processes           |
| Security Automation   | Automate security activities                    |
| Continuous Security   | Security throughout the lifecycle               |

---

# 62. Core Concepts to Memorize First

If time is limited, prioritize these concepts:

```text
1. DevSecOps
2. SDLC
3. CI/CD
4. Shift Left
5. SAST
6. SCA
7. DAST
8. Vulnerability
9. CVE
10. Risk
11. Authentication
12. Authorization
13. RBAC
14. Least Privilege
15. Defense in Depth
16. Security Gate
17. Docker
18. Container Security
19. SIEM
20. Incident Response
```

These concepts form the mental foundation for understanding the SecureOps Platform.

---

# 63. One-Sentence Project Mental Model

The entire project can be summarized as:

> **Build a real web application, secure it from the beginning, automatically test its security throughout the CI/CD pipeline, block insecure releases, deploy safely, and continuously monitor the system after deployment.**
