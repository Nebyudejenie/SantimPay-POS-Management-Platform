# 10 — RBAC: Roles, Permission Matrix & Data Scoping

> The source prompts disagree: one says **"exactly 4 roles"**, others list **8–13 roles**. Resolution
> (per §1.6): RBAC is **data-driven** (`identity.roles` + `permissions` + `role_permissions`), so the
> exact set is configuration, not code. We define **4 primary operational personas** (satisfying the
> "exactly 4" intent for the MVP) and seed the **full enterprise role set** for later activation.
> Authorization is enforced in the Spring Boot application layer (deny-by-default), not just in
> Keycloak — Keycloak authenticates and provides realm roles; fine-grained `resource:action` checks
> happen against `role_permissions`.

## 10.1 Authorization model
- **Authentication:** Keycloak (OIDC, MFA). The JWT carries the user's realm roles.
- **Coarse authorization:** Spring Security maps roles to route-level access.
- **Fine-grained authorization:** every mutating use-case calls
  `authz.require(actor, "resource:action", scope)`. Permissions are `resource:action` rows; roles
  bundle them; the effective set per role is cached in Redis.
- **Data scoping:** a user may be restricted to a **region/branch set** (`employees.region` +
  optional scope table). Region-scoped roles only see/act on data within their region.
- **Segregation of duties:** the *requester* of a workflow cannot be its *approver*
  (`maker ≠ checker`), enforced in the workflow engine.

## 10.2 Primary operational personas (MVP — the "4 roles")
| Role | Purpose |
|---|---|
| **Super Admin** | Full system + user/role administration |
| **Operations Manager** | Inventory, deployments, approvals, reporting (org-wide) |
| **Call Center Agent** | Follow-ups, KYC change intake, merchant lookup (read-mostly) |
| **Field / Deployment Officer** | Field deployments via Flutter app, region-scoped |

## 10.3 Full enterprise role set (seeded, activate as org grows)
Super Admin, Admin, Manager, Supervisor, Sales, Call Center Agent, Data Encoder, Finance, Field
Officer, Support, Auditor, Compliance, IT Administrator.

## 10.4 CRUD permission matrix (primary personas)
Legend: **C**reate · **R**ead · **U**pdate · **D**elete (soft) · **A**pprove · — none

| Module | Super Admin | Ops Manager | Call Center | Field Officer |
|---|---|---|---|---|
| Users / Roles / Permissions | C R U D | R | — | — |
| Employees | C R U D | R U | R | R (self) |
| Merchants | C R U D A | C R U A | R | R |
| Branches | C R U D | C R U | R | R |
| Merchant Owners | C R U D | C R U | R | R |
| Settlement Accounts | C R U D | C R U A | R | — |
| POS Devices / Inventory | C R U D | C R U | R | R U (assigned) |
| SIM Cards | C R U D | C R U | R | R |
| Banks (reference) | C R U D | R | R | R |
| Deployments | C R U D | C R U A | R | C R U (own/region) |
| Device Assignments | C R U D | C R U | R | C R U (region) |
| KYC Requests | C R U D A | R A | C R | R |
| KYC Change Requests | C R U D A | R A | C R | — |
| Follow-ups | C R U D | C R U | C R U (own) | — |
| Tasks | C R U D A | C R U A | R U (assigned) | R U (assigned) |
| Workflow Approvals | C R U D A | A (in scope) | — | — |
| Device Health | R | R | R | R (assigned) |
| Transactions / Reports | R, export | R, export | R (limited) | — |
| Notifications | R U | R U | R U | R U |
| Audit Logs | R | R (scope) | — | — |
| AI Scores / Assistant | R | R | R (limited) | — |

## 10.5 Extended-role highlights (segregation of duties)
| Role | Key grants | Key denials (SoD) |
|---|---|---|
| **Finance** | settlement accounts R, commission/txn reports R+export, KYC settlement-change **approve** | cannot create merchants or deploy devices |
| **Compliance / Auditor** | **read-everything** incl. audit log; KYC **approve** (compliance) | **no write** to operational data (read + approve only) |
| **Data Encoder** | create/update merchant, branch, deployment intake; encoder-confirm KYC change | cannot approve workflows, cannot manage users |
| **IT Administrator** | system config, integrations, no business-data write | cannot approve merchant/KYC, cannot see PII unless granted |
| **Supervisor / Manager** | region-scoped approvals, team task assignment, dashboards | cannot manage roles/permissions |
| **Sales** | create merchant leads, follow-ups | cannot approve, region-scoped |
| **Support** | follow-ups, device health, tasks | cannot approve KYC/merchant |

## 10.6 Data visibility / regional restriction rules
- Region-scoped roles (Field Officer, Sales, region Supervisor): rows filtered by
  `employees.region` ∩ resource region (branch region / deployment region).
- Org-wide roles (Super Admin, Compliance, Finance, Auditor, Ops Manager): no region filter.
- PII fields (national ID, owner phone, settlement account number) are **masked** in list views and
  require `pii:read` to reveal; reveals are audited. PII stored encrypted via Vault Transit
  (see [07-security-observability-dr.md](07-security-observability-dr.md)).

## 10.7 Approval workflows touched by RBAC
| Workflow | Initiator roles | Approver roles | SoD |
|---|---|---|---|
| Merchant activation | Ops Mgr, Data Encoder, Sales | Ops Manager, Compliance | requester ≠ approver |
| KYC approve / reject | Call Center, Data Encoder | Compliance, Ops Manager | maker/checker |
| KYC change (settlement) | Call Center, Data Encoder | **Finance** + Compliance | dual approval |
| Device write-off / retire | Ops Manager, IT Admin | Operations Manager | requester ≠ approver |
