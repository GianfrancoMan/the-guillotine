# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
mvn spring-boot:run          # Run the application
mvn clean install            # Full build including tests
mvn clean package -DskipTests  # Package without tests
mvn test                     # Run all tests
mvn test -Dtest=ClassName    # Run a single test class
mvn test -Dtest=ClassName#methodName  # Run a single test method
```

Integration tests require no external setup — they use **Testcontainers** to spin up a PostgreSQL 16 container automatically.

## Environment & Configuration

- **Java 25**, Spring Boot 4.0.6
- **Database**: PostgreSQL on `localhost:5432/guillotinedb` (user: `gianfo`, pass: `pstgrpsw`)
- **JWT secret**: must be set via the `KEY256` environment variable
- **Migrations**: Flyway handles schema evolution; Hibernate is in `validate` mode (never auto-generates DDL)
- **Profiles**: integration tests use `@ActiveProfiles("test")`, which activates `application-test.yaml`

## Architecture

The app is a Spring Boot REST API for a real-time quiz competition system ("The Guillotine").

### Domain Model

Three core entities:

- **User** — `ADMIN` or `PLAYER` role; username normalized to lowercase; password BCrypt-encoded
- **Quiz** — lifecycle: `CREATED → OPEN → CLOSED → REVEALED`; correct answer is set only at close time
- **Submission** — one per user per quiz (enforced at DB level via unique constraint); answer is normalized (trim + lowercase); winner is the first correct submission by timestamp+id

### Layer Structure

```
org.gfmanca.the_guillotine
├── controller/      REST endpoints, request validation
├── service/         Business logic and transactions
├── repository/      Spring Data JPA
├── domain/
│   ├── entity/      JPA entities: User, Quiz, Submission
│   └── enums/       QuizStatus, UserRole
├── dto/             Request/response bodies
├── security/        JWT filter, SecurityConfig, UserDetailsService
├── exception/       Custom exceptions and global handler
└── rate_limit/      Bucket4j configuration and filters
```

### Key Design Decisions

- **Concurrent submissions**: correctness under race conditions is guaranteed by the DB-level unique constraint `(user_id, quiz_id)`, not application-level locking. The service catches `DataIntegrityViolationException` for duplicate attempts.
- **Rate limiting**: login is limited to 5 requests/IP; submission to 3 requests/authenticated user. Implemented with Bucket4j in-memory buckets keyed on IP or username.
- **Correlation IDs**: every request gets a UUID correlation ID injected by `CorrelationIdFilter` and propagated through logs via MDC.
- **Winner determination**: `SubmissionRepository` query orders by `submittedAt` then `id` to break ties deterministically.

### API Surface

| Method | Path | Auth |
|--------|------|------|
| POST | `/api/users` | Public |
| POST | `/api/auth/login` | Public (rate-limited) |
| POST | `/api/submissions` | Authenticated (rate-limited) |
| GET | `/api/submissions/winner/{quizId}` | Authenticated |
| POST | `/api/admin/quizzes` | ADMIN |
| PATCH | `/api/admin/quizzes/{id}/open` | ADMIN |
| PATCH | `/api/admin/quizzes/{id}/close` | ADMIN |
| PATCH | `/api/admin/quizzes/{id}/answer` | ADMIN |
