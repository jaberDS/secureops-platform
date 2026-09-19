# Audit Logging

## 1. Overview

Audit logging records important security and business actions performed inside the SecureOps Platform.

The goal is to answer three questions:

- Who performed the action?
- What action was performed?
- When did the action happen?

Audit logs are useful for:

- Security monitoring
- Incident investigation
- Accountability
- Compliance
- Forensic analysis

---

## 2. Audit Log Architecture

The audit logging flow is:

```text
User Action
    |
    v
Controller
    |
    v
Service Layer
    |
    v
AuditLogService
    |
    v
AuditLogRepository
    |
    v
PostgreSQL
    |
    v
audit_logs

The application automatically creates audit records when important actions occur.

3. Database Model

The audit_logs table contains:

Field	Description
id	Unique audit event identifier
user_id	User who performed the action
action	Type of action
resource_type	Type of affected resource
resource_id	Identifier of affected resource
timestamp	Date and time of the event
details	Additional event information

The user_id can be NULL for authentication failures involving an unknown email address.

4. Incident Events

The following incident actions are currently audited.

Incident creation
Action: CREATE
Resource: INCIDENT

Example:

Incident created

The authenticated user who created the incident is recorded.

Incident assignment
Action: ASSIGN
Resource: INCIDENT

Example:

Incident assigned to analyst@example.com

The audit record stores the actual actor in user_id.

The assigned analyst is recorded in the details.

This distinction is important:

Actor  = user who performed the assignment
Target = analyst who received the assignment
Incident status change
Action: STATUS_CHANGE
Resource: INCIDENT

Example:

Status changed from ASSIGNED to INVESTIGATING

The authenticated user performing the transition is recorded.

5. Authentication Events

Authentication events are recorded as security events.

Successful login
Action: LOGIN_SUCCESS
Resource: AUTHENTICATION

Example:

Successful authentication
Failed login
Action: LOGIN_FAILURE
Resource: AUTHENTICATION

Example:

Authentication failed

Failed authentication events are recorded even when the email does not belong to an existing user.

In that case:

user_id = NULL

This allows the SOC to detect suspicious authentication activity involving unknown accounts.

6. Security and Privacy

Audit logs must not contain sensitive authentication secrets.

The system does NOT store:

- Plain-text passwords
- Password hashes
- JWT tokens
- Authentication tokens

Only security-event information is recorded.

7. Access Control

The audit-log API is protected using Spring Security.

Endpoint:

GET /api/audit-logs

Allowed roles:

ADMIN
SECURITY_ANALYST

Denied roles:

MANAGER
EMPLOYEE

This follows the principle of least privilege.

Only roles that need security visibility can access the audit history.

8. Security Testing

The audit logging feature was tested with multiple roles and scenarios.

RBAC tests
Role	GET /api/audit-logs	Result
ADMIN	200	PASS
SECURITY_ANALYST	200	PASS
MANAGER	403	PASS
EMPLOYEE	403	PASS
Authentication tests
Scenario	Expected event	Result
Valid credentials	LOGIN_SUCCESS	PASS
Existing user + wrong password	LOGIN_FAILURE	PASS
Unknown email	LOGIN_FAILURE	PASS
9. Verified Database Events

During testing, the database contained events such as:

CREATE
ASSIGN
STATUS_CHANGE
LOGIN_SUCCESS
LOGIN_FAILURE

The authentication tests also demonstrated that an unknown account can produce:

LOGIN_FAILURE
user_id = NULL

without creating a fake user.

10. SOC Value

Audit logging provides a foundation for security monitoring.

For example, a SIEM could later detect:

Multiple LOGIN_FAILURE events
        |
        v
Possible password guessing
        |
        v
SOC investigation

Another example:

LOGIN_SUCCESS
      +
STATUS_CHANGE
      +
ASSIGN
      |
      v
Incident investigation timeline

This makes audit logs useful for incident response and forensic investigation.

11. Future Improvements

Possible future improvements include:

IP address logging
User-agent logging
Request correlation IDs
Login rate limiting
Detection of repeated failed logins
Automatic alerts
Audit-log filtering
Pagination
Exporting audit logs
SIEM integration
Wazuh integration
Immutable or append-only audit storage

These improvements can be implemented as the SecureOps Platform evolves.