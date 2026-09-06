---
name: dependency-audit
description: Audit Maven, Spring Boot, Java, PostgreSQL, Redis, and container dependencies in JobApplicationLedger for vulnerabilities, compatibility, and justified upgrades. Use for dependency health, CVE investigation, or upgrade planning; do not trigger for routine feature implementation.
---

# Audit dependencies

Base conclusions on the resolved dependency graph and current authoritative evidence, not version numbers remembered from training data.

## Inventory

- Inspect `backend/pom.xml`, the Maven wrapper, Spring Boot dependency management, container image tags, and relevant build plugins.
- Resolve direct and transitive Maven dependencies with the wrapper when feasible. Distinguish versions managed by the Spring Boot BOM from explicit overrides.
- Record the effective Java and Spring Boot requirements before recommending upgrades.

## Investigate

- For vulnerability claims, use current primary sources such as vendor advisories, GitHub Security Advisories, OSV records, NVD entries, and official release notes. Cite the exact advisory and affected/fixed ranges.
- Confirm the vulnerable component and range are present in the resolved runtime or build path. Account for scope, reachability, configuration, mitigations, and whether the advisory is disputed.
- Check compatibility across Spring Boot, Java, Maven plugins, PostgreSQL, Redis, Flyway, Testcontainers, and application imports before proposing a version change.
- Do not recommend an upgrade solely because a newer release exists. Prefer the smallest supported upgrade that resolves a demonstrated risk or compatibility problem.

## Change and verify

- Preserve BOM management when possible and avoid unnecessary explicit version pins.
- Make upgrades in reviewable groups. Read migration notes for major or behavior-changing releases and update code or configuration they affect.
- Run targeted tests followed by `./mvnw test`; build the application when the dependency affects packaging or runtime startup.
- Never suppress an advisory without documenting the evidence and residual risk.

Report findings by severity and confidence, with current/resolved/fixed versions, exploitability context, recommended action, sources, and verification results.

