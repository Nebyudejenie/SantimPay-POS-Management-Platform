# 1 & 2 — Executive Summary and Architecture Decisions

## 1. Executive Summary

### 1.1 What we are building
A self-hosted, enterprise-grade **POS Management Platform** for SantimPay that runs the full
operational lifecycle of payment terminals and the merchants who use them: inventory of devices and
SIMs, merchant onboarding and KYC, branch management, daily field deployment of terminals, device
lifecycle (RMA, swap, retirement), call-center follow-ups, task assignment and approval workflows,
transaction analytics, employee management, reporting, and an AI layer for scoring, prediction, and
assisted operations.

### 1.2 Scale targets (year 1 → year 3)
| Dimension | Year 1 | Year 3 | Design ceiling |
|---|---|---|---|
| Merchants | 3,000 | 30,000 | 100k |
| Active POS terminals | 5,000 | 50,000 | 200k |
| Internal users (employees) | 150 | 600 | 2,000 |
| Transactions ingested/day | 200k | 3M | 10M |
| Field deployments/day | 200 | 1,500 | 5,000 |

These numbers matter: at year-1 scale, a single well-built PostgreSQL instance with read replicas and
partitioned hot tables is *comfortable*. This is the empirical basis for recommending a modular
monolith over microservices (see ADR-001).

### 1.3 Architecture in one paragraph
A **modular monolith** backend (**Java 21 + Spring Boot 3.x + Spring Modulith**, single deployable,
hard module boundaries enforced by an architecture-fitness test), written with **Domain-Driven
Design + Clean/Hexagonal Architecture**, using **CQRS only where read/write asymmetry justifies it**,
and an **event-driven outbox** so the modules communicate asynchronously *as if* they were already
separate services. It exposes an **API-first** contract (OpenAPI 3.1) consumed by a
**React + Vite + MUI** operations console and a **Flutter** field app. It runs on **Kubernetes on Proxmox**, deployed via **GitOps (ArgoCD)** from
**Terraform-provisioned** infrastructure configured by **Ansible**, fronted by **Cloudflare Tunnel +
Zero Trust + WAF** with no inbound ports open. Data lives in **PostgreSQL** (primary), **Redis**
(cache/queues), **MinIO** (objects/attachments), and **pgvector** (AI retrieval). Identity is
**Keycloak** (OIDC). Observability is **Prometheus + Grafana + Loki + Tempo**. Secrets are
**Vault**. Images are scanned by **Trivy** and stored in **Harbor**.

### 1.4 Headline recommendations (and challenges to the brief)
1. **Modular monolith, not microservices, for the core platform.** (ADR-001) Extract services on
   evidence, not on aspiration.
2. **Use Keycloak OR Vault-for-identity, not both for the same job.** Keycloak owns *human/app
   identity & SSO*; Vault owns *secrets & dynamic DB credentials*. Don't let them overlap. (ADR-007)
3. **Pick ONE message backbone.** Use the **transactional outbox + Redis Streams** initially; do not
   introduce Kafka/RabbitMQ until throughput or fan-out demands it. (ADR-004)
4. **Do not self-host an LLM on day one.** Start AI as offline batch scoring + a thin RAG; gate any
   real-time LLM behind a clear cost/latency budget. (ADR-010, AI doc)
5. **Two databases logical, one cluster physical.** OLTP and analytics separated by schema + read
   replica + materialized views, not by a second DB product, until volume forces a columnar store.
6. **Proxmox is a single point of failure unless clustered.** Minimum 3 Proxmox nodes with Ceph or
   ZFS replication; otherwise your "enterprise" platform dies with one host. (ADR-009)
7. **Transactions are reference data here, not the system of record.** This platform *ingests*
   transaction summaries for analytics; the payment switch remains the source of truth. Designing it
   as a ledger would be scope creep and a compliance burden. (ADR-006)

### 1.5 Cost & timeline shape
- **MVP (operational core) in ~12 weeks** with a 6–8 person team.
- **Production-hardened v1 in ~6 months.**
- Year-1 infra is **3× Proxmox nodes + 1 backup/DR node**, fully self-hosted, no hyperscaler.

