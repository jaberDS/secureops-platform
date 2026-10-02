# Day 12 — SOC / SIEM Integration

## 1. Goal

The goal of Day 12 was to connect the **SecureOps Platform** to **Splunk**.

The main idea is:

> When SecureOps detects a security event, the event is saved in the database and exported to Splunk.

This gives the project a basic **SOC / SIEM monitoring capability**.

---

# 2. Architecture

The Day 12 architecture is:

```text
                    SecureOps Platform
                           |
                           |
                    Security Event
                           |
                           v
                 ThreatDetectionService
                           |
              +------------+------------+
              |                         |
              v                         v
       PostgreSQL Database          SIEM Exporter
              |                         |
              |                         | HTTP POST
              |                         v
              |                  Splunk HEC :8088
              |                         |
              |                         v
              |                   Splunk index
              |                         |
              +-------------------------+
                                        |
                                        v
                                 SecureOps SOC
                                    Dashboard
```

The important flow is:

```text
Security Event
      ↓
SecureOps
      ↓
PostgreSQL
      ↓
SIEM Exporter
      ↓
Splunk HEC
      ↓
Splunk
      ↓
SOC Dashboard
```

---

# 3. Technologies Used

Day 12 uses:

* Spring Boot
* Java 17
* PostgreSQL
* Docker
* Splunk
* Splunk HTTP Event Collector (HEC)
* REST API
* JWT authentication
* Asynchronous Java processing
* SPL (Search Processing Language)

---

# 4. Splunk HEC

## What is HEC?

HEC means:

> HTTP Event Collector

It allows an application to send events to Splunk using HTTP.

Instead of manually sending logs to Splunk, SecureOps sends security events automatically.

The HEC endpoint used in the lab is:

```text
http://192.168.50.10:8088
```

The HEC port is:

```text
8088
```

Splunk was configured to receive events through HEC.

---

# 5. SecureOps SIEM Components

The backend contains several components for SIEM integration.

## SiemEvent

`SiemEvent` is the data object sent to Splunk.

It contains information such as:

```text
eventId
eventType
severity
actor
ipAddress
source
description
timestamp
application
```

Example:

```json
{
  "eventId": 5,
  "eventType": "DISABLED_ACCOUNT_LOGIN",
  "severity": "HIGH",
  "actor": "disabled@example.com",
  "ipAddress": "192.168.50.30",
  "source": "AUTHENTICATION",
  "description": "Day 12 end-to-end SIEM test"
}
```

---

# 6. SiemExporter

`SiemExporter` is responsible for sending the security event to Splunk.

The exporter sends the event to the Splunk HEC endpoint using HTTP.

The exporter also runs asynchronously.

This means the main security detection process does not need to wait for Splunk.

The important idea is:

```text
Detect event
     ↓
Save event
     ↓
Export to Splunk asynchronously
```

If Splunk is temporarily unavailable, the security detection process can continue.

---

# 7. Async Processing

The project uses Spring asynchronous processing.

The SIEM export is performed in the background.

This is useful because Splunk is an external system.

The application should not become unavailable just because Splunk is temporarily unreachable.

The flow is:

```text
SecureOps
   |
   +---- Save security event
   |
   +---- Start SIEM export
              |
              v
           Splunk
```

---

# 8. Configuration

The Splunk configuration uses environment variables.

The main variables are:

```text
SPLUNK_HEC_URL
SPLUNK_HEC_TOKEN
SPLUNK_HEC_INDEX
SPLUNK_HEC_SOURCETYPE
```

Expected configuration:

```text
SPLUNK_HEC_URL=http://192.168.50.10:8088
SPLUNK_HEC_TOKEN=<secret>
SPLUNK_HEC_INDEX=main
SPLUNK_HEC_SOURCETYPE=secureops
```

The HEC token is kept outside the source code.

This is important because secrets should not be hard-coded in Java files or committed to Git.

---

# 9. Security Event API

SecureOps provides an API for processing security events.

Endpoint:

```text
POST /api/security/events
```

The endpoint is protected.

The user needs a valid JWT and the required security analyst role.

Example request:

```json
{
  "eventType": "DISABLED_ACCOUNT_LOGIN",
  "source": "AUTHENTICATION",
  "actor": "disabled@example.com",
  "ipAddress": "192.168.50.30",
  "description": "Day 12 end-to-end SIEM test",
  "severity": "HIGH"
}
```

---

# 10. Test Security Event

For the Day 12 test, the following event was generated:

```text
Event type:
DISABLED_ACCOUNT_LOGIN

Severity:
HIGH

Actor:
disabled@example.com

IP address:
192.168.50.30

Source:
AUTHENTICATION
```

The purpose of this test was to verify the complete pipeline.

---

# 11. Security Alert Result

