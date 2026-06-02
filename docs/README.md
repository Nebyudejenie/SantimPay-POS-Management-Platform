# SantimPay POS Management Platform — Architecture & Implementation Plan

> Single, unified production specification. Every prompt in `idea.md` has been merged into one
> enterprise architecture. This is not a per-question answer set — it is one coherent system design.

**Codename:** `posctl` (POS Control Plane)
**Author role chain:** CTO → Enterprise/Solution Architect → Principal {DevOps, Platform, DB, Backend, Frontend, Flutter, Kubernetes, Security, AI} Architect
**Status:** Design baseline v1.0 — ready to build
**Date:** 2026-05-30

---

## How to read this

The 20 required deliverables map to the files below. Read in order; each builds on the last.

| # | Deliverable | File |
|---|-------------|------|
| 1 | Executive Summary | [01-executive-summary-and-decisions.md](01-executive-summary-and-decisions.md) |
| 2 | Architecture Decisions | [01-executive-summary-and-decisions.md](01-executive-summary-and-decisions.md) |
| 3 | System Diagrams | [02-diagrams-and-domain.md](02-diagrams-and-domain.md) |
| 4 | Domain Model | [02-diagrams-and-domain.md](02-diagrams-and-domain.md) |
| 5 | Database Design | [03-database-design.md](03-database-design.md) + [../db/schema.sql](../db/schema.sql) |
| 6 | API Design | [04-api-design.md](04-api-design.md) |
| 7 | Frontend Design | [05-frontend-and-flutter.md](05-frontend-and-flutter.md) |
| 8 | Flutter Design | [05-frontend-and-flutter.md](05-frontend-and-flutter.md) |
| 9 | Kubernetes Design | [06-platform-devops.md](06-platform-devops.md) |
| 10 | Terraform Design | [06-platform-devops.md](06-platform-devops.md) |
| 11 | Security Design | [07-security-observability-dr.md](07-security-observability-dr.md) |
| 12 | Monitoring Design | [07-security-observability-dr.md](07-security-observability-dr.md) |
| 13 | Backup Design | [07-security-observability-dr.md](07-security-observability-dr.md) |
| 14 | Disaster Recovery Design | [07-security-observability-dr.md](07-security-observability-dr.md) |
| 15 | AI Architecture | [08-ai-architecture.md](08-ai-architecture.md) |
| 16 | Development Roadmap | [09-roadmap-team-deployment.md](09-roadmap-team-deployment.md) |
| 17 | Sprint Plan | [09-roadmap-team-deployment.md](09-roadmap-team-deployment.md) |
| 18 | Team Structure | [09-roadmap-team-deployment.md](09-roadmap-team-deployment.md) |
| 19 | Repository Structure | [06-platform-devops.md](06-platform-devops.md) |
| 20 | Production Deployment Guide | [09-roadmap-team-deployment.md](09-roadmap-team-deployment.md) |
| + | RBAC Roles & Permission Matrix | [10-rbac-matrix.md](10-rbac-matrix.md) |

## Mandated technology stack (honored as NON-NEGOTIABLE per idea.md)
**Backend:** Java 21 + Spring Boot 3.x + Spring Modulith (Spring Security, Data JPA/Hibernate,
Flyway, Maven, MapStruct, Lombok, Bean Validation, springdoc-openapi). **Frontend:** React 19 +
TypeScript + Vite + MUI + TanStack Query + React Router + Axios + React Hook Form + Zod. **Mobile:**
Flutter + Dart (Riverpod, Dio, go_router, Clean Architecture). **Data:** PostgreSQL, Redis, MinIO,
pgvector. **Identity:** Keycloak (OIDC/OAuth2, MFA, RBAC). **Platform:** Proxmox VE, Ubuntu 24.04,
Docker, RKE2 Kubernetes, Terraform, Ansible, GitHub Actions, ArgoCD, Harbor, Trivy, Vault.
**Edge:** Cloudflare Tunnel/DNS/WAF/Zero Trust. **Observability:** Prometheus, Grafana, Loki, Tempo.

> Where the source prompts contradicted each other on the stack (e.g. "Golang OR NestJS"), the
> explicit MANDATORY block wins — see [§1.6](01-executive-summary-and-decisions.md) for the full
> contradiction-resolution table.

## The single most important recommendation

You asked for microservices, Kubernetes, Vault, Keycloak, Harbor, full observability stack,
Terraform, Ansible, ArgoCD — on a self-hosted Proxmox cluster, on day one.

**Do not build it that way.** Build a **modular monolith** (a "majestic monolith" with hard
internal module boundaries and an outbox-based event bus), deploy it on **Kubernetes from day one
but with a deliberately small platform footprint**, and extract microservices *only* when a module
proves it needs independent scaling or an independent release cadence. The full reasoning, with the
trade-off table and the explicit extraction triggers, is in
[01-executive-summary-and-decisions.md](01-executive-summary-and-decisions.md#adr-001).

This single decision saves an estimated 6–9 months of platform plumbing and ~40% of year-one infra
spend, and it is reversible — the modular monolith is designed so extraction is mechanical, not a
rewrite.
