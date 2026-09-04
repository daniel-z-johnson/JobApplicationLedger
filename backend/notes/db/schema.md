# JobLedger Database Schema

## Current Schema

The schema below is the current working design for JobLedger.

### users

```sql
CREATE TABLE users (
    id              BIGINT PRIMARY KEY,
    email           VARCHAR(320) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
```

### companies

```sql
CREATE TABLE companies (
    id              BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    company_type    VARCHAR(30) NOT NULL,
    website_url     TEXT,
    careers_url     TEXT,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,

    CONSTRAINT fk_companies_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uq_companies_user_name
        UNIQUE (user_id, name)
);
```

### applications

```sql
CREATE TABLE applications (
    id                  BIGINT PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    company_id              BIGINT NOT NULL,
    recruiter_company_id    BIGINT,
    job_title               VARCHAR(255) NOT NULL,
    description             TEXT,
    job_url                 TEXT,
    status                  VARCHAR(50) NOT NULL,
    location                VARCHAR(255),
    work_arrangement        VARCHAR(20) NOT NULL,
    salary_min              NUMERIC(12, 2),
    salary_max          NUMERIC(12, 2),
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    applied_at          TIMESTAMP,
    last_activity_at    TIMESTAMP,

    CONSTRAINT fk_applications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_applications_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_applications_recruiter_company
        FOREIGN KEY (recruiter_company_id)
        REFERENCES companies(id),

    CONSTRAINT chk_applications_salary_range
        CHECK (
            salary_min IS NULL
            OR salary_max IS NULL
            OR salary_min <= salary_max
        )
);
```

### application_updates

```sql
CREATE TABLE application_updates (
    id                  BIGINT PRIMARY KEY,
    application_id      BIGINT NOT NULL,
    update_type         VARCHAR(50) NOT NULL,
    previous_status     VARCHAR(50),
    new_status          VARCHAR(50),
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,

    CONSTRAINT fk_application_updates_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id),

    CONSTRAINT chk_application_updates_status_change
        CHECK (
            update_type = 'STATUS_CHANGE'
            OR (
                previous_status IS NULL
                AND new_status IS NULL
            )
        )
);
```

### application_update_notes

```sql
CREATE TABLE application_update_notes (
    id                      BIGINT PRIMARY KEY,
    application_update_id   BIGINT NOT NULL,
    note                    TEXT NOT NULL,
    created_at              TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP NOT NULL,

    CONSTRAINT fk_application_update_notes_update
        FOREIGN KEY (application_update_id)
        REFERENCES application_updates(id)
);
```


### application_tasks

```sql
CREATE TABLE application_tasks (
    id              BIGINT PRIMARY KEY,
    application_id  BIGINT NOT NULL,
    description     TEXT NOT NULL,
    due_at          TIMESTAMP,
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,

    CONSTRAINT fk_application_tasks_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id)
);
```

## Recommended Indexes

```sql
CREATE INDEX idx_applications_user_last_activity
    ON applications(user_id, last_activity_at);

CREATE INDEX idx_applications_company
    ON applications(company_id);

CREATE INDEX idx_application_updates_application_created
    ON application_updates(application_id, created_at DESC);

CREATE INDEX idx_application_update_notes_update
    ON application_update_notes(application_update_id);

CREATE INDEX idx_application_tasks_application_due
    ON application_tasks(application_id, due_at);
```

## Relationships

```text
User
 ├──< Company
 └──< Application
       ├──> Company (target employer)
       ├──> Company (recruiting company, optional)
       ├──< ApplicationUpdate
       │      └──< ApplicationUpdateNote
       └──< ApplicationTask
```

The main relationships are:

- One user can have many companies.
- One user can have many applications.
- One company can be the target employer for many applications.
- One company can also act as the recruiting or staffing company for many applications.
- One application can optionally reference a recruiting company.
- One application can have many application updates.
- One application update can have many notes.
- One application can have many tasks.

---

# Notes and Design Decisions

## Users

Passwords must never be stored in plaintext.

`password_hash` should contain only the result produced by the application's configured password hashing algorithm.

`email` is unique because it is expected to identify the account used to sign in.

---

## Companies

`website_url` and `careers_url` serve different purposes.

- `website_url` is the company's primary corporate website.
- `careers_url` is the company's general careers page or hiring portal.

Keeping these separate is useful because many companies use a dedicated hiring domain or a third-party applicant tracking system.

The following constraint:

```text
UNIQUE(user_id, name)
```

prevents a single user from accidentally creating duplicate company records while allowing different users to independently track the same company.

Company notes are intentionally not part of the current core schema. A separate `company_notes` table can be added later if multiple company-level notes become useful.

---

## Applications

Each application belongs to both a user and a company.

Although the application's owner could technically be found through:

```text
Application -> Company -> User
```

`user_id` is also stored directly on `applications`.

This simplifies authorization and user-specific queries:

```sql
SELECT *
FROM applications
WHERE id = :applicationId
  AND user_id = :userId;
```


### Recruiting Company

`company_id` represents the employer or organization the user is applying to.

`recruiter_company_id` is optional and represents a staffing or recruiting company involved in the application process.

Example:

```text
Target company:      JPMorgan Chase
Recruiting company:  TEKsystems
```

For a direct application, `recruiter_company_id` remains `NULL`.

The Spring service layer should ensure that the application, target company, and recruiting company all belong to the same user.

### Work Arrangement

`work_arrangement` describes where the role is expected to be performed.

Initial values:

