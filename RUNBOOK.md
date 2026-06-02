# posctl — Bring-up & Operations Runbook

Hand-off doc for the team taking this to a real cluster. Pairs with [IMPLEMENTATION.md](IMPLEMENTATION.md)
(what's built) and [docs/09](docs/09-roadmap-team-deployment.md) (full production deployment guide).

> **This repo has never been compiled or applied in its authoring sandbox** (no JVM/Node/Flutter/
> Terraform/cluster there). Everything below is the intended, authored path — run it on a real
> toolchain. Treat the first run of each step as "verify + fix", not "known-green".

---

## 0. Prerequisites (one-time)
- 3× Proxmox VE nodes + Ceph; an Ubuntu 24.04 cloud-init template.
- Cloudflare account: zone, Zero Trust, a Tunnel token, DNS API token.
- A JDK 21 + Maven + Node 20 + pnpm + Flutter SDK + Docker build host (CI runners, or a workstation).
- Harbor reachable at `harbor.santimpay.local` (or repoint image names).
- Vault unsealed with the `posctl/*` secrets populated (see `deploy/base/api/externalsecret.yaml` keys).

## 1. Build & publish images (CI: `.github/workflows/build-release.yml`)
```bash
# Backend
cd apps/api && mvn -N wrapper:wrapper && ./mvnw verify          # compile + ModularityTests + IT
docker build -t harbor.santimpay.local/posctl/api:$(git rev-parse --short HEAD) apps/api
# Web
cd apps/web && pnpm install && pnpm build
docker build -t harbor.santimpay.local/posctl/web:$(git rev-parse --short HEAD) apps/web
# Push both; Trivy scan + cosign sign happen in CI.
```

## 2. Generate clients & mobile codegen (if changed)
```bash
make contracts                                   # TS (web) + Dart (Flutter) from openapi.yaml
cd apps/field_app && flutter pub get && dart run build_runner build --delete-conflicting-outputs
```

## 3. Provision infrastructure (Terraform)
```bash
cd infra/terraform/environments/dev
terraform init -backend-config=backend.hcl       # state in MinIO (S3-compatible)
terraform apply                                  # VMs + Cloudflare tunnel/DNS; renders Ansible inventory
```

## 4. Configure OS + Kubernetes (Ansible)
```bash
cd infra/ansible
ansible-playbook site.yml                        # hardening -> node_prep -> RKE2 -> ArgoCD bootstrap
```
After this, ArgoCD owns the cluster. `app-of-apps` reconciles everything in `deploy/argocd/apps/` by
sync-wave: namespaces(-1) → data(1) → platform+observability(2) → ingress+tunnel(3) → app(5).

## 5. Seed identity
```bash
# Import the realm (dev compose does this automatically; prod via the TF keycloak module), then:
KC_URL=https://id.posctl.santimpay.com KC_ADMIN=admin KC_PW=*** \
  infra/keycloak/assign-role-permissions.sh
```

## 6. Verify (Definition of Done — the walking skeleton)
1. `kubectl -n app get pods` → api + web Running; `kubectl -n data get clusters` → posctl-pg healthy.
2. Browse `https://dev.posctl.santimpay.com` → Keycloak login → dashboard loads.
3. Onboard a merchant in the UI → confirm a `MerchantOnboarded` row in `notification.outbox`, a KYC
   request opened, an audit row in `audit.audit_log`, and a trace in Tempo/Grafana.
4. Approve KYC → approve the activation workflow (as a *different* user — maker≠checker) → merchant
   goes ACTIVE.

## 7. Environment promotion
| Env | Overlay | ArgoCD sync | Image tag |
|-----|---------|-------------|-----------|
| dev | `deploy/overlays/dev` | automated | `dev-latest` (CI bumps to git sha) |
| staging | `deploy/overlays/staging` | automated | `staging-latest` |
| **prod** | `deploy/overlays/prod` | **manual** (reviewed) | pinned `vX.Y.Z` |

Promote = a reviewed git commit changing the overlay's `newTag`; for prod, a human triggers the
ArgoCD sync (Argo Rollouts then does a canary with metric gates). Rollback = `git revert` (ArgoCD
reconciles) or Argo Rollouts auto-abort.

## 8. Day-2 quick reference
- **Backups:** CloudNativePG ships WAL + daily base backups to MinIO (`posctl-pg-backups`), 35-day
  PITR. KYC evidence bucket is object-locked (WORM). **Test a restore monthly.**
- **DR:** datacenter loss → re-run steps 3–4 on the DR node, restore PG via CNPG PITR, re-point the
  Cloudflare Tunnel. Targets: RPO ≤ 5 min, RTO ≤ 4 h (docs/07 §14). Run a quarterly game-day.
- **Secrets rotation:** Vault dynamic DB creds + External Secrets refresh hourly; rotate the Keycloak
  admin and Cloudflare tunnel token on a schedule.
- **Observability:** Grafana (in `observability` ns) has the golden-signal + business dashboards;
  Alertmanager → on-call. Every request carries `X-Request-Id` correlating logs↔traces↔audit.
