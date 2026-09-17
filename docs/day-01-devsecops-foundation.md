# Day 01 — DevSecOps Foundation

## 1. Objective

The objective of Day 1 was to define the **SecureOps Platform** project and establish its DevSecOps foundation.

The main goals were:

* Understand the project requirements
* Define the application scope
* Define the system architecture
* Understand the DevSecOps lifecycle
* Select appropriate security tools
* Define the CI/CD security pipeline
* Establish the documentation structure

The goal was not to build the application yet.

The goal was to first understand:

> **What are we building, how will it work, and how will security be integrated throughout development?**

---

## 2. Project Overview

The project is called:

**SecureOps Platform**

It is a secure enterprise web application designed to manage:

* Employees
* Security incidents
* Incident investigations
* Reports
* Audit logs
* Security operations

The platform will contain different user roles:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

Each role will have different permissions.

The project is designed as a realistic example of a secure software development lifecycle.

---

## 3. Main Objective

The central objective is to build a web application while integrating security into the complete development lifecycle.

Instead of adding security only at the end:

```text
Development
     ↓
Testing
     ↓
Deployment
     ↓
Security
```

the project follows a DevSecOps approach:

```text
Development
     ↓
Security
     ↓
Testing
     ↓
Build
     ↓
Security
     ↓
Deployment
     ↓
Monitoring
     ↓
Security Feedback
     ↓
Development
```

Security becomes a continuous activity.

---

## 4. DevSecOps

### What is DevSecOps?

**DevSecOps = Development + Security + Operations**

DevSecOps is an approach where security is integrated into the software development and deployment process.

The main idea is:

> **Security should be everyone's responsibility.**

Developers, security engineers, and operations teams work together.

---

## 5. Traditional Security vs DevSecOps

### Traditional approach

```text
Develop
   ↓
Test
   ↓
Deploy
   ↓
Security Check
```

Security is often performed late.

This can make vulnerabilities expensive and difficult to fix.

### DevSecOps approach

```text
Plan
 ↓
Develop
 ↓
Security Testing
 ↓
Build
 ↓
Security Testing
 ↓
Deploy
 ↓
Monitor
 ↓
Improve
```

Security is integrated throughout the lifecycle.

---

## 6. Shift Left

One important DevSecOps concept learned during Day 1 was:

**Shift Left**

Shift Left means performing security checks earlier in the development lifecycle.

For example:

```text
Developer writes code
        ↓
SAST
        ↓
Dependency Scan
        ↓
Secret Scan
        ↓
Build
```

Instead of discovering a vulnerability after production deployment, the pipeline attempts to detect it earlier.

### Example

A developer accidentally commits a secret:

```text
github_token = "SECRET123"
```

A secret scanning tool such as **Gitleaks** can detect the secret before the application is deployed.

---

## 7. SDLC

The project follows the concept of the **Software Development Life Cycle (SDLC)**.

The simplified lifecycle is:

```text
Plan
 ↓
Design
 ↓
Develop
 ↓
Test
 ↓
Deploy
 ↓
Operate
 ↓
Monitor
 ↓
Improve
```

Security activities are integrated into each stage.

---

## 8. CI/CD

### Continuous Integration

**CI** means developers frequently integrate their code into a shared repository.

Example:

```text
Developer
   ↓
Git Push
   ↓
GitHub
   ↓
GitHub Actions
   ↓
Build
   ↓
Tests
```

### Continuous Delivery / Deployment

After the application passes the required checks, it can continue toward deployment.

```text
Build
 ↓
Security Checks
 ↓
Test
 ↓
Deploy
```

CI/CD allows automation of these processes.

---

## 9. DevSecOps Pipeline

The initial SecureOps Platform pipeline was designed as:

```text
Developer
    ↓
Git Push
    ↓
GitHub
    ↓
GitHub Actions
    ↓
┌───────────────────────────┐
│ Security Checks           │
│                           │
│ SAST                      │
│ SCA                       │
│ Secret Scanning           │
└───────────────────────────┘
    ↓
Build
    ↓
Tests
    ↓
Docker Build
    ↓
Container Security Scan
    ↓
Test Environment
    ↓
DAST
    ↓
Security Gate
    ↓
Deploy
    ↓
Monitoring / Logging
    ↓
SIEM
    ↓
Security Feedback
```

This architecture will be implemented progressively during the project.

---

## 10. Security Tools

Several security tools were selected for different security purposes.

| Security Area      | Tool                   |
| ------------------ | ---------------------- |
| SAST               | SonarQube              |
| SCA                | OWASP Dependency-Check |
| Secret Scanning    | Gitleaks               |
| Container Scanning | Trivy                  |
| DAST               | OWASP ZAP              |
| SIEM / Monitoring  | Wazuh                  |
| CI/CD              | GitHub Actions         |
| Containerization   | Docker                 |

---

## 11. SAST

**SAST = Static Application Security Testing**

SAST analyzes application source code without running the application.

