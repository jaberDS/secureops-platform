# Day 13 — Advanced Security Detection

## 1. Goal

The goal of Day 13 was to improve the security detection system.

The application already collected security events. On Day 13, we added and validated detection rules that can recognize suspicious activity and create security alerts.

The detection engine connects:

```text
Security Event
      ↓
Threat Detection Service
      ↓
Detection Rule
      ↓
Security Alert
      ↓
Risk Score
      ↓
SIEM / Splunk
```

---

## 2. Threat Detection Service

The main component is:

```text
ThreatDetectionService
```

It receives security events and checks them against detection rules.

The service:

1. Saves the security event.
2. Sends the event to the SIEM exporter.
3. Checks the event against detection rules.
4. Creates an alert when suspicious activity is detected.
5. Assigns a severity.
6. Assigns a risk score.
7. Saves the security alert.

---

## 3. Detection Rules

The application currently detects several important security situations.

### Rule 1 — Multiple Login Failures

Event:

```text
LOGIN_FAILURE
```

The application checks the last 10 minutes.

If the same actor has at least three failed login attempts:

```text
3+ LOGIN_FAILURE events
        ↓
MULTIPLE_LOGIN_FAILURES
        ↓
MEDIUM
        ↓
Risk Score: 10
```

This can indicate a possible brute-force or password-guessing attempt.

---

### Rule 2 — Disabled Account Login

Event:

```text
DISABLED_ACCOUNT_LOGIN
```

When a disabled account attempts authentication:

```text
DISABLED_ACCOUNT_LOGIN
        ↓
Detection Rule:
DISABLED_ACCOUNT_LOGIN
        ↓
HIGH
        ↓
Risk Score: 20
```

This is important because a disabled account should not be able to authenticate.

---

### Rule 3 — Unauthorized Access

Event:

```text
UNAUTHORIZED_ACCESS
```

The system creates:

```text
Rule:
UNAUTHORIZED_ACCESS

Severity:
HIGH

Risk Score:
20
```

This detects attempts to access protected resources without enough permission.

---

### Rule 4 — Role Change

Event:

```text
ROLE_CHANGED
```

The system creates:

```text
Rule:
ROLE_CHANGE

Severity:
HIGH

Risk Score:
15
```

Role changes are security-sensitive because changing a user's role can change their permissions.

---

### Rule 5 — Administrative Activity

Event:

```text
ADMIN_ACTIVITY
```

The system creates:

```text
Rule:
SUSPICIOUS_ADMIN_ACTIVITY

Severity:
HIGH

Risk Score:
15
```

Administrative activity can require additional security review.

---

### Rule 6 — Critical Security Event

If an event has:

```text
severity = CRITICAL
```

the detection engine creates:

```text
Rule:
CRITICAL_SECURITY_EVENT

Severity:
CRITICAL

Risk Score:
25
```

---

## 4. Risk Score

Each detection rule has a risk score.

Current scores:

| Detection                 | Severity | Risk |
| ------------------------- | -------- | ---: |
| Multiple login failures   | MEDIUM   |   10 |
| Disabled account login    | HIGH     |   20 |
| Unauthorized access       | HIGH     |   20 |
| Role change               | HIGH     |   15 |
| Suspicious admin activity | HIGH     |   15 |
| Critical security event   | CRITICAL |   25 |

The application also calculates the total risk score from recent alerts.

The current implementation looks at alerts from the last hour and limits the result to:

```text
100
```

---

## 5. Security Alert

When a detection rule matches an event, the system creates a `SecurityAlert`.

An alert contains information such as:

```text
ID
Detection Rule
Severity
Title
Description
Source Event ID
Timestamp
Status
Risk Score
```

Example:

```json
{
  "id": 1,
  "detectionRule": "DISABLED_ACCOUNT_LOGIN",
  "severity": "HIGH",
  "title": "Disabled account login attempt",
  "description": "A disabled account attempted to authenticate.",
  "sourceEventId": 5,
  "status": "NEW",
  "riskScore": 20
}
```

---

## 6. End-to-End Detection Test

A real security event was generated:

```text
Event Type:
DISABLED_ACCOUNT_LOGIN

Source:
AUTHENTICATION

Actor:
disabled@example.com

IP:
192.168.50.30

Severity:
HIGH
```

The application created:

```text
Security Alert:
DISABLED_ACCOUNT_LOGIN

Severity:
HIGH

Risk Score:
20

Status:
NEW
```

The backend log confirmed the event:

```text
SIEM exported:
id=5
type=DISABLED_ACCOUNT_LOGIN
severity=HIGH
```

This proved that the detection system works at runtime.

---

## 7. Automated Tests

The detection engine was tested with automated unit tests.

The tests cover:

```text
Disabled account login
Unauthorized access
Role changes
Critical events
Normal events
Multiple login failures
```

The expected behavior was verified.

For example:

```text
3 login failures
        ↓
MULTIPLE_LOGIN_FAILURES
        ↓
MEDIUM
        ↓
Risk 10
```

A normal low-severity event does not create an alert unless another detection condition matches.

---

## 8. SIEM Connection

The detection engine is connected to the SIEM exporter.

The flow is:

```text
Security Event
      ↓
Database
      ↓
ThreatDetectionService
      ↓
SiemExporter
      ↓
Splunk HEC
      ↓
Splunk
```

This means security events can be investigated outside the application.

---

## 9. Day 13 Result

Day 13 successfully validated the advanced detection engine.

The application can now:

* Detect suspicious security events.
* Apply detection rules.
* Create security alerts.
* Assign severity.
* Calculate risk scores.
* Store alerts.
* Export security events to Splunk.
* Support automated testing.

### Day 13 Checklist

* [x] Threat detection service
* [x] Multiple login failure detection
* [x] Disabled account detection
* [x] Unauthorized access detection
* [x] Role change detection
* [x] Administrative activity detection
* [x] Critical event detection
* [x] Risk scoring
* [x] Security alert creation
* [x] Automated detection tests
* [x] Runtime detection test
* [x] SIEM export validation

---

## 10. Conclusion

Day 13 added an important security layer to SecureOps Platform.

The application is no longer only collecting security events.

It can now analyze events, detect suspicious behavior, create alerts, assign risk, and send the information to the SIEM.

The main security flow is:

```text
Detect
  ↓
Alert
  ↓
Score
  ↓
Export
  ↓
Investigate
```
