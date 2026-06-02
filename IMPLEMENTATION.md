# posctl — Implementation Baseline (Build Phase)

> This is the **build playbook**, not a theory document. It pins the decisions, the repo layout that
> now exists on disk, naming conventions, the exact implementation order, the first sprint, and the
> precise next files to create. Source of truth for *why* = [docs/](docs/). Source of truth for
> *what to build now* = this file + the scaffolded code.

---

## 1. Project architecture summary
A **modular monolith** (`posctl`) — one deployable Spring Boot application internally split into
bounded-context **Spring Modulith** modules with hard isolation, communicating via an in-process
event bus backed by a **transactional outbox**. Clean/Hexagonal layering inside each module
(`domain → application → infrastructure → web`). React+Vite+MUI console and a Flutter field app
consume an **OpenAPI-first** contract. Runs on RKE2 Kubernetes on Proxmox, GitOps via ArgoCD,
fronted by Cloudflare Tunnel/Zero Trust. Full rationale: [docs/01](docs/01-executive-summary-and-decisions.md).

## 2. Final tech stack (confirmed, locked)
| Layer | Technology |
|---|---|
| Backend | **Java 21, Spring Boot 3.3.x, Spring Modulith 1.2.x**, Spring Security (OAuth2 Resource Server), Spring Data JPA/Hibernate, Flyway, Maven, MapStruct, Lombok, Bean Validation, springdoc-openapi |
| Frontend | **React 19, TypeScript, Vite, MUI**, TanStack Query, React Router, Axios, React Hook Form, Zod, i18next |
| Mobile | **Flutter, Dart**, Riverpod, Dio/Retrofit, go_router, Drift (offline), Clean Architecture |
| Data | **PostgreSQL 16** (+pgvector), **Redis 7** (cache + Streams), **MinIO** (S3 objects) |
| Identity | **Keycloak** (OIDC/OAuth2, MFA, RBAC realm) |
| Secrets | **Vault** (+ External Secrets Operator), dynamic DB creds, Transit for PII |
| Platform | Proxmox VE, Ubuntu 24.04, Docker, **RKE2** K8s, Cilium, Ceph, **Terraform** + **Ansible** |
| GitOps/CI | **GitHub + GitHub Actions (ARC self-hosted runners) → ArgoCD**, Argo Rollouts |
| Registry | **Harbor** + **Trivy** + Cosign |
| Observability | **Prometheus, Grafana, Loki, Tempo**, Alertmanager, OpenTelemetry |
| Edge | **Cloudflare** Tunnel / DNS / WAF / Zero Trust |

## 3. Monorepo vs multi-repo — DECISION: **two repos**
1. **`posctl`** (this repo) — polyglot app monorepo: `apps/api` (Spring Boot), `apps/web` (React),
   `apps/field_app` (Flutter), `packages/contracts` (OpenAPI + event schemas → generated clients),
   `packages/tokens`, `db/` (migrations). One PR changes a contract + both consumers atomically.
2. **`posctl-platform`** — Terraform/Ansible/Kustomize/ArgoCD (different blast radius & approvers).
   In this baseline its content is scaffolded under [`infra/`](infra/) and [`deploy/`](deploy/) so a
   team can split it out with a single `git filter-repo` when ready.

Rationale + extraction triggers: [docs/06 §19.1](docs/06-platform-devops.md).

## 4. Repository structure (now on disk)
```
posctl/
├─ apps/
│  ├─ api/                     # Spring Boot modular monolith (Maven)
│  │  ├─ pom.xml
│  │  ├─ Dockerfile
│  │  └─ src/main/java/com/santimpay/posctl/
│  │     ├─ PosctlApplication.java
│  │     ├─ shared/            # OPEN module: kernel used by all (domain base, outbox, audit, security, web, config)
│  │     ├─ merchant/          # FULLY IMPLEMENTED reference bounded context
│  │     ├─ inventory/ deployment/ kyc/ tasks/ workflow/
│  │     ├─ followup/ analytics/ health/ notification/ identity/ ai/   # stubs w/ package-info
│  │     └─ ...
│  ├─ web/                     # React + Vite + MUI (feature-sliced)
│  └─ field_app/               # Flutter (Clean Architecture, offline-first)
├─ packages/
│  ├─ contracts/               # openapi.yaml (source of truth) + event JSON Schemas
│  └─ tokens/                  # design tokens (shared web+flutter)
├─ db/                         # schema.sql (reference) + Flyway migrations live in api
├─ infra/
│  ├─ terraform/               # modules + environments (dev/staging/prod)
│  └─ ansible/                 # hardening + RKE2 bootstrap
├─ deploy/                     # Kustomize base+overlays + ArgoCD app-of-apps
├─ .github/workflows/          # CI, build-release, iac, security
├─ docs/                       # the merged architecture (source of truth for "why")
├─ pnpm-workspace.yaml turbo.json package.json Makefile
└─ IMPLEMENTATION.md (this file)
```