### 1.6 Resolving contradictions in the source prompts (merge rules)
The brief is a concatenation of several prompts that **disagree** with each other. Per the MASTER
instruction ("merge into ONE"), conflicts are resolved by this precedence: **(1) the explicit
NON-NEGOTIABLE / MANDATORY block wins**, then (2) the MASTER PROJECT INSTRUCTION, then (3) later/more
specific statements. Resolved decisions:

| Conflict in source | Options seen | Resolved decision | Basis |
|---|---|---|---|
| Backend framework | Spring Boot *(mandatory)* vs "Golang OR NestJS" vs "NestJS or Spring Boot" | **Java 21 + Spring Boot 3.x (Spring Modulith)** | NON-NEGOTIABLE block |
| Frontend | React+Vite+MUI *(mandatory)* vs "React/Next.js" vs "Flutter Web" | **React 19 + Vite + TypeScript + MUI** | NON-NEGOTIABLE block |
| Architecture | "Microservices" vs "Modular Monolith first, microservice-ready" | **Modular monolith, microservice-ready** | Mandatory block + ADR-001 |
| Scale | "thousands" vs "1,000,000+ merchants / 5,000,000+ POS" | **Design for thousands→tens of thousands now; health-telemetry & txn tables engineered for millions** | MASTER says "thousands"; health/txn volume genuinely needs million-row design |
| Messaging | Redis vs RabbitMQ vs Kafka | **Outbox + Redis Streams now; RabbitMQ/Kafka on trigger** | ADR-004 (cost vs need) |
| Roles | "exactly 4 roles" vs 8–13 roles | **Data-driven RBAC; seed the full role set; 4 are the *primary* operational personas** | superset satisfies both |
| Search | OpenSearch/Elasticsearch | **Postgres FTS + `pg_trgm` now; OpenSearch when search SLAs demand** | avoid premature heavy infra |
| Tracing | Tempo vs Jaeger | **Tempo** (Grafana-native) | single-vendor coherence |
| PG HA | Patroni+PgBouncer vs operator | **CloudNativePG (primary) — Patroni+PgBouncer is the documented equivalent alternative** | k8s-native ops; see ADR-013 |

### ADR-013 — PostgreSQL HA: CloudNativePG (Patroni is the sanctioned alternative)
**Decision.** Run PostgreSQL via **CloudNativePG** (k8s-native operator: streaming replication,
automated failover, PITR/WAL to MinIO, read replicas) with **PgBouncer** for pooling. The brief's
"Patroni + PgBouncer" is functionally equivalent; CloudNativePG is preferred *because* we are on
Kubernetes (less bespoke glue). If the team standardizes on VM-based Postgres, **Patroni + etcd +
PgBouncer + pgBackRest** delivers the same guarantees and is the explicit fallback.

---

## 2. Architecture Decisions (ADRs)

Each ADR: **Context → Decision → Consequences → When to revisit.**

### ADR-001 — Modular Monolith over Microservices (for now)
**Context.** The brief asks for microservices. At year-1 scale (≤5k terminals, ≤150 users, ≤200k
txn/day) the dominant cost of microservices is *operational*: distributed transactions, network
failure modes, per-service CI/CD, service mesh, distributed tracing complexity, and a platform team
you do not yet have. The domain is also highly cohesive — merchants, devices, deployments, and tasks
are tightly related and frequently queried together.

**Decision.** Build a **modular monolith**:
- One deployable backend, internally split into **bounded-context modules** (`identity`,
  `merchant`, `inventory`, `deployment`, `kyc`, `tasks`, `workflow`, `followup`, `analytics`,
  `notifications`, `ai`).
- Modules may **only** call each other through **published application interfaces** (ports) or via
  **domain events**, never by reaching into another module's repositories or tables. Enforced by an
  automated **architecture-fitness test** (ArchUnit-style) in CI.
