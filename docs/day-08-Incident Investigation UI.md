# Day 08 — Incident Investigation UI

## 1. Overview

Day 08 focuses on building the **Incident Investigation UI** for the SecureOps Platform.

The objective is to allow authorized security users to:

* View security incidents.
* Search and filter incidents.
* Select an incident for investigation.
* View incident details.
* Assign incidents to security analysts.
* Progress incidents through an investigation workflow.
* Record investigation notes.
* Integrate incident investigation with the SecureOps dashboard.
* Enforce backend authentication and role-based authorization.
* Handle API errors and loading states.
* Test the complete investigation workflow.

This day connects the application's **security monitoring dashboard** with an operational incident-response workflow.

---

# 2. Day 08 Objectives

The main objectives were:

1. Build the incident list.
2. Add client-side incident filtering.
3. Build incident details.
4. Implement an investigation workflow.
5. Implement incident actions.
6. Enforce RBAC.
7. Integrate the incident page with the dashboard.
8. Connect the Angular UI to the Spring Boot backend.
9. Test the investigation functionality.
10. Document the implementation.
11. Commit and push the completed work to Git.

---

# 3. Architecture

The Day 08 feature follows the existing SecureOps architecture:

```text
                    SecureOps Platform
                           |
                           v
                Angular Security Frontend
                           |
                           |
                    HTTP + JWT Token
                           |
                           v
                Spring Boot REST API
                           |
             +-------------+-------------+
             |                           |
             v                           v
       Incident API              Investigation Note API
             |                           |
             +-------------+-------------+
                           |
                           v
                       PostgreSQL
```

The frontend is responsible for:

* Displaying incidents.
* Searching incidents.
* Filtering incidents.
* Displaying investigation information.
* Sending authorized actions to the backend.
* Displaying API responses and errors.

The backend is responsible for:

* Authentication.
* Authorization.
* Business rules.
* Incident state transitions.
* Incident assignment.
* Investigation note persistence.
* Audit/security controls.
* Database operations.

The database stores the persistent incident and investigation data.

---

# 4. Incident List

The incident page displays incidents retrieved from the backend.

Each incident contains information such as:

* Incident ID.
* Title.
* Description.
* Category.
* Severity.
* Status.
* Reporter.
* Assigned analyst.

The Angular application stores the complete backend result in:

```typescript
incidents: Incident[] = [];
```

A separate collection is used for the filtered result:

```typescript
filteredIncidents: Incident[] = [];
```

This separation is important because the original backend dataset remains available while the UI can apply different filters.

---

# 5. Incident Filtering

Day 08 introduces client-side filtering.

The filtering interface contains:

* Search.
* Severity filter.
* Status filter.
* Clear Filters button.
* Result counter.

The search field can search across:

* Incident ID.
* Title.
* Description.
* Category.
* Reporter email.
* Assigned analyst email.

Example:

```text
Search: suspicious
```

The application searches the available incident information and displays matching incidents.

---

## 5.1 Severity Filtering

The severity filter supports:

```text
All Severities
Critical
High
Medium
Low
```

The selected value is stored in:

```typescript
severityFilter = '';
```

The filter is applied against the incident severity.

---

## 5.2 Status Filtering

The status filter supports:

```text
All Statuses
Open
Assigned
Investigating
Contained
Resolved
Closed
```

The selected value is stored in:

```typescript
statusFilter = '';
```

---

## 5.3 Combined Filtering

The filters can be combined.

For example:

```text
Search: login
Severity: HIGH
Status: INVESTIGATING
```

The application returns only incidents satisfying all selected conditions.

Conceptually:

```text
Search match
      AND
Severity match
      AND
Status match
```

---

## 5.4 Clear Filters

The Clear Filters button resets:

```typescript
searchTerm = '';
severityFilter = '';
statusFilter = '';
```

The complete incident list is then restored.

---

## 5.5 Filter Result Counter

The UI displays:

```text
Showing X of Y incidents
```

For example:

```text
Showing 3 of 8 incidents
```

This allows the analyst to immediately understand how many incidents match the current filters.

---

# 6. Incident Details

Selecting an incident opens the investigation panel.

The investigation panel displays:

* Incident ID.
* Category.
* Severity.
* Status.
* Reporter.
* Assigned analyst.
* Description.

Example:

```text
INCIDENT INVESTIGATION

Suspicious PowerShell Activity

Investigation details for incident #6
```

The selected incident is stored in:

```typescript
selectedIncident: Incident | null = null;
```

When an incident is selected:

```typescript
selectIncident(incident)
```

