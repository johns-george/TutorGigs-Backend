# TutorGigs Backend

[![Backend CI](https://github.com/johns-george/TutorGigs-Backend/actions/workflows/ci.yml/badge.svg)](https://github.com/johns-george/TutorGigs-Backend/actions/workflows/ci.yml)

REST + WebSocket API for **TutorGigs**, a tutoring marketplace where tutors publish gigs and students bid, chat and manage bookings.

- **Live app:** https://final-tutor-app-frontend.onrender.com/
- **Live API:** https://final-tutor-app-backend.onrender.com (health check: `/api/health`)
- **Frontend repo:** https://github.com/johns-george/TutorGigs-Frontend

## Highlights

- **49 REST endpoints across 14 REST controllers**, organised by feature domain: Auth, Gig, Bid, Chat, Review, Wishlist, Student details, Admin, Health — plus **2 STOMP endpoints** (`/chat.sendMessage`, `/chat.ping`).
- **Stateless auth:** JWT access + refresh tokens, role-based authorization (Spring Security), token blacklisting on logout, RSA-OAEP encrypted passwords in transit, password-reset by email.
- **Real-time chat:** 1:1 and group chat over WebSocket (STOMP/SockJS). A message is committed first, then published to Kafka; a router reads the recipient's instance from Redis presence and delivers over that instance's WebSocket topic, so clients on **any** server instance receive live messages. Recipients who are offline get a short-lived Redis inbox entry.
- **Hardening:** Redis-backed fixed-window rate limiting (in-memory fallback), centralised exception handling, bean validation on request DTOs, `X-Forwarded-For` only behind a trusted proxy.
- **API docs:** OpenAPI/Swagger UI at `/swagger-ui.html` (spec at `/v3/api-docs`).
- **Tests:** 59 JUnit 5 / MockMvc tests across 14 test classes, run against in-memory H2.

## Tech stack

Java 17 · Spring Boot 3.5 · Spring Security · Spring Data JPA/Hibernate · PostgreSQL · Redis · Kafka · WebSocket (STOMP/SockJS) · JJWT · Flyway · springdoc-openapi · Maven · Docker

## Architecture

```
com.tutorapp.webapp
├── Auth/      login, signup, password reset, token blacklist (Redis + in-memory)
├── Gig/       create / explore / manage gigs
├── Bid/       place and withdraw bids
├── Chat/      chats, messages, group chat, Kafka delivery, presence
├── Review/    ratings
├── Wishlist/  saved gigs
├── User/      student and tutor profiles
├── Admin/     admin operations
└── Common/    security config, JWT filter, rate-limit filter + store, WebSocket/Redis/Kafka config, exception handling
```

```
React client ──REST──▶ Spring Boot ──JPA──▶ PostgreSQL
      │                    │
      └──WebSocket(STOMP)──┤
                           ├──presence──▶ Redis (which instance holds the socket)
                           └──publish───▶ Kafka `chat.message.created`
                                            └──▶ router ──▶ owning instance ──▶ socket
```

Sending a message is transactional: the message row is saved first and delivery runs after the commit, so a broker failure is logged instead of rolling the message back. `CHAT_KAFKA_ENABLED=false` falls back to the local STOMP broker (single instance only).

## Run locally

Prerequisites: JDK 17, PostgreSQL; Redis and Kafka if you want multi-instance chat.

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/tutorgigs
export DB_USERNAME=postgres
export DB_PASSWORD=<your-password>
export JWT_SECRET=<long-random-string>
export MAIL_USERNAME=<smtp-user>
export MAIL_PASSWORD=<smtp-app-password>
# optional: REDIS_HOST (localhost), REDIS_PORT (6379), SERVER_PORT (8080), FRONTEND_URL (http://localhost:5173),
#           KAFKA_BOOTSTRAP_SERVERS (localhost:29092), CHAT_KAFKA_ENABLED (true),
#           TRUST_FORWARDED_HEADERS (false)

./mvnw spring-boot:run
```

API: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html

## Configuration reference

| Variable | Required | Purpose |
|---|---|---|
| `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` | yes | PostgreSQL connection |
| `JWT_SECRET` | yes | Signing key for JWTs |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | yes | SMTP for password-reset emails |
| `REDIS_HOST`, `REDIS_PORT` | no | Token blacklist, rate limits, chat presence (defaults: `localhost`, `6379`) |
| `KAFKA_BOOTSTRAP_SERVERS` | no | Kafka for cross-instance chat delivery (default `localhost:29092`) |
| `CHAT_KAFKA_ENABLED` | no | `true` (default) routes chat through Kafka; `false` uses the local STOMP broker |
| `TRUST_FORWARDED_HEADERS` | no | Honor `X-Forwarded-For` for rate limiting (default `false`; enable only behind a trusted proxy) |
| `FRONTEND_URL` | no | Allowed CORS origin (default `http://localhost:5173`) |
| `SERVER_PORT` | no | HTTP port (default `8080`) |

## Tests

```bash
./mvnw verify   # 59 tests / 14 test classes
```

The suite runs on in-memory H2 with Kafka and Redis auto-configuration excluded, so it exercises neither a real PostgreSQL, Redis nor Kafka instance. Redis/in-memory store selection is covered by context tests for both configurations.

## Docker

```bash
./mvnw -DskipTests package
docker build -t tutorgigs-backend .
docker run -p 8080:8080 --env-file .env tutorgigs-backend
```

The full stack (Postgres, Redis, a single Kafka broker, backend, frontend) is described by the `docker-compose.yml` in the workspace root, which reads secrets from `.env` (not committed).

## CI

[GitHub Actions](https://github.com/johns-george/TutorGigs-Backend/actions/workflows/ci.yml) checks out the repo, sets up Temurin JDK 17 with the Maven cache and runs `./mvnw -B verify` on every push and pull request.

## Known limitations and next steps

- **Chat delivery is best-effort.** `ChatService` commits the message before publishing; a Kafka publish failure is logged and the message is not lost, but that delivery attempt is skipped, so recipients see it only when they reload the conversation.
- **Redis is optional, and the fallbacks are per instance.** Without Redis configured, `InMemoryTokenBlacklistService` and `InMemoryRateLimitStore` are used: a logout only invalidates the token on the instance that handled it, and rate-limit counters are per instance. If Redis *is* configured but unreachable, the Redis-backed stores throw instead of failing over at runtime.
- **Multi-instance chat needs both Redis and Kafka.** With `CHAT_KAFKA_ENABLED=false`, messages are broadcast only on the local STOMP broker. Offline delivery is a short-lived Redis inbox entry plus a log line — not a device push notification; there is no FCM/APNs integration.
- **Rate limiting** is a fixed window in Redis (a burst can straddle a window boundary) and a sliding window in memory. Only `POST /api/auth/login`, `POST /api/auth/signup` and `/api/forgot/**` are limited.
- **`X-Forwarded-For` is ignored by default.** Forwarded client IPs are only used when `TRUST_FORWARDED_HEADERS=true`.
- **Test coverage is backend-only and database-agnostic.** Tests run on H2 with Redis and Kafka auto-configuration excluded; nothing exercises a real PostgreSQL, Redis or Kafka instance.
- **Docker Compose is a single-node development setup:** one Kafka broker (replication factor 1), one Redis, one backend.
- **Not implemented:** earnings or payouts, email verification, and a frontend test suite.

## Author

Johns George · [LinkedIn](https://www.linkedin.com/in/johns-george-3568b8233/) · [GitHub](https://github.com/johns-george)
