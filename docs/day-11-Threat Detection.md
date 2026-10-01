# Day 11 — Threat Detection

## 1. Objective

The objective of Day 11 was to add a basic **Threat Detection Engine** to the SecureOps Platform.

The platform now transforms security activity into security events, analyzes those events using detection rules, creates security alerts, and calculates a security risk score.

The main detection flow is:

```text
Security Activity
       ↓
   Audit Log
       ↓
 Security Event
       ↓
Detection Engine
       ↓
 Detection Rule
       ↓
Security Alert
       ↓
  Risk Score
       ↓
 Investigation
```

This provides the foundation for future SOC and SIEM integration.

---

# 2. Threat Detection Architecture

The Day 11 architecture extends the existing security and audit logging system.

```text
Authentication / Security Activity
              ↓
        AuditLogService
              ↓
        SecurityEvent
              ↓
    ThreatDetectionService
              ↓
        Detection Rules
              ↓
        SecurityAlert
              ↓
          Risk Score
```

The important components are:

* `SecurityEvent`
* `SecurityAlert`
* `ThreatDetectionService`
* `ThreatDetectionController`
* `SecurityEventRepository`
* `SecurityAlertRepository`
* `ThreatSeverity`
* `DetectionStatus`

---

# 3. Security Events

A new `SecurityEvent` entity was introduced to represent security-related activity.

A security event contains:

* Event type
* Source
* Actor
* IP address
* Description
* Severity
* Timestamp

Example event types include:

```text
LOGIN_FAILURE
DISABLED_ACCOUNT_LOGIN
UNAUTHORIZED_ACCESS
ROLE_CHANGED
ADMIN_ACTIVITY
```

Security events are stored in the:

```text
security_events
```

database table.

The event provides the information required by the detection engine.

---

# 4. Threat Severity

The system defines four threat severity levels:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### LOW

Used for normal or low-risk security activity.

Example:

```text
LOGIN_SUCCESS
```

### MEDIUM

Used for suspicious activity that requires monitoring.

Example:

```text
LOGIN_FAILURE
```

### HIGH

Used for security activity that requires investigation.

Examples:

```text
DISABLED_ACCOUNT_LOGIN
UNAUTHORIZED_ACCESS
ROLE_CHANGED
ADMIN_ACTIVITY
```

### CRITICAL

Used for events that have the highest security importance.

Example:

```text
CRITICAL_SECURITY_EVENT
```

---

# 5. Detection Rules

The `ThreatDetectionService` analyzes security events using predefined detection rules.

## Rule 1 — Multiple Login Failures

Event type:

```text
LOGIN_FAILURE
```

Detection condition:

```text
3 or more failed login attempts
within a 10-minute window
for the same actor
```

When the condition is reached, the system creates:

```text
Detection Rule: MULTIPLE_LOGIN_FAILURES
Severity: MEDIUM
Risk Score: 10
```

Example:

```text
Login attempt 1
     ↓
LOGIN_FAILURE
     ↓
No alert

Login attempt 2
     ↓
LOGIN_FAILURE
     ↓
No alert

Login attempt 3
     ↓
LOGIN_FAILURE
     ↓
MULTIPLE_LOGIN_FAILURES
     ↓
Security Alert
```

This prevents every individual failed login from immediately generating an alert.

---

## Rule 2 — Disabled Account Login

Event type:

```text
DISABLED_ACCOUNT_LOGIN
```

Detection result:

```text
Detection Rule: DISABLED_ACCOUNT_LOGIN
Severity: HIGH
Risk Score: 20
```

This detects an authentication attempt involving a disabled account.

---

## Rule 3 — Unauthorized Access

Event type:

```text
UNAUTHORIZED_ACCESS
```

Detection result:

```text
Detection Rule: UNAUTHORIZED_ACCESS
Severity: HIGH
Risk Score: 20
```

This detects attempts to access protected resources without sufficient permission.

---

## Rule 4 — Role Change

Event type:

```text
ROLE_CHANGED
```

Detection result:

```text
Detection Rule: ROLE_CHANGE
Severity: HIGH
Risk Score: 15
```

Role changes are security-sensitive because changing a user's role can change their permissions.

---

## Rule 5 — Administrative Activity

Event type:

```text
ADMIN_ACTIVITY
```

Detection result:

```text
Detection Rule: SUSPICIOUS_ADMIN_ACTIVITY
Severity: HIGH
Risk Score: 15
```

Administrative activity is treated as security-sensitive activity that may require review.

---

## Rule 6 — Critical Security Event

If an event has:

```text
Severity = CRITICAL
```

the detection engine creates:

```text
Detection Rule: CRITICAL_SECURITY_EVENT
Severity: CRITICAL
Risk Score: 25
```

This provides a generic rule for critical security events.

---

# 6. Security Alerts

When a detection rule is triggered, the system creates a `SecurityAlert`.

A security alert contains:

* Alert ID
* Detection rule
* Severity
* Title
* Description
* Source event
* Timestamp
* Status
* Risk score

