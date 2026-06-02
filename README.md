# posctl — SantimPay POS Management Platform

Production monorepo. Modular-monolith backend (Spring Boot + Spring Modulith), React+Vite+MUI
console, Flutter field app, self-hosted on Proxmox/RKE2 via GitOps.

- **Where to start:** [IMPLEMENTATION.md](IMPLEMENTATION.md) — build playbook, order, Sprint 1.
- **Why (architecture):** [docs/](docs/) — the merged single source of truth.
- **DB:** [db/schema.sql](db/schema.sql) (reference) · executable migrations in
  [apps/api/src/main/resources/db/migration](apps/api/src/main/resources/db/migration).

## Layout
```
apps/api        Spring Boot modular monolith (reference module: merchant)
apps/web        React + Vite + MUI console
apps/field_app  Flutter offline-first field app
packages/       contracts (OpenAPI) + design tokens
db/             reference schema
infra/          Terraform + Ansible (provision + configure)
deploy/         Kustomize base/overlays + ArgoCD app-of-apps
.github/        CI / build-release / iac / security pipelines
docs/           architecture (the "why")
```

## Quick start (local inner loop)
```bash
cp .env.example .env
make up                      # postgres + redis + minio + keycloak
cd apps/api && mvn -N wrapper:wrapper && cd -   # one-time: create maven wrapper
make api                     # backend on :8080  (swagger at /swagger-ui.html)
make web                     # console on :5173
```

## Walking skeleton (Sprint 1) — Definition of Done
Create a merchant in the web UI → row in Postgres → `MerchantOnboarded` in `notification.outbox`
→ relayed to Redis stream → audit row written → trace visible in Tempo. See
[IMPLEMENTATION.md §8](IMPLEMENTATION.md).
