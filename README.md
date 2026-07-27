# TrustFlow

An identity verification workflow backend, built with Spring Boot to practice transaction management, concurrency, and testing on a Java backend service.

## Tech Stack

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL + Flyway
* RabbitMQ
* JUnit 5 + Mockito + Testcontainers

## Features

* User registration and login (JWT authentication)
* Submit verification request, view own requests
* Reviewer/admin: list pending requests, approve or reject
* Optimistic locking to prevent double-review
* DB constraint to prevent duplicate pending requests per user
* Audit log for every submit/approve/reject action
* Publish verification events to RabbitMQ after transaction commit

## Getting Started

Copy `.env.example` to `.env` and fill in the values. Postgres and RabbitMQ start automatically via docker-compose support.

```bash
./mvnw spring-boot:run
```

## Testing

Integration tests need Docker running and the env vars loaded:

```bash
set -a && source .env && set +a
./mvnw test
```

## Notes

* Stateless JWT auth, no session
* Race conditions (duplicate submit, double-review) handled at the DB layer, not just app layer
* Events published only after commit (`@TransactionalEventListener(phase = AFTER_COMMIT)`)
* Feature-based package structure
