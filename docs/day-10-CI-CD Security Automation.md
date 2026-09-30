# Day 10 — CI/CD Security Automation

## 1. Objective

The objective of Day 10 is to automate the security testing process using CI/CD.

Instead of running security tests manually on the local computer, GitHub Actions automatically runs the tests when code is pushed to the repository.

The workflow is:

```text
Developer changes code
        ↓
git push
        ↓
GitHub Actions
        ↓
Start PostgreSQL
        ↓
Configure Java 17
        ↓
Run Maven tests
        ↓
Security tests
        ↓
PASS or FAIL
```

This gives the SecureOps Platform an automated security verification process.

---

## 2. What is CI/CD?

### CI — Continuous Integration

Continuous Integration means automatically checking new code when developers push changes.

The CI process can:

* Build the application
* Download dependencies
* Run automated tests
* Detect errors
* Detect security regressions
* Report success or failure

### CD — Continuous Delivery / Deployment

Continuous Delivery or Continuous Deployment automates the process of delivering or deploying software after the required checks pass.

For SecureOps Platform, Day 10 mainly focuses on **CI security automation**.

---

## 3. GitHub Actions

GitHub Actions is the CI/CD automation system used by the SecureOps Platform project.

The workflow is stored in:

```text
.github/workflows/security-tests.yml
```

The workflow is named:

```text
SecureOps Security Tests
```

GitHub Actions automatically detects the workflow when code is pushed to the `main` branch.

It also runs when a pull request targets the `main` branch.

---

## 4. Day 10 Architecture

The CI/CD security architecture is:

```text
Developer
    |
    | git push
    v
GitHub Repository
    |
    v
GitHub Actions
    |
    +---- Checkout repository
    |
    +---- Start PostgreSQL 17
    |
    +---- Configure Java 17
    |
    +---- Configure Maven
    |
    +---- Run security tests
    |
    v
Security Test Result
    |
    +---- PASS
    |
    +---- FAIL
```

The important idea is:

> Every important code change can be automatically checked by the security test suite.

---

## 5. GitHub Actions Workflow

The workflow file is:

```text
.github/workflows/security-tests.yml
```

The workflow runs on:

```yaml
push:
  branches:
    - main
```

and:

```yaml
pull_request:
  branches:
    - main
```

Therefore, the security tests can run automatically when:

* Code is pushed to `main`
* A pull request targets `main`

---

## 6. Workflow Job

The workflow contains one main job:

```text
security-tests
```

The job runs on:

```text
ubuntu-latest
```

The job performs the complete CI security test process.

The main steps are:

```text
1. Checkout repository
2. Start PostgreSQL
3. Set up Java 17
4. Verify Java
5. Verify Maven
6. Run Maven security tests
```

---

## 7. PostgreSQL in CI

The SecureOps Platform backend uses PostgreSQL.

The application configuration contains:

```text
spring.datasource.url=jdbc:postgresql://localhost:5432/secureops
```

Therefore, the GitHub Actions environment needs a PostgreSQL database before the Spring Boot tests can start.

The workflow starts:

```text
PostgreSQL 17
```

with:

```text
Database: secureops
User: secureops
Password: secureops_ci_password
Port: 5432
```

The workflow also uses a PostgreSQL health check:

```text
pg_isready
```

This checks that PostgreSQL is ready before Maven starts the tests.

---

## 8. Java 17

The SecureOps Platform backend uses Java 17.

GitHub Actions configures Java using:

```yaml
uses: actions/setup-java@v5
```

The workflow uses the Temurin distribution:

```text
distribution: temurin
java-version: "17"
```

Maven dependency caching is also enabled:

```text
cache: maven
```

This can reduce the time required to download Maven dependencies during future workflow executions.

---

## 9. Maven

The backend is a Maven project.

The Maven configuration is located at:

```text
backend/pom.xml
```

The project uses:

```text
Spring Boot 4.0.8
Java 17
Maven
PostgreSQL
Spring Security
JWT
```

The CI workflow runs:

```text
mvn -B clean test
```

inside:

```text
backend
```

The command performs:

```text
clean
    ↓
Compile
    ↓
Test
```

