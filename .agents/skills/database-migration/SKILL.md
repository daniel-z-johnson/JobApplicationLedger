---
name: database-migration
description: Design or review PostgreSQL schema changes and Flyway migrations for JobApplicationLedger, including JPA alignment, constraints, indexes, and safe data transitions. Use when changing persistent models or SQL; do not use for query-only application changes with no schema impact.
---

# Change the database safely

Treat committed versioned migrations as an append-only history that may already be applied.

## Inspect first

- Read all existing migrations in version order, the affected JPA entities and repositories, and `backend/notes/db/schema.md` when it describes the intended design.
- Distinguish the desired model from the currently deployed schema. The next migration must transform the latter into the former.
- Identify table size, nullability, existing rows, uniqueness, foreign-key behavior, transaction constraints, and application compatibility assumptions that affect rollout safety.

## Write the migration

- Add a new, uniquely numbered `V*__*.sql` migration. Do not edit an existing migration unless the user explicitly confirms it has never been applied anywhere that matters.
- Prefer database-enforced invariants for durable rules: primary keys, foreign keys, uniqueness, checks, and appropriate nullability.
- Preserve user ownership in keys, constraints, and indexes where it is part of the access model.
- For populated tables, plan backfills and constraint tightening so existing rows remain valid. Avoid destructive column or table changes without an explicit data-preservation decision.
- Add indexes from demonstrated query and ordering needs. Check column order and avoid redundant indexes.
- Keep JPA mappings, repository queries, and schema documentation consistent with the migration.
- Never place production credentials or environment-specific values in migration SQL.

## Verify

- Run migration-aware application tests against PostgreSQL through Testcontainers when available.
- Verify both an empty-database migration path and the logical upgrade path from the previous schema when the change is risky.
- Review generated queries or use `EXPLAIN` for performance-motivated indexes when representative data is available.
- Report compatibility assumptions, locking or backfill risk, verification performed, and a forward-fix or rollback strategy appropriate to the change.

