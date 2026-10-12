# TutorGigs Backend

REST + WebSocket API for **TutorGigs**, a tutoring marketplace where tutors publish gigs and students bid, chat and manage bookings.

- **Live app:** https://final-tutor-app-frontend.onrender.com/
- **Live API:** https://final-tutor-app-backend.onrender.com (health check: `/api/health`)
- **Frontend repo:** https://github.com/johns-george/TutorGigs-Frontend

## Highlights

- **49 REST endpoints across 15 controllers**, organised by feature domain: Auth, Gig, Bid, Chat, Review, Wishlist, User, Admin.
- **Stateless auth:** JWT access + refresh tokens, role-based authorization (Spring Security), token blacklisting on logout, password-reset by email.
- **Real-time chat:** 1:1 and group chat over WebSocket (STOMP/SockJS). Messages are published through **Redis pub/sub**, so they reach clients connected to *any* server instance, not just the one that received them.
- **Hardening:** rate-limiting filter, centralised exception handling, bean validation on request DTOs.
- **API docs:** OpenAPI/Swagger UI at `/swagger-ui.html` (spec at `/v3/api-docs`).
- **Tests:** 40 JUnit 5 / MockMvc tests across 6 test classes, run against in-memory H2.

## Tech stack

Java 17 · Spring Boot 3.5 · Spring Security · Spring Data JPA/Hibernate · PostgreSQL · Redis · WebSocket (STOMP/SockJS) · JJWT · Flyway · springdoc-openapi · Maven · Docker

## Architecture

```
com.tutorapp.webapp
├── Auth/      login, signup, password reset, token blacklist
├── Gig/       create / explore / manage gigs
├── Bid/       place and withdraw bids
├── Chat/      chats, messages, group chat, Redis publisher/subscriber
├── Review/    ratings
├── Wishlist/  saved gigs
├── User/      student and tutor profiles
├── Admin/     admin operations
└── Common/    security config, JWT filter, rate-limit filter, WebSocket + Redis config, exception handling
```

```
React client ──REST──▶ Spring Boot ──JPA──▶ PostgreSQL
      │                    │
      └──WebSocket(STOMP)──┤
                           └──pub/sub──▶ Redis ──▶ every server instance ──▶ subscribed clients
```

## Run locally

Prerequisites: JDK 17, PostgreSQL, Redis.

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/tutorgigs
export DB_USERNAME=postgres
export DB_PASSWORD=<your-password>
export JWT_SECRET=<long-random-string>
export MAIL_USERNAME=<smtp-user>
export MAIL_PASSWORD=<smtp-app-password>
# optional: REDIS_HOST (localhost), REDIS_PORT (6379), SERVER_PORT (8080), FRONTEND_URL (http://localhost:5173)

./mvnw spring-boot:run
```

API: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html

## Tests

```bash
./mvnw test
```

## Docker

```bash
./mvnw -DskipTests package
docker build -t tutorgigs-backend .
docker run -p 8080:8080 --env-file .env tutorgigs-backend
```

## CI

GitHub Actions (`.github/workflows/ci.yml`) builds the project and runs the test suite on every push and pull request.

## Configuration reference

| Variable | Required | Purpose |
|---|---|---|
| `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` | yes | PostgreSQL connection |
| `JWT_SECRET` | yes | Signing key for JWTs |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | yes | SMTP for password-reset emails |
| `REDIS_HOST`, `REDIS_PORT` | no | Redis for chat pub/sub (defaults: `localhost`, `6379`) |
| `FRONTEND_URL` | no | Allowed CORS origin (default `http://localhost:5173`) |
| `SERVER_PORT` | no | HTTP port (default `8080`) |

## Author

Johns George · [LinkedIn](https://www.linkedin.com/in/johns-george-3568b8233/) · [GitHub](https://github.com/johns-george)
