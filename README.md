# EventFlow

EventFlow is a lightweight event management REST API built with Spring Boot, JWT authentication, and PostgreSQL. It provides user registration/authentication, event creation and management, category management, and a registration/waitlist flow.

## Features

- User authentication with JWT
- Admin and user roles (`USER`, `ADMIN`)
- Create, update, cancel events
- Register for events (confirmed / waitlist / cancelled)
- Category management (admin-only for create/update/delete)
- Global exception handling with structured error responses

## Tech stack

- Java 17
- Spring Boot 4 (Web, Data JPA, Security, Validation)
- Spring Security + JWT
- PostgreSQL
- Maven
- Lombok

## Prerequisites

- JDK 17
- Maven (or use the bundled `mvnw` / `mvnw.cmd`)
- PostgreSQL (or another supported DB)

## Quick start

1. Clone the repo

```bash
git clone <repo-url>
cd EventFlow
```

2. Configure the database and JWT secret in `src/main/resources/application.yaml` (see below)

3. Build and run (development):

```bash
./mvnw spring-boot:run
# Windows
mvnw.cmd spring-boot:run
```

Or build a jar and run:

```bash
./mvnw -DskipTests package
java -jar target/EventFlow-0.0.1-SNAPSHOT.jar
```

## Configuration

Edit the application configuration at [src/main/resources/application.yaml](src/main/resources/application.yaml#L1). Minimal example:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eventflow
    username: eventflow
    password: secret
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: YOUR_SECRET_KEY
  expirationMs: 86400000

server:
  port: 8080
```

There is also an SQL file for reference DDL: [ddl_Category.sql](ddl_Category.sql).

## Running tests

```bash
./mvnw test
# Windows
mvnw.cmd test
```

## API Overview

Authentication

- POST `/api/v1/register` — register a new user
- POST `/api/v1/login` — login and receive a JWT
- POST `/api/v1/admin/register` — register an admin
- POST `/api/v1/admin/login` — admin login

User endpoints

- GET `/api/v1/me` — get current user's profile (requires auth)
- PUT `/api/v1/me` — update profile
- POST `/api/v1/me/change-password` — change password

Events & Registrations (typical endpoints)

- GET `/api/v1/events` — list events (paging)
- GET `/api/v1/events/{id}` — event details
- POST `/api/v1/events` — create event (auth)
- PUT `/api/v1/events/{id}` — update event (owner)
- POST `/api/v1/events/{id}/cancel` — cancel an event (owner)

- GET `/api/v1/registrations` — list registrations (paging)
- POST `/api/v1/registrations` — create registration (register to event)

Note: some admin or owner-only endpoints are protected via role checks or method-level `@PreAuthorize` annotations.

Authentication: send the JWT in the `Authorization` header as `Bearer <token>` for protected endpoints.

## Admin

- Methods that modify categories and certain management actions are restricted to users with the `ADMIN` role (`@PreAuthorize("hasRole('ADMIN')")`).
- Create an admin via the admin register endpoint (`/api/v1/admin/register`) or set the role directly in the DB for testing.

## Project structure (high level)

- `src/main/java/.../controller` — REST controllers
- `src/main/java/.../service` — business logic (AuthService, UserService, EventService, RegistrationService, CategoryService)
- `src/main/java/.../repository` — Spring Data repositories
- `src/main/java/.../model` — JPA entities
- `src/main/java/.../dto` & `.../dtos` — request/response DTOs
- `src/main/resources` — application configuration and static assets

## Tips

- If you change entity mappings, adjust `spring.jpa.hibernate.ddl-auto` or manage schema migrations with Flyway/Liquibase.
- To debug authentication issues, check the JWT secret in `application.yaml` and ensure the token is sent as `Authorization: Bearer <token>`.

## Contributing

Contributions are welcome. Open PRs against the main branch and include tests for new behavior.

## License

This project does not include a license file; add one if you plan to publish this repository.
