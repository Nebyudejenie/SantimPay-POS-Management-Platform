# @posctl/contracts

**The API contract is the source of truth.** `openapi.yaml` (OpenAPI 3.1) defines every endpoint of
the posctl backend; the TypeScript (web) and Dart (Flutter) clients are **generated** from it, so the
three codebases can never drift in their wire types (ADR-011, API-first).

## What's here
- `openapi.yaml` — hand-curated 3.1 spec covering all 12 modules (identity, merchant, inventory,
  deployment, kyc, workflow, tasks, followup, health, analytics, notification, ai).
- `openapitools.json` / `package.json` — generator config + scripts.

## Generate clients
```bash
make contracts          # from repo root — regenerates both clients
# or directly:
pnpm --filter @posctl/contracts gen:ts     # -> apps/web/src/generated/api   (typescript-axios)
pnpm --filter @posctl/contracts gen:dart   # -> apps/field_app/lib/generated/api (dart-dio)
```
Generated output is git-ignored (`**/src/generated/`, `**/generated/`); regenerate on contract change.

## Lint
```bash
make contracts-lint     # redocly lint openapi.yaml
```

## Keeping it honest (CI contract test)
The backend also emits a runtime spec at `/v3/api-docs` (springdoc). A CI job (`ci.yml` → `contracts`)
lints this file; a fuller contract test should diff the generated `/v3/api-docs` against this curated
spec so a controller change that isn't reflected here fails the build. Until that diff is wired, treat
`openapi.yaml` as authoritative and update it in the same PR as any controller change (CODEOWNERS
requires backend + web + mobile review on this path).

## Migration note (web)
`apps/web` currently hand-types a few models (e.g. `features/merchant/types.ts`). After the first
`make contracts`, switch those imports to `@/generated/api` and delete the hand-written duplicates;
the generated Axios client is pre-wired to the shared `http` instance.
