\# Project Name: SecureID



Production-grade Identity and Access Management platform.



\## Tech Stack



\- Java 21

\- Spring Boot 4.1

\- Spring Cloud Gateway

\- Spring Security

\- React.js

\- PostgreSQL

\- Redis

\- Kafka

\- OAuth 2.0

\- OpenID Connect

\- Keycloak

\- Docker



\## Architecture



SecureID follows a microservice architecture.



\### Services



\- API Gateway

\- Auth Service

\- User Service



&#x20; Additional services will be introduced incrementally as the platform evolves.

\- Audit Service

\- Notification Service

\- etc.



\## Development



Start/run infrastructure:

docker compose -f infrastructure/docker/docker-compose.yml up -d



Build:

mvn clean verify