SecureOps generated a security alert.

The result was:

```text
Alert ID:
1

Detection rule:
DISABLED_ACCOUNT_LOGIN

Severity:
HIGH

Title:
Disabled account login attempt

Source event ID:
5

Status:
NEW

Risk score:
20
```

This confirms that the security detection logic worked.

---

# 12. PostgreSQL Persistence

The security event was also stored in PostgreSQL.

The important event ID was:

```text
5
```

The generated alert referenced the same source event:

```text
sourceEventId = 5
```

This creates a relationship between the original security event and the generated security alert.

The flow is:

```text
Security Event
      |
      | ID = 5
      v
PostgreSQL
      |
      v
Security Alert
      |
      | sourceEventId = 5
      v
Alert
```

---

# 13. Backend SIEM Verification

The backend log confirmed that the event was exported.

Important log message:

```text
SIEM exported: id=5 type=DISABLED_ACCOUNT_LOGIN severity=HIGH
```

This is important evidence because it proves that the Java application attempted the SIEM export.

---

# 14. Splunk Verification

The event was successfully received by Splunk.

The event contained:

```text
actor = disabled@example.com
application = secureops-platform
description = Day 12 end-to-end SIEM test
eventId = 5
eventType = DISABLED_ACCOUNT_LOGIN
ipAddress = 192.168.50.30
severity = HIGH
source = AUTHENTICATION
```

Splunk information:

```text
index = main
sourcetype = secureops
application = secureops-platform
```

This confirms the complete path:

```text
SecureOps
    ↓
SIEM Exporter
    ↓
Splunk HEC
    ↓
Splunk index=main
```

---

# 15. Splunk Search — All SecureOps Events

The first SOC search was:

```spl
index=main sourcetype=secureops application=secureops-platform
| table _time eventId eventType severity actor ipAddress source description
| sort - _time
```

This search displays SecureOps security events in chronological order.

At the time of testing, Splunk returned:

```text
6 events
```

The events included login success events and the Day 12 security test.

---

# 16. Splunk Search — High and Critical Events

The second search was:

```spl
index=main sourcetype=secureops application=secureops-platform severity IN (HIGH,CRITICAL)
| table _time eventId eventType severity actor ipAddress description
| sort - _time
```

The search returned the Day 12 high-severity event:

```text
eventId:
5

eventType:
DISABLED_ACCOUNT_LOGIN

severity:
HIGH
```

This search can be useful for a SOC analyst who wants to focus on important security events.

---

# 17. Splunk Search — Disabled Account Login

The third search was:

```spl
index=main sourcetype=secureops application=secureops-platform eventType=DISABLED_ACCOUNT_LOGIN
| table _time eventId eventType severity actor ipAddress description
| sort - _time
```

The result showed:

```text
5
DISABLED_ACCOUNT_LOGIN
HIGH
disabled@example.com
192.168.50.30
```

This search focuses on one specific security detection rule.

---

# 18. Splunk Search — Login Failures

The project also prepared a search for login failures:

```spl
index=main sourcetype=secureops application=secureops-platform eventType=LOGIN_FAILURE
| stats count by actor ipAddress
| sort - count
```

During the Day 12 test, there were no `LOGIN_FAILURE` events.

Therefore, the search returned:

```text
0 results
```

This is expected because no login failure test event was generated during this verification.

---

# 19. Splunk Search — Events by Severity Over Time

The project also used:

```spl
index=main sourcetype=secureops application=secureops-platform
| timechart count by severity
```

This showed the number of events over time.

The main observed values included:

```text
HIGH
LOW
NULL
```

The Day 12 high-severity event appeared in the `HIGH` series.

The login success events appeared in the `LOW` series.

---

# 20. SecureOps SOC Dashboard

A Splunk Classic Dashboard was created.

Dashboard name:

```text
SecureOps SOC
```

Description:

```text
SecureOps security monitoring dashboard powered by Splunk.
```

The dashboard contains five panels.

---

# 21. Dashboard Panel 1 — Security Events Over Time

Visualization:

```text
Line Chart
```

Title:

```text
Security Events Over Time
```

Search:

```spl
index=main sourcetype=secureops application=secureops-platform
| timechart count by severity
```

Purpose:

Show security activity over time and separate events by severity.

---

# 22. Dashboard Panel 2 — Events by Severity

Visualization:

```text
Column Chart
```

Title:

```text
Events by Severity
```

Search:

```spl
index=main sourcetype=secureops application=secureops-platform
| stats count by severity
| sort - count
```

Purpose:

Show how many events exist for each severity.

---

# 23. Dashboard Panel 3 — Top Actors

Visualization:

```text
Bar Chart
```

Title:

```text
Top Actors
```

Search:

