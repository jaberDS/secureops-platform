# SecureOps Platform — Security Tool Selection

## 1. Overview

Security is integrated into SecureOps Platform throughout the software development lifecycle.

Instead of relying on a single security tool, the project uses multiple tools that address different security risks.

The main security approach is:

```text
Source Code
     ↓
SAST
     ↓
Dependency Security
     ↓
Secret Detection
     ↓
Automated Tests
     ↓
Container Security
     ↓
DAST
     ↓
Security Gate
     ↓
Deployment
     ↓
Monitoring
```

Each tool has a specific responsibility.

The objective is to apply the principle of **defense in depth**, meaning that multiple security controls protect the application at different stages.

---

# 2. Tool Selection Principles

Security tools were selected according to several criteria:

* Relevance to the project
* Compatibility with the technology stack
* Automation capability
* CI/CD integration
* Open-source or accessible licensing
* Community adoption
* Learning value
* Ability to generate useful security findings
* Suitability for a student DevSecOps project

The project prioritizes tools that can be integrated into an automated pipeline.

---

# 3. Security Tool Overview

| Security Area      | Tool                    | Main Purpose                        | Pipeline Stage   |
| ------------------ | ----------------------- | ----------------------------------- | ---------------- |
| SAST               | SonarQube               | Analyze source code                 | Development / CI |
| SCA                | OWASP Dependency-Check  | Detect vulnerable dependencies      | CI               |
| Secret Scanning    | Gitleaks                | Detect exposed secrets              | Development / CI |
| Testing            | JUnit / Angular Testing | Validate application behavior       | CI               |
| Container Security | Trivy                   | Scan Docker images                  | CI               |
| DAST               | OWASP ZAP               | Test the running application        | Acceptance       |
| CI/CD              | GitHub Actions          | Automate the pipeline               | All stages       |
| Containerization   | Docker                  | Package applications consistently   | Build            |
| Monitoring / SIEM  | Wazuh                   | Collect and analyze security events | Operations       |

---

# 4. SonarQube — SAST

## 4.1 What is SonarQube?

SonarQube is a code analysis platform used to identify problems in source code.

It can detect:

* Bugs
* Vulnerabilities
* Security weaknesses
* Code smells
* Maintainability problems

In this project, SonarQube is used as the main **SAST** tool.

SAST means:

**Static Application Security Testing**

Static means that the application does not need to be running during the analysis.

The tool analyzes the source code itself.

---

## 4.2 Why SonarQube?

SonarQube was selected because:

* It supports Java and other common technologies.
* It can be integrated into CI/CD pipelines.
* It provides structured analysis results.
* It helps identify security problems early.
* It is useful for learning secure coding practices.

---

## 4.3 Where is it used?

SonarQube is used during development and CI.

```text
Developer
    ↓
Git Push
    ↓
GitHub Actions
    ↓
SonarQube
    ↓
Source Code Analysis
    ↓
Security / Quality Result
```

---

## 4.4 Security Objective

The objective is to detect security weaknesses as early as possible.

This follows the **Shift Left** principle.

Shift Left means moving security checks earlier in the development lifecycle.

---

# 5. OWASP Dependency-Check — SCA

## 5.1 What is Dependency-Check?

OWASP Dependency-Check is a tool used to identify known vulnerabilities in third-party dependencies.

Modern applications use many external libraries.

For example:

```text
Spring Boot
   ↓
Spring Security
   ↓
Jackson
   ↓
Other Libraries
```

A vulnerability in one dependency can affect the entire application.

---

## 5.2 SCA

SCA means:

**Software Composition Analysis**

SCA focuses on the components and dependencies used by an application.

The tool can identify dependencies associated with known vulnerabilities, commonly referenced using **CVE** identifiers.

CVE means:

**Common Vulnerabilities and Exposures**

It is a standardized identifier for publicly known vulnerabilities.

Example:

```text
Application
    ↓
Dependency
    ↓
Known Vulnerability
    ↓
CVE
    ↓
Security Finding
```

---

## 5.3 Why Dependency-Check?

It was selected because:

* The application uses many third-party dependencies.
* Dependency vulnerabilities are a common application security risk.
* It can be integrated into CI/CD.
* It helps demonstrate software supply-chain security.

---

# 6. Gitleaks — Secret Scanning

## 6.1 What is Gitleaks?

Gitleaks is a tool used to detect secrets that may accidentally appear in source code or Git history.

Examples of secrets include:

* API keys
* Passwords
* Access tokens
* Private keys
* Database credentials

Example of an insecure situation:

```text
application.properties

database.password=MySecretPassword
```

This should never be committed to a public repository.

---

## 6.2 Why Gitleaks?

Gitleaks was selected because accidentally exposed secrets can create serious security risks.

The tool can scan repositories and identify patterns that look like credentials or other sensitive information.

---

## 6.3 Pipeline Position

Gitleaks should run early in the pipeline.