- Each module owns its **own schema** within the single PostgreSQL database. No cross-schema foreign
  keys; cross-module references are by ID + eventual consistency.

**Consequences.** Single transaction boundary when you need it, one deploy, one trace, drastically
lower ops burden — while keeping seams so clean that any module can be lifted into its own service
later by swapping its in-process event handler for a network one.

**When to revisit / extraction triggers (extract a module into a service when ANY is true):**
- It needs a *different scaling profile* (e.g., `analytics` ingestion spikes independent of the UI).
- It needs an *independent release cadence* owned by a separate team.
- It has a *different compliance/isolation requirement* (e.g., `kyc` PII isolation).
- A single module's load is provably the bottleneck and vertical scaling is exhausted.

**Comparison table.**

| Criterion | Microservices (brief) | Modular Monolith (recommended) | Verdict |
|---|---|---|---|
| Time to MVP | Slow (platform first) | Fast | Monolith |
| Ops burden at <150 users | Very high | Low | Monolith |
| Independent scaling | Native | Per-module via extraction | Tie (deferred) |
| Transactional integrity | Hard (sagas) | Easy (single DB) | Monolith |
| Team size needed | 15+ | 6–8 | Monolith |
| Blast radius | Small per service | Whole app | Microservices |
| Future flexibility | High | High *if seams kept clean* | Tie |

### ADR-002 — Domain-Driven Design as the organizing principle
**Decision.** Strategic DDD defines the module boundaries (bounded contexts, ubiquitous language,
context map in [02](02-diagrams-and-domain.md)). Tactical DDD (aggregates, value objects, domain
events, repositories) is used inside the core domain modules (`merchant`, `inventory`, `deployment`,
`kyc`, `workflow`). CRUD-only modules (`notifications`, `attachments`) deliberately **skip** heavy
tactical DDD — don't gold-plate simple modules.

### ADR-003 — Clean + Hexagonal Architecture inside each module
**Decision.** Each module is layered: `domain` (entities, value objects, domain services, events —
zero framework imports) → `application` (use cases / command & query handlers, ports) →
`infrastructure` (adapters: persistence, messaging, external APIs) → `interface` (HTTP controllers,
event subscribers). Dependencies point **inward only**. Ports/adapters make the DB, broker, and
external systems swappable and testable.

### ADR-004 — Eventing: Transactional Outbox + Redis Streams first
**Context.** The brief implies heavy event-driven architecture. True brokers (Kafka) add real ops
weight.
**Decision.** Use the **transactional outbox pattern**: domain events are written in the *same DB
transaction* as the state change, then a relay publishes them to **Redis Streams** for in-process
and cross-module consumers. Guarantees at-least-once delivery with no distributed transaction.
Introduce Kafka/Redpanda **only** when (a) sustained event throughput >50k/s, or (b) you need
durable replay/log compaction for many independent consumer groups.

### ADR-005 — CQRS, selectively
**Decision.** Apply CQRS **only** to read-heavy, shape-divergent areas: **analytics dashboards**,
**device/merchant search**, and **reporting**. These read from **materialized views / read models**
updated by events, served from read replicas. The rest of the system uses a normal
command-service-repository flow. Do **not** impose CQRS+event-sourcing globally — it is the most
common over-engineering trap in fintech back-offices.

### ADR-006 — Transactions are analytical reference data, not a ledger
**Decision.** The platform ingests **transaction summaries** from the payment switch (batch + stream)
into a partitioned, append-only analytics store. It is **not** the financial system of record and
performs **no money movement**. This bounds scope, audit, and PCI exposure dramatically. Settlement
accounts are stored as reference for reporting, not for posting entries.

### ADR-007 — Identity vs Secrets separation
**Decision.** **Keycloak** = OIDC/OAuth2 for humans and machine clients, SSO, RBAC realm, MFA.
**Vault** = application secrets, dynamic short-lived PostgreSQL credentials, PKI for internal mTLS,
encryption-as-a-service for field-level PII. Never store user passwords in Vault; never store app
secrets in Keycloak. App authorization decisions are enforced in the application layer using
Keycloak-issued claims + a fine-grained permission table (see DB design).