The `-B` option runs Maven in batch mode, which is suitable for CI environments.

---

## 10. CI Environment Variables

The Spring Boot application requires environment variables.

The database password is configured using:

```text
SECUREOPS_DB_PASSWORD
```

The JWT secret is configured using:

```text
SECUREOPS_JWT_SECRET
```

The JWT expiration value is configured using:

```text
SECUREOPS_JWT_EXPIRATION_MS
```

The CI workflow provides test values for these variables.

The CI environment therefore has the information required to start the application and execute the tests.

For real production deployments, sensitive credentials and secrets should be stored using protected secret-management mechanisms instead of committing production secrets to the repository.

---

## 11. Security Tests Executed in CI

The CI pipeline executes the automated security tests created during Day 09.

The security test suite covers areas including:

* Authentication
* Authentication negative cases
* Password hashing
* JWT security
* Disabled accounts
* RBAC
* Authorization
* Incident authorization
* Investigation-note authorization
* Negative security testing

The purpose is to make sure that security controls continue to work after code changes.

---

## 12. Authentication Testing

The automated tests verify authentication behavior.

Examples include:

```text
Valid credentials
    ↓
Authentication succeeds
```

and:

```text
Invalid credentials
    ↓
Authentication fails
```

The tests also verify security behavior for disabled accounts.

This helps prevent accidental changes that could allow unauthorized users to authenticate.

---

## 13. Password Security Testing

The project uses password hashing instead of storing raw passwords.

The security tests verify that:

```text
Raw password
      ↓
Password encoder
      ↓
Password hash
```

The tests verify:

* The password is hashed
* The correct password matches the hash
* An incorrect password does not match

This provides an automated check for password-security behavior.

---

## 14. JWT Security Testing

The project uses JWT authentication.

The tests verify security behavior around JWT authentication.

The general process is:

```text
Login
  ↓
JWT generated
  ↓
JWT sent with request
  ↓
JWT validated
  ↓
User authenticated
```

Invalid JWT requests must not receive unauthorized access.

This allows CI to detect regressions in JWT authentication.

---

## 15. RBAC Testing

RBAC means:

> Role-Based Access Control

The application contains roles such as:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

The security tests verify that users receive access according to their roles.

For example:

```text
ADMIN
    ↓
Administrative endpoints
    ↓
Allowed
```

while a lower-privileged role should receive an authorization failure when attempting to access an endpoint that requires administrator privileges.

---

## 16. Incident Authorization Testing

The project contains incident-management endpoints.

The tests verify that roles have the expected permissions.

For example, the project tests access to incident resources for:

```text
ADMIN
SECURITY_ANALYST
MANAGER
EMPLOYEE
```

The tests verify both allowed and forbidden requests.

This helps detect accidental changes to incident authorization rules.

---

## 17. Investigation Note Testing

The project also contains investigation-note functionality.

The security tests verify access to investigation notes.

The tested permissions include:

```text
Read notes:
ADMIN              → allowed
SECURITY_ANALYST   → allowed
MANAGER            → allowed
EMPLOYEE           → forbidden
```

and:

```text
Create notes:
ADMIN              → allowed
SECURITY_ANALYST   → allowed
MANAGER            → forbidden
EMPLOYEE           → forbidden
```

These tests make sure that investigation information is protected by role-based authorization.

---

## 18. Security Failure Behavior

One important CI/CD principle is:

> A security test failure should make the CI job fail.

For example:

```text
Security test
     ↓
Failure
     ↓
Maven test failure
     ↓
GitHub Actions failure
     ↓
❌ CI does not pass
```

This makes security testing part of the software delivery process.

---

## 19. Successful GitHub Actions Execution

The first GitHub Actions execution was triggered by:

```text
Commit: 745630e
Message:
ci: automate security tests with github actions
```

The workflow was:

```text
SecureOps Security Tests
```

The result was:

```text
SUCCESS
```

The first execution completed in:

```text
1 minute 37 seconds
```

This confirmed that the CI environment successfully:

1. Started PostgreSQL.
2. Configured Java 17.
3. Configured Maven.
4. Started the Spring Boot test environment.
5. Executed the security test suite.
6. Completed successfully.

---