Example:

```text
Source Code
     ↓
SonarQube
     ↓
Security Analysis
     ↓
Vulnerabilities / Code Issues
```

SAST can help identify insecure coding patterns.

---

## 12. SCA

**SCA = Software Composition Analysis**

Modern applications use external libraries and dependencies.

For example:

```text
Spring Boot
   ↓
Maven Dependencies
   ↓
External Libraries
```

A dependency may contain a known vulnerability.

SCA tools can identify vulnerable dependencies.

The selected tool is:

**OWASP Dependency-Check**

---

## 13. Secret Scanning

Secrets should never be committed to source control.

Examples of secrets include:

```text
Passwords
API Keys
Access Tokens
Private Keys
Database Credentials
```

The selected tool is:

**Gitleaks**

Example:

```text
Developer
    ↓
Git Commit
    ↓
Gitleaks
    ↓
Secret detected?
   ↙       ↘
 YES       NO
 ↓          ↓
BLOCK      CONTINUE
```

---

## 14. Container Security

The application will later be packaged into Docker containers.

Containers must also be scanned for vulnerabilities.

The selected tool is:

**Trivy**

Example:

```text
Docker Image
     ↓
Trivy
     ↓
OS vulnerabilities
+
Library vulnerabilities
+
Misconfigurations
```

---

## 15. DAST

**DAST = Dynamic Application Security Testing**

DAST tests a running application from the outside.

Unlike SAST:

```text
SAST
Source Code
   ↓
Analysis
```

DAST works with:

```text
Running Application
       ↓
Security Scanner
       ↓
Potential Vulnerabilities
```

The selected DAST tool is:

**OWASP ZAP**

---

## 16. Security Gate

A **Security Gate** is a decision point in the pipeline.

For example:

```text
Security Scan
     ↓
Are critical vulnerabilities detected?
     ↓
   ┌───────┐
   │       │
  YES      NO
   │       │
 BLOCK   CONTINUE
```

The purpose is to prevent insecure software from automatically continuing through the pipeline.

The exact thresholds will be defined later.

---

## 17. Application Architecture

The planned application architecture contains several main components:

```text
                 Users
                   │
                   ↓
          ┌─────────────────┐
          │ Angular Frontend│
          └────────┬────────┘
                   │
                   ↓
          ┌─────────────────┐
          │ Spring Boot API │
          └────────┬────────┘
                   │
                   ↓
          ┌─────────────────┐
          │   PostgreSQL    │
          └─────────────────┘
```

Security and DevSecOps components surround the application:

```text
                    GitHub
                       │
                       ↓
               GitHub Actions
                       │
       ┌───────────────┼───────────────┐
       ↓               ↓               ↓
     SAST             SCA        Secret Scan
       │               │               │
       └───────────────┼───────────────┘
                       ↓
                    Build
                       ↓
                  Docker Image
                       ↓
                    Trivy
                       ↓
                Test Environment
                       ↓
                     ZAP
                       ↓
                Security Gate
                       ↓
                  Deployment
                       ↓
                Monitoring / SIEM
```

---

## 18. Repository Structure

The project documentation was organized as:

```text
secureops-platform/
│
├── README.md
├── LICENSE
├── .gitignore
│
└── docs/
    ├── glossary.md
    ├── project-definition.md
    │
    ├── architecture/
    │   ├── application-architecture.md
    │   └── devsecops-pipeline.md
    │
    └── security/
        └── tool-selection.md
```

This structure separates:

* Project definition
* Architecture
* Security documentation
* Technical terminology

---

## 19. Important Concepts Learned

During Day 1, the following concepts were introduced:

| Concept         | Meaning                                       |
| --------------- | --------------------------------------------- |
| DevSecOps       | Integrating security into DevOps              |
| SDLC            | Software Development Life Cycle               |
| CI/CD           | Automated integration and delivery            |
| Shift Left      | Moving security earlier                       |
| SAST            | Static code security analysis                 |
| SCA             | Dependency security analysis                  |
| DAST            | Testing a running application                 |
| Security Gate   | Decision point that can block insecure builds |
| CVE             | Identifier for a known vulnerability          |
| Hardening       | Reducing unnecessary security weaknesses      |
| Least Privilege | Giving only required permissions              |
| Remediation     | Fixing a security problem                     |

---

## 20. Day 1 Result

Day 1 established the conceptual and architectural foundation of the SecureOps Platform.

The project now has:

```text
Project Definition
       ↓
Application Architecture
       ↓
DevSecOps Architecture
       ↓
Security Tool Selection
       ↓
Development Plan
```

The most important principle established during Day 1 is:

> **Security is integrated into the application lifecycle from the beginning, not added at the end.**

Day 1 prepared the project for actual application development.

Day 2 then implemented the first full-stack foundation using:

```text
Angular
   ↓
Spring Boot
   ↓
PostgreSQL
   ↓
Docker
```

The next phase is to progressively add application security and DevSecOps automation.
