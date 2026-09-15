---
name: privacy-data-lifecycle
description: Design or review personal-data handling in JobApplicationLedger, including collection, ownership, logs, retention, export, correction, deletion, and third-party disclosure. Use for features involving users, login history, IP addresses, job applications, notes, analytics, or privacy requests; do not use for general legal advice.
---

# Protect personal data through its lifecycle

Evaluate the concrete data flow and turn privacy requirements into testable engineering behavior. Do not present the result as a definitive legal determination.

## Map the data

- Identify each personal or potentially sensitive field, its source, purpose, owner, storage location, recipients, and retention trigger.
- Treat account identifiers, authentication history, IP addresses, application notes, recruiter interactions, compensation details, and inferred job-search activity as personal data unless established otherwise.
- Minimize collection and response payloads. Do not retain a field merely because it may become useful.

## Enforce lifecycle and access

- Scope reads, writes, exports, and deletions to the authenticated owner or an explicitly authorized administrative role.
- Prevent cross-user object access at the query or service boundary, not only in the UI.
- Keep personal data, credentials, session identifiers, CSRF tokens, and detailed authentication failures out of logs, metrics labels, traces, and error responses.
- Define retention and deletion behavior for primary rows, child records, login history, caches, sessions, backups, and third-party copies. Check cascade behavior deliberately.
- Make access, correction, export, deletion, consent, or opt-out behavior discoverable and testable when the feature requires it.
- Apply encryption, secret management, and transport protections appropriate to the data and environment.

## Compliance research

- When the request depends on current GDPR or Texas requirements, consult current primary legal or regulator sources and cite them. Separate sourced requirements from engineering recommendations and unresolved applicability questions.
- Escalate questions about jurisdiction, exemptions, controller/processor roles, deadlines, or legal basis for qualified privacy or legal review.

## Deliver

Provide a compact data-flow summary, concrete risks, recommended controls, tests or evidence, and open legal/product decisions. Prioritize exposure, unauthorized access, inability to honor lifecycle requests, and excessive retention over cosmetic policy wording.