loads the investigation context and retrieves the associated investigation notes.

---

# 7. Investigation Workflow

The incident lifecycle implemented in SecureOps is:

```text
OPEN
  |
  v
ASSIGNED
  |
  v
INVESTIGATING
  |
  v
CONTAINED
  |
  v
RESOLVED
  |
  v
CLOSED
```

The workflow represents a simplified security incident-response lifecycle.

---

## 7.1 OPEN

An incident begins in the:

```text
OPEN
```

state.

The incident has been detected or reported but has not yet progressed through the investigation workflow.

---

## 7.2 ASSIGNED

The incident can be assigned to a security analyst.

```text
OPEN → ASSIGNED
```

The assignment identifies the analyst responsible for handling the incident.

---

## 7.3 INVESTIGATING

The incident can progress to:

```text
ASSIGNED → INVESTIGATING
```

This indicates that investigation activities are actively being performed.

---

## 7.4 CONTAINED

After investigation, the incident can progress to:

```text
INVESTIGATING → CONTAINED
```

Containment represents the stage where actions have been taken to limit or isolate the security impact.

---

## 7.5 RESOLVED

The next transition is:

```text
CONTAINED → RESOLVED
```

The security issue is considered resolved from the operational perspective.

---

## 7.6 CLOSED

Finally:

```text
RESOLVED → CLOSED
```

Once closed, the incident has no further status transition.

The frontend therefore displays:

```text
No further transition
```

for a closed incident.

---

# 8. Incident Actions

The investigation interface provides operational controls.

Two major actions are implemented:

1. Status transition.
2. Analyst assignment.

---

## 8.1 Status Update

The frontend determines the next valid status based on the current status.

The workflow is:

```text
OPEN → ASSIGNED
ASSIGNED → INVESTIGATING
INVESTIGATING → CONTAINED
CONTAINED → RESOLVED
RESOLVED → CLOSED
CLOSED → No transition
```

The frontend sends the selected transition to the backend.

The backend remains responsible for validating the operation.

This is important because frontend controls must not be treated as the security boundary.

---

# 9. Analyst Assignment

An incident can be assigned to a security analyst.

The UI loads users from the backend and selects users whose role is:

```text
SECURITY_ANALYST
```

Disabled users are excluded.

The interface displays:

```text
Select security analyst
```

After selecting an analyst, the application sends the assignment request to the backend.

The incident then displays the assigned analyst.

---

# 10. Investigation Notes

Day 08 also introduces persistent investigation notes.

Notes allow analysts to record:

* Observations.
* Findings.
* Investigation actions.
* Relevant security information.
* Investigation progress.

The UI provides a text area:

```text
Write an investigation note...
```

The note length is limited to:

```text
5000 characters
```

The frontend also validates that the note is not empty.

---

# 11. Investigation Note API

The backend provides two main endpoints.

### Get notes

```http
GET /api/incidents/{incidentId}/notes
```

This retrieves the investigation notes associated with an incident.

### Create note

```http
POST /api/incidents/{incidentId}/notes
```

This creates a new investigation note.

The frontend sends the note content to the backend.

The backend persists the note in PostgreSQL.

---

# 12. Investigation Note Authorization

Investigation notes use role-based access control.

The current authorization model is:

| Operation    | ADMIN | SECURITY_ANALYST | MANAGER |
| ------------ | ----: | ---------------: | ------: |
| View notes   |   Yes |              Yes |     Yes |
| Create notes |   Yes |              Yes |      No |

This separates investigation visibility from investigation modification.

Managers can review investigation information without receiving permission to create analyst notes.

---

# 13. Incident API Authorization

The incident API also uses RBAC.

The current model is:

| Operation        | ADMIN | SECURITY_ANALYST | MANAGER |
| ---------------- | ----: | ---------------: | ------: |
| View incidents   |   Yes |              Yes |     Yes |
| Update incidents |   Yes |              Yes |      No |

This means that incident modification is restricted to operational security roles.

---

# 14. Authentication

The Angular frontend uses JWT authentication.

The authentication flow is:

```text
User Login
    |
    v
Spring Boot Authentication API
    |
    v
JWT Token
    |
    v
Angular Token Storage
    |
    v
HTTP Interceptor
    |
    v
Authorization: Bearer <JWT>
    |
    v
Spring Boot API
```

Protected incident endpoints therefore receive the authenticated user's JWT.

The backend validates the token before allowing protected operations.

---

# 15. Error Handling

The incident interface handles several backend response conditions.

### HTTP 400

The requested operation is invalid.

Example:

