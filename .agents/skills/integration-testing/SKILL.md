---
name: integration-testing
description: Design, implement, or diagnose Spring Boot tests for JobApplicationLedger, including MVC security tests and PostgreSQL or Redis Testcontainers integration tests. Use when adding coverage, reproducing a backend bug, or verifying cross-layer behavior; do not use for production implementation alone.
---

# Test Spring behavior

Choose the cheapest test boundary that proves the requested behavior without mocking away the risk.

## Select the boundary

- Use a plain unit test for isolated domain or utility logic.
- Use an MVC slice for routing, validation, JSON contracts, authentication failures, authorization, sessions, and CSRF behavior.
- Use a persistence or full application test when JPA mappings, Flyway SQL, transactions, PostgreSQL behavior, Redis sessions, or multiple layers are material.
- Reuse the repository's service connections and Testcontainers configuration rather than introducing parallel container setup.

## Build useful tests

- Reproduce a reported bug with a failing test before changing production code when practical.
- Assert observable behavior and stable contracts, not private implementation details.
- Cover realistic negative paths: unauthenticated and unauthorized access, missing CSRF tokens for state changes, invalid input, missing records, duplicate operations, and cross-user access when applicable.
- Keep data isolated and deterministic. Avoid dependence on test order, local databases, fixed ports, wall-clock timing, or external networks.
- Let Flyway initialize integration-test schemas. Do not hand-create a competing schema in test setup.
- Use PostgreSQL and Redis when their semantics matter; do not substitute an in-memory implementation that can hide dialect, constraint, transaction, or session defects.

## Run and report

- From `backend`, run a targeted test with `./mvnw -Dtest=ClassName test`, then run `./mvnw test` when feasible.
- If Docker or another required service is unavailable, preserve the tests and clearly identify what remains unverified. Do not weaken the test to make it pass locally.
- Summarize the behavior proven, commands and results, and any remaining coverage gap.

