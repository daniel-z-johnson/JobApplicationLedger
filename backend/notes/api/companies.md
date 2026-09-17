# Companies API

The controller maps `/companies`; the application's `/api` context path makes the public base URL `/api/companies`.

All endpoints require authentication. Ownership comes from the authenticated principal. POST, PUT, and DELETE also require a CSRF token under the existing security configuration.

| Method | URL | Success |
| --- | --- | --- |
| GET | `/api/companies` | 200, paginated company responses |
| POST | `/api/companies` | 201, company response and `Location` header |
| GET | `/api/companies/{companyId}` | 200, company response |
| PUT | `/api/companies/{companyId}` | 200, updated company response |
| DELETE | `/api/companies/{companyId}` | 204, empty body |

## Create and update

```json
{
  "name": "Acme",
  "companyType": "EMPLOYER",
  "websiteUrl": "https://example.com",
  "careersUrl": "https://example.com/careers"
}
```

`name` and `companyType` are required and limited to 255 and 127 characters respectively. Company type values are not yet restricted to an enum; `EMPLOYER` is an example. Optional URLs must use HTTP or HTTPS. Blank optional URLs become null.

POST must omit `version` (or supply null); the server initializes it. PUT requires a nonnegative `version` copied from the most recent company response:

```json
{
  "name": "Acme",
  "companyType": "EMPLOYER",
  "websiteUrl": "https://example.com",
  "careersUrl": null,
  "version": 0
}
```

PUT replaces all editable fields: omitted optional URLs are cleared. The request does not accept ownership or audit fields as editable properties. Responses contain `id`, `name`, `companyType`, `websiteUrl`, `careersUrl`, `createdAt`, `updatedAt`, and `version`. JPA increments the version when the company is changed; always use the version returned by the server for the next edit.

An outdated version or a concurrent write returns 409. Reload the company and reconcile changes before resubmitting; do not blindly retry with the newer version. A missing or negative PUT version returns 400. DELETE keeps its existing contract without a client version, although JPA detects conflicting changes between the server's load and delete.

## Pagination

Example: `GET /api/companies?page=0&size=20&sort=name,asc`

Pages are zero-based. The default size is 20; requests above 100 are capped at 100. Supported sort fields are `id`, `name`, `companyType`, `createdAt`, and `updatedAt`. Use `asc` or `desc`; repeat `sort` for multiple fields. The service adds `id` as a tie-breaker when absent. The default ordering is by name, then ID.

The response contains a `content` array and a `page` object with `size`, `number`, `totalElements`, and `totalPages`. Only the authenticated user's companies contribute to results and totals.

## Errors

Errors use the existing `ApiErrorResponse` format:

- 400: invalid company details, malformed JSON or company UUID, or unsupported sort field.
- 401: authentication is required. A write request without a valid CSRF token may be rejected with 403 first.
- 403: missing or invalid CSRF token.
- 404: company is missing or belongs to another user; both return `Company not found`.
- 409: database conflict, including duplicate company names for the same user after case and surrounding-space normalization.
- 409: stale company version or optimistic-locking conflict.