Alerts are stored in:

```text
security_alerts
```

the database table.

---

# 7. Alert Status

Security alerts support three statuses:

```text
NEW
ACKNOWLEDGED
RESOLVED
```

### NEW

The alert has been generated but has not yet been handled.

### ACKNOWLEDGED

A security analyst has reviewed the alert and acknowledged it.

### RESOLVED

The security issue associated with the alert has been investigated and resolved.

New alerts are created with:

```text
status = NEW
```

---

# 8. Risk Score

The threat detection engine calculates a risk score from recent security alerts.

The current calculation uses alerts from the last hour.

The final score is limited to:

```text
100
```

Example:

```text
Multiple login failures       10
Unauthorized access           20
Role change                   15
Critical security event      25
--------------------------------
Total                         70
```

The maximum score is:

```text
100
```

This score can later be displayed in the SOC dashboard.

---

# 9. Threat Detection Service

The main detection logic is implemented in:

```text
ThreatDetectionService.java
```

The service performs the following operations:

```text
1. Receive security event
2. Store security event
3. Analyze event
4. Apply detection rules
5. Create security alert if a rule matches
6. Calculate risk score
7. Return alert information
```

The service therefore acts as the main detection engine of the application.

---

# 10. Security Detection API

The threat detection controller is:

```text
ThreatDetectionController.java
```

The base endpoint is:

```text
/api/security
```

## Process Security Event

```http
POST /api/security/events
```

Purpose:

Process a security event through the detection engine.

The event is stored and analyzed.

If a detection rule is triggered, a security alert is returned.

---

## Get Security Alerts

```http
GET /api/security/alerts
```

Purpose:

Return security alerts ordered by timestamp.

---

## Get One Security Alert

```http
GET /api/security/alerts/{id}
```

Purpose:

Return a specific security alert.

---

## Get Risk Score

```http
GET /api/security/risk
```

Purpose:

Return the current calculated security risk score.

---

# 11. Security API Authorization

The security endpoints are protected by Spring Security.

The current security configuration protects:

```text
/api/security/**
```

with the:

```text
SECURITY_ANALYST
```

role.

The security tests verify that:

```text
SECURITY_ANALYST
        ↓
     ALLOWED
```

while:

```text
EMPLOYEE
        ↓
      403
```

and:

```text
Unauthenticated User
        ↓
      403
```

This prevents unauthorized users from accessing the threat detection system.

---

# 12. Integration With Audit Logging

The existing `AuditLogService` was integrated with the threat detection engine.

Security-related audit events are now forwarded to:

```text
ThreatDetectionService
```

The integration flow is:

```text
Security Activity
       ↓
AuditLogService
       ↓
SecurityEventRequest
       ↓
ThreatDetectionService
       ↓
Detection Rule
       ↓
SecurityAlert
```

For example:

```text
LOGIN_FAILURE
       ↓
AuditLogService
       ↓
SecurityEvent
       ↓
ThreatDetectionService
       ↓
MULTIPLE_LOGIN_FAILURES
       ↓
SecurityAlert
```

This means the detection engine is connected to actual application security activity.

---

# 13. Database Components

Day 11 introduced two new database entities.

## Security Event

```text
SecurityEvent
```

Database table:

```text
security_events
```

Repository:

```text
SecurityEventRepository
```

---

## Security Alert

```text
SecurityAlert
```

Database table:

```text
security_alerts
```

Repository:

```text
SecurityAlertRepository
```

The `SecurityAlert` entity also keeps a reference to its source `SecurityEvent`.

```text
SecurityEvent
      ↓
SecurityAlert
```

This allows an alert to be traced back to the event that generated it.

---

# 14. DTOs

The following DTOs were introduced.

## SecurityEventRequest

Used to receive security event information.

```text
SecurityEventRequest
```

Main fields:

```text
eventType
source
actor
ipAddress
description
severity
```

---

## SecurityAlertResponse

Used to return alert information to the API client.

```text
SecurityAlertResponse
```

Main fields:

```text
id
detectionRule
severity
title
description
sourceEventId
timestamp
status
riskScore
```

---

# 15. Testing Strategy

Day 11 introduced dedicated threat detection tests.

Testing was divided into:

1. Detection engine tests
2. Security API authorization tests
3. Full backend regression testing

---

# 16. ThreatDetectionServiceTest

The detection service tests verify the main detection rules.

The tests cover:

```text
1. Disabled account login
2. Unauthorized access
3. Role change
4. Critical security event
5. Normal low-severity event
6. Multiple login failures
```

Result:

```text
Tests run: 6
Failures: 0
Errors: 0
Skipped: 0
```

Status:

```text
PASS ✅
```

---

# 17. ThreatDetectionApiSecurityTest

The API security tests verify access control.

The tests cover:

