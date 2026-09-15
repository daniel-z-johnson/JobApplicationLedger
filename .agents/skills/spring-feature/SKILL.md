---
name: spring-feature
description: Implement an end-to-end backend feature in this JobApplicationLedger repository using Spring Boot, Spring MVC, Security, JPA, PostgreSQL, Redis sessions, and Flyway. Use for new endpoints or domain behavior; do not use for isolated bug fixes or review-only requests.
---

# Implement a Spring feature

Deliver the smallest complete vertical slice that satisfies the request and preserves the repository's existing contracts.

## Understand the slice

- Inspect the relevant controller, DTOs, services, repositories, entities, exception handling, security configuration, migrations, and tests before editing.
- Confirm the feature's owner and authorization boundary. User-owned data must be queried and mutated through the authenticated user's identifier; do not rely on an identifier supplied by the client to establish ownership.
- Reuse established package structure and response/error shapes. Do not expose JPA entities or password hashes through the API.

## Implement

- Validate request DTOs at the HTTP boundary and enforce business invariants in the service layer.
- Keep controllers focused on HTTP and authentication concerns. Put transaction boundaries around service operations that must succeed atomically.
- Preserve session authentication and CSRF behavior. Explicitly classify new routes as public or authenticated; default to authenticated when the product requirement is silent.
- Normalize values used as stable identifiers consistently with existing behavior.
- If persistence changes, add a new Flyway migration and update the JPA mapping in the same change. Never silently depend on Hibernate to evolve the schema.
- Convert expected failures into the repository's structured API error response without disclosing credentials, tokens, internal SQL, or personal data.

## Verify

- Add focused tests for the happy path, validation, authorization/ownership, absence, conflict, and changed failure behavior that materially apply.
- Run the narrowest relevant Maven test first from `backend`, then run `./mvnw test` before handoff when the environment supports Docker-backed tests.
- Report the implemented behavior, migrations or contract changes, tests run, and any unverified assumption.

