# IAM Security

## Purpose

SecureOps Platform uses IAM (Identity and Access Management) to control:

- User identity
- Authentication
- User roles
- Account status
- Password security
- Access control
- JWT authentication

## User Lifecycle

A user can have one of these account states:

- ACTIVE
- DISABLED

Disabled users cannot authenticate.

Existing JWT tokens are also rejected when the account becomes disabled.

## Roles

The application currently supports:

- ADMIN
- SECURITY_ANALYST
- MANAGER
- EMPLOYEE

Roles are used to implement Role-Based Access Control (RBAC).

## Authentication

Authentication uses:

1. Email
2. Password
3. BCrypt password hashing
4. JWT token

Passwords are never stored as plain text.

## Email Normalization

Emails are normalized before authentication and storage.

Example:

```text
 JABER@EXAMPLE.COM