```text
1. SECURITY_ANALYST can access alerts
2. SECURITY_ANALYST can access risk score
3. EMPLOYEE cannot access alerts
4. EMPLOYEE cannot access risk score
5. Unauthenticated user cannot access alerts
6. Unauthenticated user cannot access risk score
```

Result:

```text
Tests run: 6
Failures: 0
Errors: 0
Skipped: 0
```

Status:

```text
PASS ✅
```

---

# 18. Debug Validation

During development, real authentication events were also observed in the database.

Example:

```text
EVENT: id=5
type=LOGIN_FAILURE
actor=day11-detection-login@secureops.local

EVENT: id=6
type=LOGIN_FAILURE
actor=day11-detection-login@secureops.local
```

This confirmed that failed authentication activity was being recorded as security events.

---

# 19. Full Backend Test Result

After the Day 11 implementation, the complete backend test suite was executed.

Final result:

```text
Tests run: 36
Failures: 0
Errors: 0
Skipped: 0
```

Build result:

```text
BUILD SUCCESS
```

This confirms that the Day 11 implementation passed the complete backend test suite without introducing failing tests.

---

# 20. Day 11 Security Flow

The final Day 11 security flow is:

```text
User Activity
      ↓
Authentication / Authorization
      ↓
AuditLogService
      ↓
SecurityEvent
      ↓
ThreatDetectionService
      ↓
Detection Rules
      ↓
SecurityAlert
      ↓
Risk Score
      ↓
Security Analyst
      ↓
Investigation
```

This is the foundation for the future SOC/SIEM functionality of the platform.

---

# 21. Security Value

Before Day 11, the platform primarily recorded security activity.

```text
Security Activity
       ↓
Audit Log
```

After Day 11, the platform can actively analyze security activity.

```text
Security Activity
       ↓
Audit Log
       ↓
Security Event
       ↓
Detection
       ↓
Security Alert
       ↓
Risk Score
```

This is an important transition from simple application logging toward security monitoring.

---

# 22. Future Improvements

The current detection engine is intentionally simple.

Future improvements can include:

* IP-based detection
* Brute-force detection
* Multiple-account attack detection
* Suspicious API activity detection
* Suspicious PowerShell activity detection
* Impossible-travel detection
* Configurable detection rules
* Alert acknowledgement API
* Alert resolution API
* Automatic incident creation
* Threat intelligence enrichment
* Real-time notifications
* SIEM integration
* SOC dashboard integration
* Advanced risk scoring
* Detection rule management

These improvements will be addressed in later roadmap stages.

---

# 23. Day 11 Deliverables

The Day 11 implementation contains:

### Entities

```text
SecurityEvent.java
SecurityAlert.java
ThreatSeverity.java
DetectionStatus.java
```

### Repositories

```text
SecurityEventRepository.java
SecurityAlertRepository.java
```

### DTOs

```text
SecurityEventRequest.java
SecurityAlertResponse.java
```

### Service

```text
ThreatDetectionService.java
```

### Controller

```text
ThreatDetectionController.java
```

### Integration

```text
AuditLogService.java
```

### Tests

```text
ThreatDetectionServiceTest.java
ThreatDetectionApiSecurityTest.java
ThreatDetectionDebugTest.java
```

---

# 24. Day 11 Checklist

| Task                              | Status |
| --------------------------------- | ------ |
| Security event model              | ✅      |
| Security event repository         | ✅      |
| Security alert model              | ✅      |
| Security alert repository         | ✅      |
| Threat severity levels            | ✅      |
| Alert status model                | ✅      |
| Detection engine                  | ✅      |
| Multiple login failure detection  | ✅      |
| Disabled account detection        | ✅      |
| Unauthorized access detection     | ✅      |
| Role change detection             | ✅      |
| Administrative activity detection | ✅      |
| Critical event detection          | ✅      |
| Risk scoring                      | ✅      |
| Security API                      | ✅      |
| API authorization                 | ✅      |
| Audit log integration             | ✅      |
| Threat detection tests            | ✅      |
| API security tests                | ✅      |
| Full backend tests                | ✅      |
| Documentation                     | ✅      |

---

# 25. Final Validation

```text
DAY 11 — THREAT DETECTION

Implementation       ✅
Detection Rules      ✅
Security Alerts      ✅
Risk Scoring         ✅
API Security         ✅
Audit Integration    ✅
Unit Tests           ✅
Security Tests       ✅
Full Test Suite      ✅
Documentation        ✅
```

Final test result:

```text
36 Tests
0 Failures
0 Errors
0 Skipped
BUILD SUCCESS
```

---

# 26. Day 11 Status

```text
DAY 11 — THREAT DETECTION
STATUS: COMPLETE ✅
```

The SecureOps Platform now has a basic threat detection capability and is ready to move toward the next stage of the roadmap.

---

# 27. Next Roadmap Stage

The next stage is:

```text
DAY 12 — SOC / SIEM INTEGRATION
```

The objective of Day 12 will be to connect the security events, alerts, and risk information with a SOC/SIEM-oriented monitoring workflow.