```text
REMOTE
HYBRID
ONSITE
```

This belongs on the application rather than the company because one company may have different work arrangements for different roles.

### Job Description

`description` uses a large text field so the full job posting can be saved.

This is useful because employers frequently remove job postings after they stop accepting applications, even when an existing candidate is still active in the hiring process.

The original `job_url` should generally remain stored even if the URL later stops working.

### Timestamp Semantics

The four timestamps on an application intentionally represent different concepts.

| Column | Meaning |
| --- | --- |
| `created_at` | When the application record was created in JobLedger |
| `updated_at` | When fields on the application record were last edited |
| `applied_at` | When the application was actually submitted |
| `last_activity_at` | When meaningful activity last occurred |

Example:

```text
Application record created:       August 1
Application submitted:            August 3
Recruiter contacted user:         August 8
Job title typo corrected:         August 20
```

The timestamps could then be:

```text
created_at        August 1
applied_at        August 3
last_activity_at  August 8
updated_at        August 20
```

Correcting a typo should not cause an inactive application to appear active again.

For that reason, stale application detection should use `last_activity_at`, not `updated_at`.

`applied_at` may be `NULL` for an opportunity that has been saved but not yet submitted.

### Application Status

The initial application status values are expected to be:

```text
INTERESTED
APPLIED
RECRUITER_CONTACT
INTERVIEWING
OFFER
REJECTED
WITHDRAWN
GHOSTED
```

These can be represented as a Java enum and stored as strings.

`GHOSTED` and `stale` are different concepts.

`GHOSTED` is an application status. Stale is derived from inactivity.

For example, an application can still have a status of `INTERVIEWING` while also being stale if no meaningful activity has occurred for more than a week.

---

## Application Updates

`application_updates` stores meaningful historical events for an application.

Examples include:

- status changes
- recruiter contact
- interviews
- general hiring-process updates

The application timeline should normally be shown newest first:

```sql
SELECT *
FROM application_updates
WHERE application_id = :applicationId
ORDER BY created_at DESC;
```

### Update Types

The initial update types are:

```text
STATUS_CHANGE
CONTACT
INTERVIEW
GENERAL
```

The list should stay small initially. Additional update types can be added later when they provide a clear benefit.

### Status Changes

Changing an application's status should create an application update.

This rule belongs in the Spring service layer rather than a database trigger.

A status update might contain:

```text
update_type      STATUS_CHANGE
previous_status  APPLIED
new_status       INTERVIEWING
```

For non-status updates, `previous_status` and `new_status` should remain `NULL`.

A typical service transaction should:

1. Load the application.
2. Capture the current status.
3. Change the application status.
4. Create a `STATUS_CHANGE` application update.
5. Store the previous and new statuses.
6. Update `last_activity_at`.
7. Commit all changes in one transaction.

The database check constraint ensures that non-status updates cannot accidentally contain status transition fields.

Service-layer validation should additionally ensure that a `STATUS_CHANGE` record contains valid previous and new statuses.

---

## Application Update Notes

An application update can have multiple notes.

For example:

```text
Update: Technical Interview
Date: September 4

Notes:
- Interview with engineering manager
- Asked about Spring transactions and Kafka
- Review Kafka consumer groups before the next round
```

Using a separate table avoids putting several unrelated notes into one large field.

It also allows notes to be added or edited independently of the underlying historical event.

---


## Application Tasks

`application_tasks` represents future actions associated with an application.

Examples include:

```text
Follow up with recruiter
Prepare system design examples
Send thank-you email
Check for an expected response
```

Tasks are intentionally separate from `application_updates`.

An application update answers:

> What happened?

An application task answers:

> What should I do next?

### Task State

A separate task status column is not currently necessary.

Task state can be derived from timestamps:

```text
completed_at IS NULL      -> OPEN
completed_at IS NOT NULL  -> COMPLETED
```

A task is overdue when:

```text
due_at < current time
AND completed_at IS NULL
```

This avoids storing redundant state that could become inconsistent.

## Stale Applications

Staleness is derived state and should not currently be stored as a boolean column.

The initial rule is:

> An active application is stale when it has had no meaningful activity for more than seven days.

Example PostgreSQL query:

```sql
SELECT *
FROM applications
WHERE user_id = :userId
  AND last_activity_at < CURRENT_TIMESTAMP - INTERVAL '7 days'
  AND status NOT IN ('REJECTED', 'WITHDRAWN', 'OFFER')
ORDER BY last_activity_at ASC;
```

The exact list of terminal statuses may change over time.

The seven-day threshold should eventually be configurable instead of duplicated as a hard-coded value throughout the application.

One case that will need to be handled explicitly is an application whose `last_activity_at` is `NULL`. For a newly created opportunity, the application may eventually use `created_at` or `applied_at` as the fallback activity date until its first update is recorded.

---

## Application History vs. Record Edits

An `application_update` represents something meaningful that happened during the hiring process.

Examples that should create an application update:

```text
Recruiter contacted me
Interview scheduled
Interview completed
Application status changed
Hiring manager requested additional information
```

Ordinary corrections to application metadata should not:

```text
Fixed a typo in the job title
Corrected the location
Updated a broken URL
Edited the saved job description
```

This distinction is the reason both `updated_at` and `last_activity_at` exist.

---

## Possible Future Tables

These are not part of the current schema.

### company_notes

A future `company_notes` table could support multiple notes about a company, such as:

- hiring process observations
- recruiter history
- office or hybrid-work information
- previous interview experience