```spl
index=main sourcetype=secureops application=secureops-platform
| stats count by actor
| sort - count
| head 10
```

Purpose:

Show the actors associated with the most security events.

---

# 24. Dashboard Panel 4 — High/Critical Events

Visualization:

```text
Statistics Table
```

Title:

```text
High/Critical Events
```

Search:

```spl
index=main sourcetype=secureops application=secureops-platform severity IN (HIGH,CRITICAL)
| table _time eventId eventType severity actor ipAddress description
| sort - _time
```

Purpose:

Show high and critical security events that may require investigation.

---

# 25. Dashboard Panel 5 — Disabled Account Attempts

Visualization:

```text
Statistics Table
```

Title:

```text
Disabled Account Attempts
```

Search:

```spl
index=main sourcetype=secureops application=secureops-platform eventType=DISABLED_ACCOUNT_LOGIN
| table _time eventId eventType severity actor ipAddress description
| sort - _time
```

Purpose:

Show attempts involving disabled accounts.

---

# 26. End-to-End Verification

The complete test was successful.

The verification chain was:

```text
1. Generate security event
          ↓
2. Threat detection processes event
          ↓
3. Security event saved to PostgreSQL
          ↓
4. Security alert generated
          ↓
5. SIEM exporter sends event
          ↓
6. Splunk HEC receives event
          ↓
7. Event stored in Splunk
          ↓
8. SPL searches find the event
          ↓
9. SOC dashboard displays the data
```

This proves that the SecureOps SIEM integration works end to end.

---

# 27. Failure Behavior

The SIEM exporter is designed so that the security detection pipeline does not depend completely on Splunk availability.

If Splunk is unavailable:

```text
SecureOps
    |
    +---- Security event
    |
    +---- PostgreSQL
    |
    +---- SIEM export attempt
                |
                X
             Splunk
```

The application can still keep the security event in PostgreSQL.

The SIEM exporter logs a warning when the export fails.

This is useful because a temporary SIEM problem should not stop the main application security functions.

---

# 28. Security Considerations

The HEC token is a sensitive secret.

The token should:

* Not be committed to Git.
* Not be written directly in Java source code.
* Not be placed in public documentation.
* Be provided through environment variables or secret management.
* Be rotated if it was exposed.

For this lab, the HEC token should be rotated after completing the Day 12 documentation.

---

# 29. Day 12 Evidence

The following evidence was collected during Day 12:

### Evidence 1 — Backend export

Backend log:

```text
SIEM exported: id=5 type=DISABLED_ACCOUNT_LOGIN severity=HIGH
```

### Evidence 2 — Splunk event

Splunk received:

```text
eventId = 5
eventType = DISABLED_ACCOUNT_LOGIN
severity = HIGH
actor = disabled@example.com
ipAddress = 192.168.50.30
```

### Evidence 3 — All SecureOps events

The all-events SPL search returned:

```text
6 events
```

### Evidence 4 — High/Critical search

The search returned the Day 12 `HIGH` event.

### Evidence 5 — Disabled account search

The search returned:

```text
DISABLED_ACCOUNT_LOGIN
```

### Evidence 6 — SOC Dashboard

The `SecureOps SOC` dashboard contains five monitoring panels.

---

# 30. Day 12 Checklist

```text
[✅] Splunk HEC configured
[✅] HEC port 8088
[✅] SIEM event model created
[✅] SIEM exporter created
[✅] Async SIEM export configured
[✅] Backend connected to Splunk
[✅] Security event generated
[✅] Security alert generated
[✅] Event saved to PostgreSQL
[✅] SIEM export confirmed in backend logs
[✅] Event received by Splunk
[✅] Splunk searches created
[✅] High/Critical search created
[✅] Disabled account search created
[✅] SOC dashboard created
[✅] Security Events Over Time panel
[✅] Events by Severity panel
[✅] Top Actors panel
[✅] High/Critical Events panel
[✅] Disabled Account Attempts panel
[⏳] HEC token rotation
[⏳] Git documentation commit
```

---

# 31. Day 12 Result

Day 12 added a basic SIEM integration to SecureOps.

The platform can now:

```text
Detect
   ↓
Store
   ↓
Export
   ↓
Search
   ↓
Monitor
```

Security events can move from the SecureOps application to Splunk and become visible through SOC searches and dashboards.

This creates the foundation for later work such as:

* Incident investigation
* Security testing
* Automated workflows
* Alert correlation
* SOC monitoring
* DevSecOps security automation

---

# 32. Next Step

The next security task after documentation is to rotate the Splunk HEC token.

After token rotation, the backend configuration must be updated and the containers restarted.

Then the integration should be tested again to confirm that:

```text
New token
    ↓
SecureOps
    ↓
Splunk HEC
    ↓
Event received
```

The old token should no longer be used.