### ADR-008 — Backend language/stack: **Java 21 + Spring Boot 3.x (MANDATED)**
**Context.** The source prompts contain a **NON-NEGOTIABLE** block mandating Java 21, Spring Boot
3.x, Spring Security, Spring Data JPA/Hibernate, Flyway, Maven, JWT, OpenAPI/Swagger, MapStruct,
Lombok, Bean Validation — and explicitly forbid Node.js/NestJS/Django/etc. Later prompts loosely say
"Golang OR NestJS"; per the merge rules (§1.6) the explicit NON-NEGOTIABLE mandate wins.
**Decision.** **Backend = Java 21 + Spring Boot 3.x using *Spring Modulith***. This is in fact an
*excellent* fit for the modular-monolith decision (ADR-001): Spring Modulith gives first-class module
boundaries, verified module structure (the architecture-fitness test), an in-process event bus, and a
documented path to extraction. Stack: Spring Security + Keycloak (resource server, JWT/OIDC), Spring
Data JPA/Hibernate + **Flyway** migrations, **MapStruct** (DTO mapping), **Lombok**, **Bean
Validation**, **springdoc-openapi** (OpenAPI 3.1), Maven multi-module build.
**Consequences.** Strong, conventional, hireable enterprise stack with a 5-year maintenance horizon;
Spring Modulith's `verifies()` test enforces the same boundary rules described in ADR-001. The web
frontend is a separate React app (no language sharing), so DTO/type parity is achieved via
**OpenAPI-generated TypeScript and Dart clients** instead of a shared language.

### ADR-009 — Proxmox HA is mandatory, not optional
**Decision.** Minimum **3 Proxmox VE nodes** in a cluster with **Ceph** (or ZFS + replication) for
shared storage and live migration; a **4th node off-site/separate power** as the DR target. A single
Proxmox host hosting "enterprise" Kubernetes is a guaranteed outage. Quorum requires ≥3 nodes.

### ADR-010 — AI is additive and decoupled
**Decision.** The `ai` module consumes events and reads replicas; it never sits in the critical write
path. Phase 1 AI = scheduled batch scoring (risk/health/sales/failure) materialized into tables the
app reads. Phase 2 = RAG assistant over operational data using **pgvector** (no new vector DB
product) and an **external LLM API behind an abstraction**; self-hosting an LLM is deferred until
data-residency or cost demands it. See [08-ai-architecture.md](08-ai-architecture.md).

### ADR-011 — API-First, contract-driven
**Decision.** OpenAPI 3.1 specs are the source of truth, stored in a shared contracts repo. Server
DTOs and client SDKs (TS for web, Dart for Flutter) are **generated** from the spec. Breaking changes
go through versioned paths (`/api/v1`). Contract tests run in CI on both producer and consumers.

### ADR-012 — One environment promotion path
**Decision.** `dev` → `staging` → `prod`, all on the same Proxmox/K8s substrate but separate
namespaces/clusters, promoted by GitOps. No snowflake environments; everything reproducible from
Terraform + Ansible + ArgoCD manifests.

### Tooling rationalization (challenging redundancy in the brief)
| Concern | Keep | Why / what to drop |
|---|---|---|
| Identity/SSO | Keycloak | Vault is not an SSO; don't duplicate |
| Secrets/PKI | Vault | Keycloak is not a secret store |
| Messaging | Redis Streams (+outbox) | Defer Kafka until proven need |
| Registry | Harbor (+Trivy built-in) | Don't also run a second registry |
| Metrics/Logs/Traces | Prometheus/Loki/Tempo/Grafana | Coherent single-vendor (Grafana) stack — good choice, keep |
| Vector store | pgvector | Don't add Milvus/Weaviate until pgvector is the bottleneck |
| IaC vs config | Terraform (provision) + Ansible (configure) | Clear split: TF makes VMs/infra, Ansible installs OS/k8s; don't overlap |