## 5. Service boundaries (modules) & ownership
| Module | Aggregate(s) | Owns schema | Publishes |
|---|---|---|---|
| `identity` | User, Role, Permission, Employee | `identity` | UserProvisioned |
| `merchant` | Merchant, Owner, Branch, SettlementAccount | `merchant` | MerchantOnboarded, MerchantActivated |
| `inventory` | PosDevice, Sim, Bank | `inventory` | DeviceReceived, DeviceMarkedFaulty |
| `deployment` | Deployment, DeviceAssignment | `deployment` | DeviceAssigned, DeploymentCompleted |
| `kyc` | KycRequest, KycChangeRequest | `kyc` | KycApproved, KycChangeRequested |
| `workflow` | WorkflowInstance, Approval | `workflow` | ApprovalGranted |
| `tasks` | Task | `tasks` | TaskAssigned, TaskCompleted |
| `followup` | FollowUp | `followup` | FollowUpLogged |
| `analytics` | (read models) | `analytics` | — (consumer) |
| `health` | DeviceHealthReport | `health` | DeviceOfflineDetected |
| `notification` | Notification, Outbox | `notification` | — |
| `ai` | Score, Embedding, Conversation | `ai` | RiskScoreComputed |
**Rule:** a module may depend only on `shared` (OPEN) + another module's *published* events/named
interfaces — never its `domain`/`infrastructure`. Enforced by `ModularityTests`.

## 6. Naming conventions (locked)
- **Java packages:** `com.santimpay.posctl.<module>.<layer>`; layers: `domain`, `application`,
  `infrastructure`, `web`, `events`.
- **DB:** `snake_case`; schema-per-module; tables plural (`merchants`); PK `id` (UUIDv7); FK
  `<entity>_id`; timestamps `created_at`/`updated_at`; soft delete `deleted_at`; optimistic `version`.
- **Flyway:** `V<epoch-ordered>__<module>_<change>.sql` (e.g. `V1_0001__merchant_init.sql`).
- **REST:** `/api/v1/<plural-resource>`; actions as `:verb` (`/merchants/{id}:activate`).
- **Events:** PascalCase past tense (`MerchantActivated`); class `com.santimpay.posctl.<module>.events`.
- **React:** features in `src/features/<domain>`; components `PascalCase.tsx`; hooks `useXxx`.
- **Flutter:** `snake_case.dart`; feature folders; classes `PascalCase`.
- **Git branches:** `feat/`, `fix/`, `chore/`, trunk-based, short-lived; Conventional Commits.
- **Images:** `harbor.santimpay.local/posctl/<app>:<gitsha>`.

## 7. Exact implementation order (build sequence)
1. **Platform foundation** (infra repo): Proxmox→RKE2→ArgoCD→Cloudflare Tunnel→Harbor/Vault/Keycloak.
2. **Backend backbone**: bootstrap app, `shared` kernel (security, outbox, audit, web errors), CI,
   Flyway baseline, observability wiring. *(scaffolded here)*
3. **`merchant` module** end-to-end (reference implementation). *(scaffolded here)*
4. **`inventory`** then **`deployment`** (device lifecycle + assignment).
5. **`kyc` + `workflow`** (approval gating, merchant activation).
6. **`tasks` + `followup`**, **Flutter field MVP**.
7. **`analytics` + `health`** read models, dashboards.
8. **`ai`** phase 0–1 (feature store + batch scoring).