```text
The requested incident action is invalid.
```

### HTTP 401

Authentication failed or the session expired.

Example:

```text
Authentication failed. Your session may have expired.
```

### HTTP 403

The authenticated user does not have the required permission.

Example:

```text
Access denied. You do not have permission to perform this action.
```

### HTTP 404

The requested incident or analyst does not exist.

Example:

```text
The incident or selected analyst was not found.
```

### HTTP 0

The frontend cannot connect to the backend.

Example:

```text
Cannot connect to the backend.
```

This provides a clearer operational experience than exposing raw HTTP errors to the analyst.

---

# 16. Loading States

The interface provides loading states for asynchronous operations.

Examples include:

```text
Loading incidents...
```

```text
Loading security analysts...
```

```text
Loading investigation notes...
```

During actions, buttons also communicate progress:

```text
Updating...
```

```text
Assigning...
```

```text
Adding Note...
```

This prevents the analyst from assuming that an operation failed when the backend is still processing the request.

---

# 17. Empty States

The interface handles multiple empty conditions.

### No incidents

```text
No Incidents

No security incidents have been recorded yet.
```

### No investigation notes

```text
No investigation notes yet.

Add the first note for this incident.
```

### No filter matches

```text
No Matching Incidents

No incidents match the current search and filters.
```

This makes the application behavior clear in each situation.

---

# 18. Dashboard Integration

The incident investigation page is integrated with the SecureOps dashboard.

The dashboard provides access to the incident investigation page.

The incident page also provides:

```text
← Dashboard
```

which navigates back to:

```text
/dashboard
```

The Angular Router is used for navigation.

This creates a simple workflow:

```text
Dashboard
    |
    v
Incident Investigation
    |
    v
Select Incident
    |
    v
Investigate
```

---

# 19. Frontend Components

The main Day 08 frontend implementation is located under:

```text
frontend/src/app/pages/incidents/
```

The main files are:

```text
incidents.ts
incidents.html
incidents.css
```

### incidents.ts

Responsible for:

* Component state.
* API calls.
* Filtering.
* Incident selection.
* Investigation notes.
* Status updates.
* Analyst assignment.
* Error handling.
* Navigation.

### incidents.html

Responsible for:

* Incident list.
* Search controls.
* Filters.
* Incident details.
* Investigation controls.
* Notes interface.
* Loading states.
* Error states.
* Empty states.

### incidents.css

Responsible for:

* Page layout.
* Incident table.
* Filter toolbar.
* Buttons.
* Status badges.
* Severity badges.
* Investigation panel.
* Notes.
* Responsive design.

---

# 20. Backend Integration

The Angular component communicates with the existing:

```text
IncidentService
```

and:

```text
UserService
```

The IncidentService provides operations for:

```text
Get incidents
Update incident status
Assign incident
Get investigation notes
Create investigation note
```

The UserService provides the user list required for analyst assignment.

---

# 21. Client-Side vs Server-Side Filtering

The Day 08 filtering implementation is intentionally client-side.

The backend returns the incident collection:

```text
GET /api/incidents
```

Angular stores it in:

```typescript
incidents
```

The frontend then produces:

```typescript
filteredIncidents
```

from the original collection.

Advantages for the current project:

* Simple implementation.
* No additional backend endpoint.
* Immediate filtering.
* No additional database query for every filter change.
* Suitable for the current project scale.

For a much larger production environment, server-side filtering and pagination could be introduced later.

---

# 22. Security Considerations

The filtering functionality itself is not a security boundary.

The frontend must never be trusted to enforce authorization.

For example, hiding an action button from a Manager does not provide sufficient security.

The backend must continue to enforce:

```text
Authentication
+
Authorization
+
Business rules
```

The architecture therefore follows:

```text
Frontend controls
        ↓
User experience

Backend authorization
        ↓
Security boundary
```

This is an important DevSecOps principle.

---

# 23. Testing Performed

Day 08 testing covered the major investigation workflow.

### Incident list

```text
PASS
```

Incidents can be loaded from the backend.

### Search

```text
PASS
```

The search field filters incident information.

### Severity filtering

```text
PASS
```

Severity filtering works for:

```text
CRITICAL
HIGH
MEDIUM
LOW
```

### Status filtering

```text
PASS
```

Status filtering works for:

```text
OPEN
ASSIGNED
INVESTIGATING
CONTAINED
RESOLVED
CLOSED
```

### Combined filters

```text
PASS
```

Search, severity, and status can be used together.

### Clear Filters

```text
PASS
```

The complete incident list returns after clearing filters.

### Incident selection

