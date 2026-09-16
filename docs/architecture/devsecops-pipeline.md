# SecureOps Platform — DevSecOps Pipeline Architecture

## 1. Overview

SecureOps Platform follows a **DevSecOps pipeline** that integrates security throughout the software development lifecycle.

The objective is to make security an automated and continuous part of development, testing, deployment, and operations.

Instead of waiting until the application is finished to perform security testing, security checks are introduced at multiple stages of the pipeline.

The main pipeline is:

```text
Developer
    │
    ▼
Git Repository
    │
    ▼
Continuous Integration
    │
    ├── SAST
    ├── SCA
    ├── Secret Scanning
    └── Automated Tests
    │
    ▼
Application Build
    │
    ▼
Container Build
    │
    └── Container Security Scan
    │
    ▼
Test Environment
    │
    └── DAST
    │
    ▼
Security Gate
    │
    ├── FAIL → Stop Pipeline
    │
    └── PASS → Deploy
                  │
                  ▼
              Production
                  │
                  ▼
          Monitoring & Logging
                  │
                  ▼
                 SIEM
```

---

# 2. DevSecOps Lifecycle

The project follows a continuous lifecycle:

```text
PLAN
  │
  ▼
CODE
  │
  ▼
BUILD
  │
  ▼
TEST
  │
  ▼
SECURITY TEST
  │
  ▼
RELEASE
  │
  ▼
DEPLOY
  │
  ▼
OPERATE
  │
  ▼
MONITOR
  │
  └──────────────→ FEEDBACK → PLAN
```

Security is integrated into each relevant phase.

---

# 3. Source Code Management

The source code will be stored in a GitHub repository.

Git is used for:

* Version control
* Change tracking
* Collaboration
* Code history
* Branch management
* Code review

GitHub will host the repository and trigger the CI/CD pipeline when relevant changes are pushed.

The repository will contain:

```text
secureops-platform/
│
├── frontend/
├── backend/
├── infrastructure/
├── tests/
│
├── docs/
│
├── .github/
│   └── workflows/
│
├── README.md
├── LICENSE
└── .gitignore
```

---

# 4. Continuous Integration

**Continuous Integration (CI)** means automatically building and testing the application when developers push changes to the repository.

The CI pipeline will be implemented using GitHub Actions.

A simplified workflow is:

```text
Git Push
   │
   ▼
GitHub Actions
   │
   ├── Checkout Source Code
   │
   ├── Install Dependencies
   │
   ├── Run Security Checks
   │
   ├── Run Tests
   │
   └── Build Application
```

The purpose is to detect problems as early as possible.

---

# 5. SAST — Static Application Security Testing

**SAST** analyzes source code without running the application.

The initial SAST tool selected for the project is:

**SonarQube**

SAST will be used to identify potential problems such as:

* Security vulnerabilities
* Dangerous coding patterns
* Code quality problems
* Bugs
* Maintainability issues

The simplified process is:

```text
Source Code
     │
     ▼
   SAST
     │
     ▼
Security Analysis
     │
     ├── Problems Found
     │
     └── No Critical Problems
```

SAST is part of the **Shift Left** security approach because source-code analysis happens early in the lifecycle.

---

# 6. SCA — Software Composition Analysis

Modern applications depend on external libraries and packages.

**Software Composition Analysis (SCA)** is used to identify security problems in these dependencies.

The initial SCA tool selected for the project is:

**OWASP Dependency-Check**

The process is:

```text
Application Dependencies
          │
          ▼
     Dependency-Check
          │
          ▼
   Vulnerability Analysis
          │
          ▼
      CVE Detection
```

The objective is to identify dependencies with known vulnerabilities.

A **CVE** is a public identifier assigned to a known cybersecurity vulnerability.

---

# 7. Secret Scanning

Secrets such as passwords, API keys, tokens, and private keys must not be committed to the repository.

The project will use:

**Gitleaks**

Gitleaks scans the repository for potentially exposed secrets.

The workflow is:

```text
Developer
    │
    ▼
Git Commit / Push
    │
    ▼
Gitleaks
    │
    ├── Secret Detected → FAIL
    │
    └── No Secret → CONTINUE
```

Example secrets that must never be committed:

```text
DATABASE_PASSWORD
API_KEY
JWT_SECRET
PRIVATE_KEY
ACCESS_TOKEN
```