## 8. First sprint (Sprint 1 — 2 weeks): "Walking skeleton"
**Goal:** an authenticated request flows edge→API→DB→event→audit, observable, deployed via GitOps.
**Backend**
- [ ] `apps/api` builds (`mvn verify`), boots, `/actuator/health` green.
- [ ] `shared` kernel: `AggregateRoot`, `DomainEvent`, JPA auditing, `OutboxEvent`+relay,
      `SecurityConfig` (Keycloak JWT resource server), `@RequiresPermission`, global problem-details
      handler, idempotency filter, OpenAPI config.
- [ ] Flyway `V1_0000__baseline.sql` (schemas + extensions + shared/outbox/audit) and
      `V1_0001__merchant_init.sql`.
- [ ] `merchant` module: onboard + get + list + activate (workflow-stubbed), emits
      `MerchantOnboarded`/`MerchantActivated` via outbox, audited.
- [ ] `ModularityTests` (Spring Modulith verify) green in CI.
**Frontend**
- [ ] Vite app boots, MUI theme, OIDC login against Keycloak, `/me`, Merchants list + detail + create.
**Platform**
- [ ] Dockerfiles build; Harbor push w/ Trivy gate; ArgoCD syncs `dev` overlay; reachable via tunnel.
- [ ] Prometheus scrapes `/actuator/prometheus`; Loki gets JSON logs; Tempo gets traces.
**Definition of Done:** create a merchant in the web UI → row in PG → `MerchantOnboarded` in outbox →
audit row written → trace visible in Tempo, all in `dev`.

## 9. Build progress & next files

**Done in this baseline:** root tooling; `shared` kernel (outbox + relay, audit, security, web errors,
config); **ALL 12 modules implemented end-to-end** — `merchant`, `inventory`, `deployment`,
`workflow`, `kyc`, `tasks`, `followup`, `health`, `analytics`, `notification`, `identity`, `ai`;
Flyway `V1_0000..V1_0012`; full RBAC seed; `ModularityTests` + 8 aggregate test suites +
walking-skeleton + onboarding-saga integration tests; web merchant feature; Flutter skeleton;
IaC/GitOps/CI.

Module design notes: `health` = high-volume **append-only** telemetry (monthly range-partitioned,
latest-status MV, offline-transition event). `analytics` = **CQRS read side** (dashboard + monthly
commission report); `AnalyticsReadModel` is the single sanctioned cross-schema read path.
`notification` = event→inbox fan-out + **SSE** live stream. `identity` = Keycloak-synced
users/employees + `/me`. `ai` = Phase-0 data foundation (scores, feature store, pgvector embeddings,
RAG conversation tables) — read side live, offline scorers/RAG to come.

**The core operational saga is now wired end-to-end (all via published events / named interfaces):**
```
onboard merchant ──MerchantOnboarded──▶ kyc opens KycRequest
kyc.approve ──(workflow.api)──▶ MERCHANT_ACTIVATION workflow (PENDING)
ops approves (maker≠checker) ──ApprovalGranted──▶ merchant activates
merchant active ──MerchantActivated──▶ deployment eligible
deployment.complete ──DeviceAssigned──▶ inventory device → DEPLOYED
```
Dependency graph stays acyclic: kyc→merchant.events, kyc→workflow.api, merchant→workflow.events,
deployment→merchant.events, inventory→deployment.events, tasks→inventory.events,
{merchant,inventory,deployment,kyc,tasks,followup}→shared.

**Event-driven work generation now live:** `DeviceMarkedFaulty` → `tasks` auto-creates a HIGH-priority
swap task (`tasks→inventory.events` via `@ApplicationModuleListener`, system-context creation through
the `tasks` `@NamedInterface("api")`).

**All 12 backend modules are now scaffolded end-to-end. Remaining work shifts to "make it run":**
1. **Compile/run gate:** generate the Maven wrapper, run `mvn verify` (ModularityTests +
   Testcontainers) and fix anything the compiler/modulith surfaces. *(Not yet executed here.)*
2. `infra/keycloak/realm-export.json` — realm + clients (`posctl-web`, `posctl-field`, `posctl-api`)
   + role→permission claim mapper (the JWT `permissions` claim the API authorizes against).