```text
PASS
```

Selecting an incident opens its investigation details.

### Status workflow

```text
PASS
```

The incident status workflow was tested through the available transitions.

### Analyst assignment

```text
PASS
```

Incident assignment to a security analyst was tested successfully.

### Investigation notes

```text
PASS
```

Existing notes can be loaded and new investigation notes can be created.

### Angular build

```text
PASS
```

The production Angular build completed successfully.

---

# 24. Build Verification

The frontend was verified using:

```powershell
npm run build
```

The build completed successfully.

The Angular build output was generated under:

```text
frontend/dist/frontend
```

This confirms that the Day 08 frontend changes compile successfully.

---

# 25. Day 08 Checklist

```text
DAY 08 — INCIDENT INVESTIGATION UI

[✓] Incident List
[✓] Incident Filtering
[✓] Incident Details
[✓] Investigation Workflow
[✓] Incident Actions
[✓] Security / RBAC
[✓] Dashboard Integration
[✓] Backend Integration
[✓] Testing
[✓] Documentation
[ ] Git Commit
[ ] Git Push
```

The final Git operations remain after documentation is saved.

---

# 26. Security Operations Workflow

The resulting workflow can be represented as:

```text
             SECURITY DASHBOARD
                     |
                     v
             INCIDENT DETECTED
                     |
                     v
              INCIDENT LIST
                     |
          +----------+----------+
          |                     |
          v                     v
       SEARCH                FILTER
          |                     |
          +----------+----------+
                     |
                     v
             SELECT INCIDENT
                     |
                     v
            INCIDENT DETAILS
                     |
          +----------+----------+
          |                     |
          v                     v
       ASSIGN               INVESTIGATE
          |                     |
          |                     v
          |              ADD INVESTIGATION
          |                    NOTES
          |                     |
          +----------+----------+
                     |
                     v
              STATUS UPDATE
                     |
                     v
                 CONTAINED
                     |
                     v
                 RESOLVED
                     |
                     v
                  CLOSED
```

This provides the foundation for a small SOC-style incident management workflow.

---

# 27. DevSecOps Value

Day 08 demonstrates several important DevSecOps concepts.

### Security visibility

Security incidents are visible through a dedicated operational interface.

### Incident response

Analysts can progress incidents through defined states.

### RBAC

Different users receive different operational permissions.

### Auditability

Incident actions and investigation notes provide an operational record that can later integrate with the platform's audit capabilities.

### Secure API design

The frontend communicates with authenticated backend APIs rather than directly accessing the database.

### Error handling

Security and operational failures are explicitly handled.

### Separation of responsibilities

The Angular frontend handles presentation while Spring Boot handles security, authorization, business rules, and persistence.

---

# 28. Future Improvements

Possible future enhancements include:

* Server-side filtering.
* Pagination.
* Sorting.
* Incident priority.
* Incident timeline.
* File/evidence attachments.
* IOC management.
* MITRE ATT&CK mapping.
* Automated incident enrichment.
* Incident comments and mentions.
* Investigation task management.
* Incident escalation.
* SLA tracking.
* Advanced audit trails.
* SIEM event correlation.
* Automated detection rules.
* Security alert ingestion.
* SOC workflow automation.

These features can be integrated into the later SecureOps roadmap.

---

# 29. Day 08 Result

Day 08 transforms the SecureOps Platform from a dashboard that **displays security information** into a system that supports a basic **security incident investigation workflow**.

The platform can now:

```text
View
  ↓
Search
  ↓
Filter
  ↓
Select
  ↓
Investigate
  ↓
Assign
  ↓
Record Notes
  ↓
Update Status
  ↓
Resolve
  ↓
Close
```

This creates the operational foundation required for the next stages of the project, particularly:

```text
Day 09 → Security Testing Automation
Day 10 → Security Automation & Workflows
Day 11 → SOC / SIEM Integration
Day 12 → DevSecOps Security Pipeline
Day 13 → Advanced Security & Detection
Day 14 → Final Integration & Documentation
```

---

# 30. Conclusion

Day 08 successfully implemented the Incident Investigation UI for SecureOps Platform.

The implementation combines:

* Angular.
* Spring Boot.
* JWT authentication.
* RBAC.
* REST APIs.
* PostgreSQL persistence.
* Incident state management.
* Client-side filtering.
* Investigation notes.
* Analyst assignment.
* Error handling.
* Loading states.
* Responsive UI.
* Build verification.

The result is a functional security-operations workflow that provides the foundation for future SOC, SIEM, automation, detection, and DevSecOps capabilities.