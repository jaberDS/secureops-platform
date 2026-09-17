# Day 02 — Full-Stack Application Foundation

## 1. Objective

The objective of Day 2 was to build the first working version of the SecureOps Platform.

The application contains:

- Angular frontend
- Spring Boot backend
- PostgreSQL database
- Docker
- REST API

The goal was to create a complete request flow:

Browser → Angular → Spring Boot → PostgreSQL

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| Frontend | Angular |
| Backend | Spring Boot |
| Language | Java |
| Database | PostgreSQL |
| Database container | Docker |
| API | REST |
| ORM | Spring Data JPA / Hibernate |
| Build tool | Maven |
| Version control | Git / GitHub |

---

## 3. Application Architecture

```text
┌──────────────────────────────┐
│       Angular Frontend       │
│         localhost:4200       │
└──────────────┬───────────────┘
               │ HTTP
               ↓
┌──────────────────────────────┐
│       Spring Boot API        │
│         localhost:8081       │
└──────────────┬───────────────┘
               │ JPA / Hibernate
               ↓
┌──────────────────────────────┐
│       PostgreSQL Database    │
│         localhost:5432       │
└──────────────────────────────┘