# 6 — API Design

## 6.1 Principles
- **API-first / contract-first.** OpenAPI 3.1 is the source of truth in a `contracts` repo. Server
  validation, web TS SDK, and Flutter Dart SDK are **generated** from it. Contract tests gate CI.
- **REST + JSON** for the operational API (resource-oriented, cache-friendly, easy for Flutter and
  React). **Server-Sent Events / WebSocket** only for live dashboards & notifications. GraphQL is
  **deliberately rejected** for v1 — it adds N+1 and authZ complexity the back-office doesn't need.
- **Versioned** under `/api/v1`. Breaking changes → `/api/v2`. Additive changes are non-breaking.
- **Consistent envelope, pagination, filtering, idempotency, and error format** across all endpoints.

## 6.2 Conventions
- **Auth:** `Authorization: Bearer <JWT>` (Keycloak OIDC access token). Scopes/roles in claims;
  fine-grained checks against `role_permissions` in the app layer.
- **Idempotency:** mutating POSTs accept `Idempotency-Key` header; stored 24h to dedupe retries
  (critical for the Flutter app on flaky field networks).
- **Pagination:** cursor-based — `?limit=50&cursor=<opaque>`; response includes `next_cursor`.
- **Filtering/sorting:** `?status=active&sort=-created_at&q=fuzzy`.
- **Correlation:** every request gets `X-Request-Id` (propagated to logs/traces/audit).
- **Rate limiting:** at Cloudflare + per-token bucket in Redis.
- **Concurrency:** `If-Match: <version>` header → optimistic lock against row `version`.

### Standard error shape (RFC 9457 Problem Details)
```json
{
  "type": "https://errors.posctl/validation",
  "title": "Validation failed",
  "status": 422,
  "detail": "branch_no must be unique within merchant",
  "instance": "/api/v1/merchants/018f.../branches",
  "request_id": "req_01H...",
  "errors": [{ "field": "branch_no", "code": "duplicate" }]
}
```

### Standard list response
```json
{
  "data": [ /* items */ ],
  "page": { "limit": 50, "next_cursor": "eyJpZCI6Li4ufQ==", "total_estimate": 1280 }
}
```

## 6.3 Resource map (selected endpoints)

```
# Identity & access
POST   /api/v1/auth/token                 # exchange (delegated to Keycloak)
GET    /api/v1/me                          # current user + permissions
GET    /api/v1/users        POST /users    PATCH /users/{id}
GET    /api/v1/roles        POST /roles
GET    /api/v1/permissions
GET    /api/v1/employees    POST /employees PATCH /employees/{id}

# Merchant domain
GET    /api/v1/merchants                   # filter/search/paginate
POST   /api/v1/merchants                   # onboard (starts workflow)
GET    /api/v1/merchants/{id}
PATCH  /api/v1/merchants/{id}
POST   /api/v1/merchants/{id}:activate     # action (workflow-gated)
POST   /api/v1/merchants/{id}:suspend
GET    /api/v1/merchants/{id}/owners       POST .../owners
GET    /api/v1/merchants/{id}/branches     POST .../branches
GET    /api/v1/merchants/{id}/settlement-accounts

# KYC
GET    /api/v1/kyc-requests                POST /kyc-requests
POST   /api/v1/kyc-requests/{id}:submit
POST   /api/v1/kyc-requests/{id}:approve   # permission kyc:approve
POST   /api/v1/kyc-requests/{id}:reject
POST   /api/v1/kyc-requests/{id}/documents # multipart -> MinIO
# KYC change requests (settlement account / trade name / both)
GET    /api/v1/kyc-change-requests          POST /kyc-change-requests
POST   /api/v1/kyc-change-requests/{id}:approve   # applies change via workflow
POST   /api/v1/kyc-change-requests/{id}:reject

# Inventory
GET    /api/v1/devices                     POST /devices         # bulk import supported
GET    /api/v1/devices/{id}
POST   /api/v1/devices/{id}:mark-faulty
POST   /api/v1/devices/{id}:retire
GET    /api/v1/sims                         POST /sims
GET    /api/v1/banks

# Deployment
GET    /api/v1/deployments?date=2026-05-30  POST /deployments
POST   /api/v1/deployments/{id}:start
POST   /api/v1/deployments/{id}:complete   # body: assignments + evidence photos
GET    /api/v1/device-assignments?device_id=...&current=true

# Tasks / follow-ups / workflow
GET    /api/v1/tasks?assignee=me&status=open   POST /tasks
POST   /api/v1/tasks/{id}:assign
POST   /api/v1/tasks/{id}:complete
GET    /api/v1/follow-ups                    POST /follow-ups
GET    /api/v1/workflows/{id}                POST /workflows/{id}:decide

# Device health monitoring (telemetry ingest is high-volume)
POST   /api/v1/health/reports               # device/agent posts telemetry (batchable, idempotent)
POST   /api/v1/health/reports:bulk          # bulk ingest from collector
GET    /api/v1/health/devices/{id}/latest   # latest health snapshot
GET    /api/v1/health/fleet?status=offline  # fleet board (from mv_device_latest_health)

# Analytics / reporting (read models, served from replica)
GET    /api/v1/analytics/dashboard
GET    /api/v1/analytics/merchant-health?tier=high
GET    /api/v1/analytics/transactions/monthly?month=2026-05  # commission rollup report
GET    /api/v1/reports/{report}:export?format=csv|xlsx|pdf
POST   /api/v1/imports/{dataset}            # bulk import (devices, merchants, monthly txn) -> staged job
GET    /api/v1/stream/notifications          # SSE

# AI
POST   /api/v1/ai/assistant/conversations
POST   /api/v1/ai/assistant/conversations/{id}/messages   # RAG answer + citations
GET    /api/v1/ai/scores?subject_type=merchant&id=...
```

## 6.4 Action endpoints vs pure REST
Domain actions that aren't simple CRUD use the `:verb` sub-resource style (`:activate`, `:complete`,
`:approve`). This keeps state-machine transitions explicit and auditable rather than overloading
`PATCH` with magic status strings.

## 6.5 Files & uploads
- Direct-to-MinIO via **pre-signed URLs**: client asks `POST /attachments:presign`, uploads to MinIO,
  then confirms `POST /attachments` with the storage key + checksum. Keeps large blobs off the API
  pods. KYC evidence buckets use **object-lock (WORM)** for compliance retention.

## 6.6 Sample OpenAPI fragment
```yaml
openapi: 3.1.0
info: { title: posctl API, version: 1.0.0 }
paths:
  /api/v1/merchants:
    post:
      operationId: onboardMerchant
      security: [{ bearerAuth: [] }]
      parameters:
        - { in: header, name: Idempotency-Key, schema: { type: string } }
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/MerchantCreate' }
      responses:
        '201': { description: created, content: { application/json: { schema: { $ref: '#/components/schemas/Merchant' } } } }
        '422': { $ref: '#/components/responses/Problem' }
components:
  securitySchemes:
    bearerAuth: { type: http, scheme: bearer, bearerFormat: JWT }
  schemas:
    MerchantCreate:
      type: object
      required: [legal_name, category]
      properties:
        legal_name: { type: string, maxLength: 200 }
        trade_name: { type: string }
        tax_id:     { type: string }
        category:   { type: string }
```

## 6.7 Internal contracts (events)
Cross-module events are also versioned schemas (JSON Schema), stored alongside OpenAPI in the
`contracts` repo, so producers and consumers evolve safely. Event names and payloads are listed in
[02-diagrams-and-domain.md §4.4](02-diagrams-and-domain.md).
