---
name: release-readiness
description: Assess and improve release readiness for JobApplicationLedger across Maven builds, tests, Spring profiles, secrets, PostgreSQL, Redis, Flyway, Actuator, sessions, and Docker Compose. Use for pre-release checks or production-readiness work; do not deploy or mutate live environments unless explicitly requested.
---

# Assess release readiness

Produce evidence-backed release status and fix in-scope repository issues when the user requests implementation.

## Establish the target

- Determine the intended environment and release artifact. Keep local-development convenience separate from production requirements.
- Inspect the Maven build, application profiles, Docker configuration, Flyway migrations, security configuration, observability endpoints, and operational documentation.

## Check release blockers

- Build and test: reproducible wrapper-based build, passing tests, Java/runtime compatibility, and no dependence on generated local artifacts.
- Configuration: secrets supplied outside source control, explicit production datasource/Redis settings, safe session-cookie attributes, trusted proxy handling, and no accidental development profile.
- Security: least-privilege route access, protected management endpoints, CSRF/session behavior, sanitized errors and logs, and no default credentials exposed beyond local development.
- Data: migrations run before incompatible code paths, backups and recovery expectations are stated, destructive transitions are identified, and startup failure is understandable.
- Reliability: PostgreSQL and Redis health/dependency behavior, timeouts, connection limits, graceful failure, and restart behavior.
- Observability: useful health signals and logs without exposing secrets or personal data; do not expose every Actuator endpoint by default in production.
- Packaging: only required source and configuration enter the artifact; local files such as cookies, IDE state, build output, or archives are excluded.

## Verify and report

- Run `./mvnw test` and the relevant packaging command from `backend` when feasible. Exercise container startup and a minimal authenticated smoke path when requested and supported.
- Do not claim production readiness from static inspection alone. Mark checks as passed, blocked, failed, or not assessed and attach the command or file evidence.
- Do not publish, deploy, rotate credentials, or modify external infrastructure without explicit authorization.
- End with a release recommendation, blocking issues, non-blocking follow-ups, and exact unverified assumptions.