```text
Git Push
    ↓
Gitleaks
    ↓
Secret Found?
   ↙      ↘
 YES       NO
  ↓         ↓
FAIL      Continue
```

If a real secret is detected, the pipeline should stop.

---

# 7. JUnit and Automated Tests

## 7.1 Purpose

Automated tests verify that the application behaves correctly.

Testing is not itself a security scanner, but it is an important security foundation.

A broken security function can create a vulnerability.

---

## 7.2 Examples

Tests may verify:

* Authentication
* Authorization
* User creation
* Incident creation
* Role restrictions
* Input validation
* API behavior

Example:

```text
Employee requests ADMIN endpoint
            ↓
Authorization check
            ↓
Access denied
            ↓
Test passes
```

---

# 8. Docker — Containerization

## 8.1 What is Docker?

Docker packages an application and its dependencies into a container.

A container provides a consistent environment for running software.

Example:

```text
Angular
   ↓
Container

Spring Boot
   ↓
Container

PostgreSQL
   ↓
Container
```

Docker is not primarily a security scanning tool.

It provides the packaging and execution environment that other security tools can analyze.

---

## 8.2 Why Docker?

Docker was selected because:

* It provides reproducible environments.
* It simplifies deployment.
* It integrates well with CI/CD.
* It is widely used in modern DevOps environments.
* It allows container security to be demonstrated.

---

# 9. Trivy — Container Security

## 9.1 What is Trivy?

Trivy is a security scanner commonly used to identify vulnerabilities in container images and related components.

For example:

```text
Docker Image
     ↓
Trivy Scan
     ↓
Operating System Packages
     +
Application Dependencies
     ↓
Vulnerabilities
```

---

## 9.2 Why Trivy?

Trivy was selected because:

* It integrates well with CI/CD.
* It can scan container images.
* It is practical for a Docker-based project.
* It provides vulnerability information that can be used in security gates.

---

## 9.3 Security Gate Example

```text
Docker Build
     ↓
Trivy Scan
     ↓
Critical Vulnerability?
     ↙             ↘
   YES              NO
    ↓                ↓
 Pipeline FAIL    Continue
```

The exact severity threshold will be defined during implementation.

---

# 10. OWASP ZAP — DAST

## 10.1 What is OWASP ZAP?

OWASP ZAP is a web application security testing tool.

It is used to test a running web application.

This makes it different from SAST.

```text
SAST
↓
Analyzes source code

DAST
↓
Tests running application
```

---

## 10.2 DAST

DAST means:

**Dynamic Application Security Testing**

Dynamic means that the application is running while the security test is performed.

Example:

```text
Angular + Spring Boot
        ↓
Running Application
        ↓
OWASP ZAP
        ↓
HTTP Requests
        ↓
Security Analysis
        ↓
Findings
```

---

## 10.3 Why OWASP ZAP?

ZAP was selected because:

* It is designed for web application security testing.
* It can be automated.
* It integrates into CI/CD pipelines.
* It provides practical security testing experience.
* It complements SAST.

---

# 11. GitHub Actions — CI/CD Automation

## 11.1 What is GitHub Actions?

GitHub Actions is the automation platform used to execute the project's CI/CD workflows.

It can automatically:

* Build the application
* Run tests
* Run security tools
* Build Docker images
* Scan Docker images
* Run DAST
* Enforce security gates
* Deploy the application

---

## 11.2 Why GitHub Actions?

It was selected because:

* The source code is hosted on GitHub.
* It integrates directly with the repository.
* It supports automated workflows.
* It can execute security tools.
* It allows the complete DevSecOps pipeline to be demonstrated.

---

# 12. Wazuh — Security Monitoring

## 12.1 What is Wazuh?

Wazuh is a security monitoring and SIEM/XDR platform.

It can collect and analyze security-related events from monitored systems.

In this project, Wazuh is considered primarily for the **Operations** phase.

---

## 12.2 Why Wazuh?

Wazuh is relevant because the project includes cybersecurity and security operations concepts.

It can help demonstrate:

* Log collection
* Security monitoring
* Event detection
* Alerting
* Host monitoring
* Security visibility

---

## 12.3 Position in the Architecture

```text
SecureOps Application
        ↓
Application Logs
        ↓
System / Container Logs
        ↓
Wazuh
        ↓
Security Events
        ↓
Alerts / Investigation
```

Wazuh integration may be implemented after the core DevSecOps pipeline is operational.

---

# 13. Why Multiple Security Tools?

No single tool can detect every type of security problem.

Each tool observes a different layer.

```text
                SecureOps Platform
                       │
       ┌───────────────┼────────────────┐
       │               │                │
       ▼               ▼                ▼
   Source Code    Dependencies      Secrets
       │               │                │
   SonarQube     Dependency-Check   Gitleaks
       │               │                │
       └───────────────┼────────────────┘
                       │
                       ▼
                    Docker
                       │
                       ▼
                    Trivy
                       │
                       ▼
              Running Application
                       │
                       ▼
                    OWASP ZAP
                       │
                       ▼
                  Deployment
                       │
                       ▼
                    Wazuh
```