## 20. CI Warning Cleanup

The first workflow used:

```text
actions/setup-java@v4
```

GitHub reported that this version was deprecated.

The workflow was updated to:

```text
actions/setup-java@v5
```

The update was committed with:

```text
Commit: a9b6c05
```

Commit message:

```text
ci: update setup java action
```

The updated workflow was successfully pushed to GitHub.

---

## 21. GitHub Actions Re-run

The GitHub Actions workflow was successfully re-run.

The result was:

```text
Status: Success
```

The successful execution completed in:

```text
50 seconds
```

This confirmed that the CI workflow continued to work after the workflow update.

---

## 22. Complete CI/CD Flow

The final CI/CD security flow is:

```text
Developer
    |
    | Code change
    v
Git
    |
    | git push
    v
GitHub
    |
    v
GitHub Actions
    |
    +----------------------+
    |                      |
    v                      v
PostgreSQL 17          Java 17
    |                      |
    +----------+-----------+
               |
               v
             Maven
               |
               v
          Security Tests
               |
        +------+------+
        |             |
        v             v
      PASS           FAIL
        |             |
        v             v
    CI success     CI failure
```

---

## 23. Why This Is Important

Before Day 10, security tests could be executed manually.

After Day 10, security tests are connected to the GitHub repository.

This means the project has moved from:

```text
Manual security testing
```

to:

```text
Automated CI security testing
```

This reduces the risk of forgetting to run security tests after code changes.

It also creates a repeatable security verification process.

---

## 24. Day 10 Checklist

```text
Repository inspection              ✅
Maven configuration inspection     ✅
PostgreSQL requirement inspection  ✅
GitHub Actions workflow            ✅
PostgreSQL CI service              ✅
Java 17 CI setup                   ✅
Maven CI setup                     ✅
Security tests in CI               ✅
Automatic execution on push        ✅
Successful GitHub Actions run      ✅
CI warnings identified             ✅
setup-java updated to v5           ✅
Workflow committed                 ✅
Workflow pushed to GitHub          ✅
CI re-run successful               ✅
Documentation                      ✅
```

---

## 25. What I Learned

Day 10 introduced the connection between security testing and CI/CD.

Important concepts:

```text
CI
Continuous Integration

CD
Continuous Delivery / Deployment

GitHub Actions
CI/CD automation platform

Workflow
A defined sequence of automated CI/CD steps

Runner
The machine that executes the workflow

Service
A supporting application used during the workflow

PostgreSQL service
The database required by the Spring Boot tests

Security test
An automated test that verifies security behavior

Security gate
A check that can prevent a successful pipeline when security tests fail
```

The most important mental model is:

```text
Code change
    ↓
Git push
    ↓
CI pipeline
    ↓
Build
    ↓
Security tests
    ↓
PASS / FAIL
```

---

## 26. Day 10 Final Result

Day 10 successfully connected the security tests from Day 09 to an automated GitHub Actions CI pipeline.

The SecureOps Platform now has:

* GitHub Actions automation
* Java 17 CI environment
* PostgreSQL 17 CI service
* Maven test execution
* Automated security testing
* Automatic execution on push
* Security test verification
* Successful CI execution

The project now automatically checks important security controls when code changes are pushed to GitHub.

---

## 27. Evidence

Important evidence for Day 10 includes:

```text
1. GitHub Actions workflow file

.github/workflows/security-tests.yml
```

```text
2. GitHub Actions successful run

SecureOps Security Tests
Status: Success
```

```text
3. Git commit

745630e
ci: automate security tests with github actions
```

```text
4. Workflow update

a9b6c05
ci: update setup java action
```

These provide evidence that CI/CD security automation was implemented and successfully executed.

---

## 28. Next Step

The next stage is:

# Day 11 — Threat Detection

Day 10 focused on automatically testing security controls.

Day 11 will begin the detection phase.

The planned direction is:

```text
Application activity
        ↓
Security events
        ↓
Detection rules
        ↓
Suspicious activity
        ↓
Security alert
```

The project will move from:

```text
Security Testing
```

toward:

```text
Threat Detection
```

This is an important step toward the final SOC/SIEM architecture of the SecureOps Platform.