Real credentials will never be stored directly in the public repository.

---

# 8. Automated Testing

The pipeline will execute automated tests before deployment.

Testing will include:

* Unit tests
* Integration tests
* API tests
* Security-related tests

The basic flow is:

```text
Source Code
     │
     ▼
Build
     │
     ▼
Automated Tests
     │
     ├── FAIL → Stop Pipeline
     │
     └── PASS → Continue
```

Automated tests provide confidence that new changes do not break existing functionality.

---

# 9. Application Build

After security checks and automated tests pass, the application will be built.

The project contains two main application components:

```text
Angular Frontend
       +
Spring Boot Backend
       │
       ▼
Application Build
```

The build process converts source code into deployable application artifacts.

---

# 10. Containerization

Docker will be used to package the application into containers.

The planned containers include:

```text
Docker Environment
│
├── Angular Frontend
│
├── Spring Boot Backend
│
└── PostgreSQL
```

Docker provides a consistent environment between development, testing, and deployment.

Docker Compose may be used to run the application components together during development and testing.

---

# 11. Container Security

Building a Docker image does not automatically make the image secure.

The project will use:

**Trivy**

Trivy will scan container images for security issues.

The workflow is:

```text
Dockerfile
    │
    ▼
Docker Image
    │
    ▼
   Trivy
    │
    ├── Vulnerability Found → FAIL
    │
    └── Acceptable Result → CONTINUE
```

The scan can identify vulnerabilities in packages and components included in the container image.

---

# 12. Test Environment

Before production deployment, the application will run in a controlled test environment.

The test environment allows security testing against the running application.

The environment may contain:

```text
Test Environment
│
├── Frontend
├── Backend
└── Database
```

This environment should be isolated from real production data.

---

# 13. DAST — Dynamic Application Security Testing

**DAST** tests a running application from the outside.

The initial DAST tool selected for the project is:

**OWASP ZAP**

Unlike SAST, DAST does not primarily analyze source code.

Instead, it interacts with the running application.

```text
Running Application
        │
        ▼
     OWASP ZAP
        │
        ▼
Security Testing
        │
        ├── Potential Issue
        │
        └── No Detected Issue
```

DAST can help identify problems related to the behavior of the running web application.

---

# 14. Security Gate

The **Security Gate** is a decision point in the pipeline.

It determines whether the application can continue toward deployment.

Example:

```text
                Security Checks
                       │
          ┌────────────┴────────────┐
          │                         │
       PASS                       FAIL
          │                         │
          ▼                         ▼
      Continue                 Stop Pipeline
          │
          ▼
       Deploy
```

Possible conditions include:

* Critical SAST vulnerability
* Critical dependency vulnerability
* Exposed secret
* Critical container vulnerability
* Failed security test
* Failed automated tests

The exact thresholds will be defined and implemented during the project.

---

# 15. Continuous Delivery / Deployment

After the required checks pass, the application can move toward deployment.

The planned flow is:

```text
Security Gate
      │
      ▼
Deployment
      │
      ▼
Application Environment
```

The project will initially focus on a controlled laboratory or development environment rather than a real production organization.

This allows security testing without exposing real users or sensitive data.

---

# 16. Secrets Management

Sensitive configuration must be separated from application source code.

Examples include:

* Database passwords
* JWT secrets
* API keys
* Service credentials

The initial approach will use:

**GitHub Actions Secrets / environment variables**

The principle is:

```text
Application Code
       X
       │
       │ No hard-coded secrets
       │
       ▼
Environment / Secret Store
       │
       ▼
Application
```

A dedicated secrets-management system such as HashiCorp Vault may be considered as a future improvement.

---

# 17. Production Security

Before deployment, the environment should be configured securely.

Security considerations include:

* Secure configuration
* Minimal exposed services
* Strong authentication
* Least privilege
* Secure network configuration
* HTTPS
* Secure environment variables
* Logging
* Monitoring
* Container security

The objective is to reduce the application's attack surface.

**Attack surface** means the collection of possible points through which an attacker could attempt to interact with or compromise a system.

---

# 18. Logging and Monitoring

After deployment, the application must continue to be monitored.

Important events include:

* Authentication attempts
* Failed logins
* Authorization failures
* Administrative actions
* Application errors
* Security events
* Container events
* System events