This is an example of **defense in depth**.

Defense in depth means using multiple security controls so that if one control misses a problem, another control may detect it.

---

# 14. Tool Responsibilities

Each tool has a clearly defined responsibility.

| Tool             | Main Question                                                |
| ---------------- | ------------------------------------------------------------ |
| SonarQube        | Is there a security or quality problem in the source code?   |
| Dependency-Check | Do our dependencies contain known vulnerabilities?           |
| Gitleaks         | Did we accidentally expose a secret?                         |
| JUnit            | Does the application behave correctly?                       |
| Docker           | Can we package the application consistently?                 |
| Trivy            | Does the container image contain known vulnerabilities?      |
| OWASP ZAP        | Does the running web application expose security weaknesses? |
| GitHub Actions   | Can we automate the complete security process?               |
| Wazuh            | What security events are happening during operations?        |

---

# 15. Security Tool Decision Flow

The tools work together rather than independently.

```text
                    Git Push
                       │
                       ▼
              ┌─────────────────┐
              │    Gitleaks     │
              │ Secret Scanning │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │    SonarQube    │
              │      SAST       │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ Dependency-Check│
              │      SCA        │
              └────────┬────────┘
                       │
                       ▼
                Automated Tests
                       │
                       ▼
                  Docker Build
                       │
                       ▼
              ┌─────────────────┐
              │      Trivy      │
              │ Container Scan  │
              └────────┬────────┘
                       │
                       ▼
              Test Environment
                       │
                       ▼
              ┌─────────────────┐
              │    OWASP ZAP    │
              │      DAST       │
              └────────┬────────┘
                       │
                       ▼
                 Security Gate
                       │
                  ┌────┴────┐
                  │         │
                 FAIL      PASS
                  │         │
                  ▼         ▼
                STOP      DEPLOY
                            │
                            ▼
                         Wazuh
                            │
                            ▼
                       Monitoring
```

---

# 16. Security Gate Strategy

The security gate is an important part of the DevSecOps architecture.

It determines whether the application can continue toward deployment.

A simplified decision model is:

```text
Security Checks
      │
      ├── SAST
      ├── SCA
      ├── Secret Scan
      ├── Container Scan
      └── DAST
             │
             ▼
       Security Gate
             │
       ┌─────┴─────┐
       │           │
     PASS         FAIL
       │           │
       ▼           ▼
   Continue       Stop
   Pipeline      Pipeline
```

The exact thresholds will be configured during implementation.

For example, the project may configure the pipeline to stop when a critical vulnerability is detected.

The thresholds should be documented so that the security decision is transparent and reproducible.

---

# 17. Tool Integration Philosophy

The project does not consider security tools as isolated software installations.

For every tool, the project will document:

1. What the tool does.
2. Why the tool was selected.
3. Where it runs.
4. What input it receives.
5. What output it produces.
6. What security problem it can detect.
7. How the result affects the pipeline.
8. How a finding is investigated.
9. How the vulnerability is remediated.
10. How the fix is verified.

This approach ensures that the project demonstrates understanding rather than simple tool installation.

---

# 18. Controlled Security Experiments

To demonstrate that the tools actually work, controlled security experiments will be performed.

Example:

```text
1. Introduce a controlled security issue.
              ↓
2. Commit the change.
              ↓
3. Start the CI/CD pipeline.
              ↓
4. Security tool detects the issue.
              ↓
5. Pipeline produces a finding.
              ↓
6. Security gate blocks the pipeline.
              ↓
7. Analyze the finding.
              ↓
8. Fix the issue.
              ↓
9. Run the pipeline again.
              ↓
10. Verify that the security check passes.
```

This process will be documented with screenshots and security reports.

---

# 19. Future Security Improvements

The initial project focuses on tools that provide the most important DevSecOps capabilities.

Future improvements may include:

* HashiCorp Vault for centralized secrets management
* Dependency-Track for software supply-chain visibility
* Kubernetes security
* Infrastructure as Code security
* Terraform security scanning
* Cloud security controls
* Advanced Wazuh integration
* Security orchestration and automation
* Threat intelligence integration
* Advanced vulnerability management

These features are outside the minimum initial scope but can be added if time and resources permit.

---

# 20. Final Security Strategy

SecureOps Platform follows a layered security strategy:

```text
                SECUREOPS SECURITY

                       │
                       ▼
                Secure Coding
                       │
                       ▼
                    SAST
                       │
                       ▼
                     SCA
                       │
                       ▼
              Secret Detection
                       │
                       ▼
              Automated Testing
                       │
                       ▼
             Container Security
                       │
                       ▼
                      DAST
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
              Security Response
```

The objective is to continuously identify, prevent, detect, and remediate security weaknesses throughout the software lifecycle.

This approach represents the core principle of the project:

> **Security should be integrated into development, delivery, deployment, and operations rather than treated as a final step.**