3. ✅ DONE — `packages/contracts/openapi.yaml` expanded to **all 12 modules** (41 operations across
   13 controllers, 41 schemas, action endpoints, RFC-9457 Problem). `make contracts` wired to generate
   `typescript-axios` (web) + `dart-dio` (Flutter) via `openapitools.json`. Remaining: run codegen on a
   Node machine, then swap `apps/web` hand-types for `@/generated/api`.
4. ✅ DONE — web features for device/deployment/kyc/workflow/tasks + analytics dashboard +
   notifications bell (SSE), generic `makeResource` hook, permission-filtered nav. Keycloak realm
   export + role→permission composite script done (`infra/keycloak/`).
5. ✅ DONE — Flutter offline outbox (Drift `OutboxOps`+`CachedDeployments`), real `DioSyncEngine`
   (enqueue/flush, presigned upload, idempotent replay), Keycloak OIDC auth, and the field screens
   (login → today's route → complete-deployment with scan/GPS/photo). Needs `dart run build_runner`
   for Drift/freezed codegen on a machine with the Flutter SDK.
6. ✅ DONE (Phase-1, all 3 scorers) — explainable, rule-based **merchant health + risk**
   (`MerchantScorer`) AND **POS-device failure-probability** (`DeviceScorer`, over
   `health.device_health_reports` telemetry), each storing per-factor explanations in
   `ai.scores.features` with `model_version`. `FeatureReader` is the isolated cross-schema read path
   (merchant + device features). Worker-gated nightly `MerchantScoringJob` computes all three and
   writes `ai.scores`; read back via `/ai/scores`. Worker-gated nightly **`ScoringJob`** (renamed;
   scores merchants + devices).
6c. ✅ DONE (Phase-2/3 seed) — **`LlmPort`** `@NamedInterface` (provider behind one abstraction;
   ships a no-op adapter so the platform runs at zero AI cost until a real provider is wired).
   `AiScoreService.record()` publishes **`MerchantScoreComputed`**; the followup module's
   **`AutoFollowUpGenerator`** reacts to low-health/high-risk by drafting an `aiGenerated` follow-up
   (human-reviewed, never auto-sent) — LLM talking points when enabled, deterministic template
   otherwise. One-way dep (`followup → ai.events`/`ai.llm`; no `ai → followup` cycle).
6d. ✅ DONE (Phase-2 RAG) — **`EmbeddingPort`** + **`EmbeddingStore`** (pgvector HNSW cosine
   retrieval over `ai.embeddings`), **`RagService`** (embed → retrieve top-k → confidence gate →
   ground LLM → cited answer), and **`POST /ai/assistant/ask`** (permission-guarded, returns
   citations + `grounded` flag). **Grounded-only guardrail:** empty/low-similarity retrieval → polite
   refusal, never a hallucination; no-op embedding/LLM adapters make it degrade safely at zero cost.
   Contract + `AssistantAnswer` schema added. Remaining: real embedding/LLM adapters + an indexing
   pipeline to populate `ai.embeddings` (chunk policies/SOPs/notes).
6b. ✅ DONE — **worker split** (scheduled beans gated by `posctl.worker.enabled`; dedicated
   `posctl-worker` Deployment, same image, single-runner, own NetworkPolicy — API pods no longer
   duplicate-poll the outbox). **Argo Rollouts canary** for prod api (10→25→50→100% with a Prometheus
   success-rate analysis gate; base Deployment scaled to 0, HPA retargeted to the Rollout).
   **Contract-parity CI test** (`ContractParityTest`) asserts every curated `operationId` is served by
   the runtime `/v3/api-docs`. staging/prod overlays + ArgoCD apps with prod manual-sync.
7. ✅ DONE (authored) — full GitOps platform: 5 Ansible roles (CIS hardening, node prep, RKE2
   server/agent, ArgoCD bootstrap), `rke2-cluster` TF module that renders the Ansible inventory, and
   ArgoCD app-of-apps with **sync-waved** platform components — CloudNativePG (HA+PITR), Redis, MinIO,
   Vault+External-Secrets, Keycloak, kube-prometheus-stack/Loki/Tempo, internal ingress-nginx +
   cloudflared (no inbound ports), then the posctl app. Remaining: `terraform apply` + `ansible-playbook`
   on a real Proxmox host (not possible in this sandbox); DR game-day.

## 9c. Web console — feature-complete (22 TS/TSX files)
Vite + React + MUI SPA, Keycloak OIDC auth gate, permission-filtered left nav. Features wired to the
real API: **Dashboard** (live KPIs, 30s refresh), **Merchants** (list/detail/onboard), **Devices**,
**Deployments**, **KYC** (assign/approve), **Approvals** (workflow approve/reject; maker≠checker
enforced server-side), **Tasks** (complete), plus a **notifications bell** consuming the backend
**SSE** stream. A generic `makeResource<T>()` hook (list/detail/create/`:action`) keeps each feature
page ~80 lines. `<Can permission>` gates affordances; the API still enforces. Keycloak realm
(`infra/keycloak/realm-export.json` + `assign-role-permissions.sh`) wires the `permissions` JWT claim
the API authorizes against.

## 9b. Static validation run (2026-05-31)

A real `mvn verify` could **not** run in this environment: no `java`/`mvn` installed, no passwordless
sudo to install them, no writable Maven repo. Honest status: **the backend has NOT been compiled.**
What WAS run is a set of static gates that catch the most common break classes:

| Gate | Result |
|---|---|
| `package` declaration matches directory path — all files | ✅ |
| brace `{}` / paren `()` balance — all 174 files | ✅ |
| every file declares its matching top-level type (public or package-private) | ✅ |
| **module boundaries** — no cross-module import except `*.events`, named-interface `*Creation/*Initiation`, or `shared` (replicates `ModularityTests`) | ✅ 0 violations *(after fix below)* |
| **permission parity** — every `PERM_*` in code is seeded by a migration | ✅ 25 referenced ⊆ 27 seeded, 0 missing *(after fix below)* |
| **`@Table` ↔ `CREATE TABLE`** parity (the `ddl-auto: validate` gate) | ✅ 15 entities, 0 missing |

**Real bug the modularity gate caught (now fixed):** `kyc` imported `workflow.domain.WorkflowType`
— an illegal cross-module `domain` import that would fail `ModularityTests`. Root cause: the
`WorkflowInitiation` `@NamedInterface` exposed the domain enum, dragging every caller into workflow's
internals. Fix: `WorkflowInitiation.start(...)` now takes the workflow type as a `String` (validated
against the enum inside `WorkflowService`); `kyc` passes `"MERCHANT_ACTIVATION"` and imports nothing
from `workflow.domain`.

**Second bug the permission gate caught (now fixed):** `DeviceService.markFaulty` is guarded by
`PERM_device:update`, but `device:update` was never seeded — so *no role* could mark a device faulty
(every call would 403). Added `device:update` to `V1_0003__inventory.sql` + granted to
SUPER_ADMIN/OPS_MANAGER/FIELD_OFFICER.

**This does NOT replace `mvn verify`.** On a machine with JDK 21 + Maven + network (+ Docker for
Testcontainers): `cd apps/api && mvn verify`. Plausible first-compile touch-ups (unverified): MapStruct
mappers' nested `audit.*` source paths, and the spring-data-redis `opsForStream().add(...)` signature.

## 10. Architecture decisions made during build (addenda)
- **ADR-014 — JPA-annotated aggregates.** Domain aggregates carry `jakarta.persistence` annotations
  (a spec, not the Spring framework) to avoid 2× persistence-mapping boilerplate across 12 modules.
  Domain stays free of *Spring* imports; repositories remain ports; MapStruct maps domain↔web DTOs
  only. This overrides the stricter "zero framework in domain" wording in docs/01 ADR-003 as the
  stronger pragmatic call for a Spring shop. *(Teams wanting pure POJO domains can split persistence
  entities into `infrastructure` later — the ports make it non-breaking.)*
- **ADR-015 — Outbox first, Redis Streams relay.** `OutboxEvent` written in the same JPA transaction;
  a relay publishes to Redis Streams + republishes as Spring Modulith app events. At-least-once;
  consumers idempotent by `event_id`.
- **ADR-016 — One Spring profile per env** (`dev`/`staging`/`prod`) + 12-factor config from env/Vault;
  no secrets in `application.yml`.
