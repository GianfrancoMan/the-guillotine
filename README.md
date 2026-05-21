# The Guillotine 🎯

A backend system for a real-time competitive quiz platform inspired by TV game shows, where users submit a single-word answer within a short time window, and the system determines the first correct submission in a fair and deterministic way.

---

## 🚀 Project Goal

The goal of this system is to reliably manage high-concurrency quiz sessions where:

- Users submit answers in a very short time window
- Only one answer per user per quiz is allowed
- The earliest correct answer must be selected as the winner
- The system must remain fair under heavy concurrent load

This requires strong guarantees in:
- correctness
- consistency
- concurrency safety
- ordering determinism

---

## 🧠 Key Design Principles

### ✔ Database-first correctness
The system relies on PostgreSQL as the source of truth:
- Deterministic ordering via timestamps
- Transactional consistency
- Constraints for data integrity
- Indexes for efficient winner resolution queries

### ✔ Concurrency-safe design
- Multiple simultaneous submissions are safely handled
- Race conditions are resolved at database level
- Unique constraints enforce one submission per user per quiz

### ✔ Security-first approach
- JWT-based authentication
- Role-based access control (admin vs user)
- Users cannot impersonate others

### ✔ Traffic protection
- Rate limiting via Bucket4j
- Protection against spam and burst traffic during live quiz windows

---

## 🏗️ Architecture Overview

The system is layered as follows:

- **REST API Layer** → exposes endpoints
- **Service Layer** → business logic
- **Persistence Layer (JPA/Hibernate)** → database interaction
- **Security Layer (Spring Security + JWT)** → authentication & authorization
- **Infrastructure Layer** → rate limiting and request control

---

## 🧱 Tech Stack

- Java 25
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway (database migrations)
- Bucket4j (rate limiting)
- Maven

---

## 🗄️ Database Model

### Users
- Stores platform users
- Unique username constraint

### Quizzes
- Represents quiz sessions
- Stores correct answer and metadata

### Submissions
- Stores user answers
- Enforces:
  - one submission per user per quiz
  - indexed ordering for fast winner resolution

---

## ⚙️ Core Flow

1. Admin creates a quiz
2. Quiz is opened for submissions
3. Users submit answers (1 per quiz)
4. System stores all submissions with timestamps
5. When quiz closes:
   - system filters correct answers
   - selects the earliest submission as winner

---

## 🧪 Testing Strategy

The project includes:

### Integration Tests
- REST API validation
- Security and JWT authentication
- End-to-end request flows

### Concurrency Tests
- Simultaneous submissions
- Winner correctness under load
- Deterministic ordering validation

### Rate Limiting Tests
- Request throttling validation
- User isolation
- Token refill behavior

---

## 🛡️ Rate Limiting

Implemented using Bucket4j:

- Login endpoint: IP-based throttling
- Submission endpoint: user-based throttling
- Prevents abuse during live events
- Ensures fair usage under load

---

## 📦 Key Features

- Real-time competitive quiz handling
- Deterministic winner selection
- Strong concurrency safety
- JWT-based authentication
- Role-based access (admin/user)
- Rate limiting protection
- Fully tested integration layer

---

## 📌 Important Architectural Insight

Correctness is not guaranteed by application code alone.

It depends heavily on:
- PostgreSQL transactional guarantees
- proper indexing strategy
- constraint enforcement
- deterministic query ordering
- concurrency-safe design

---

## 🔮 Future Improvements

- Distributed rate limiting (Redis-based)
- WebSocket live quiz updates
- Event sourcing for quiz history
- Horizontal scaling support
- Observability (metrics + tracing)

---

## 📜 License

This project is for educational and portfolio purposes.

---

## 👨‍💻 Author

I built the system by focusing on backend architecture, proper concurrency management, and real-time system design.