The application will generate logs that can later be collected and analyzed.

---

# 19. SIEM Integration

The project may integrate with a SIEM platform such as **Wazuh** for security monitoring.

SIEM means:

**Security Information and Event Management**

A SIEM collects and analyzes security-related events from different systems.

The planned architecture is:

```text
Application
     │
     ▼
   Logs
     │
     ▼
SIEM / Wazuh
     │
     ├── Detection
     ├── Correlation
     └── Alerting
```

This connects the DevSecOps project with security operations.

---

# 20. Feedback Loop

DevSecOps is not a one-time security check.

Security findings should create a feedback loop:

```text
Production
    │
    ▼
Monitoring
    │
    ▼
Security Finding
    │
    ▼
Investigation
    │
    ▼
Remediation
    │
    ▼
Code Change
    │
    ▼
CI/CD Pipeline
    │
    ▼
Security Testing
    │
    ▼
Deployment
```

This creates continuous improvement.

---

# 21. Complete DevSecOps Pipeline

The complete planned pipeline is:

```text
                         DEVELOPER
                             │
                             ▼
                     ┌──────────────┐
                     │    GitHub    │
                     └──────┬───────┘
                            │
                         Git Push
                            │
                            ▼
                  ┌────────────────────┐
                  │   GitHub Actions   │
                  └─────────┬──────────┘
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
           SAST             SCA        Gitleaks
             │              │              │
             └──────────────┼──────────────┘
                            │
                            ▼
                    Automated Tests
                            │
                            ▼
                      Build Application
                            │
                            ▼
                      Build Docker Image
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
                     SECURITY GATE
                       │          │
                     FAIL        PASS
                       │          │
                       ▼          ▼
                     STOP      DEPLOY
                                  │
                                  ▼
                              Production
                                  │
                       ┌──────────┴──────────┐
                       │                     │
                       ▼                     ▼
                    Logging              Monitoring
                       │                     │
                       └──────────┬──────────┘
                                  ▼
                              SIEM / Wazuh
                                  │
                                  ▼
                              Detection
                                  │
                                  ▼
                            Investigation
                                  │
                                  ▼
                             Remediation
                                  │
                                  └──────→ Development
```

---

# 22. Security Tool Summary

| Security Area      | Tool                   | Main Purpose                     |
| ------------------ | ---------------------- | -------------------------------- |
| CI/CD              | GitHub Actions         | Automate pipeline                |
| SAST               | SonarQube              | Analyze source code              |
| SCA                | OWASP Dependency-Check | Analyze dependencies             |
| Secret Scanning    | Gitleaks               | Detect exposed secrets           |
| Container Security | Trivy                  | Scan container images            |
| DAST               | OWASP ZAP              | Test running application         |
| Monitoring / SIEM  | Wazuh                  | Security monitoring and alerting |

---

# 23. Expected Pipeline Behavior

The pipeline should automatically stop when a configured critical security condition is detected.

Example:

```text
Developer pushes code
        │
        ▼
Security Checks
        │
        ▼
Critical vulnerability detected
        │
        ▼
Security Gate = FAIL
        │
        ▼
Deployment blocked
        │
        ▼
Developer receives feedback
        │
        ▼
Vulnerability fixed
        │
        ▼
Code pushed again
        │
        ▼
Pipeline runs again
```

This demonstrates an important DevSecOps principle:

> Security is part of the delivery process, not a separate activity performed after deployment.

---

# 24. Future Improvements

The initial pipeline may be extended with:

* Branch protection
* Pull request security checks
* Code review requirements
* Advanced secrets management
* Infrastructure as Code security scanning
* Kubernetes security
* Software Bill of Materials (SBOM)
* Advanced vulnerability management
* Security incident automation
* SIEM alert automation
* Compliance checks
* Security dashboards

These improvements will be considered after the core DevSecOps pipeline is functional.

---

# 25. Project Goal

The final objective is to demonstrate a complete practical DevSecOps workflow:

```text
Develop
   ↓
Test
   ↓
Secure
   ↓
Build
   ↓
Scan
   ↓
Deploy
   ↓
Monitor
   ↓
Detect
   ↓
Respond
   ↓
Improve
   ↓
Develop Again
```

The project is designed to demonstrate that application security can be integrated continuously throughout the software development and operational lifecycle.
