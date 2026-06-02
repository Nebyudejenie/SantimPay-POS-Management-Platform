# 3 & 4 — System Diagrams and Domain Model

All diagrams are **Mermaid** (render in GitHub/VS Code). They follow the **C4 model**
(Context → Container → Component → Deployment) plus Network and Security views.

---

## 3.1 System Context (C4 Level 1)

```mermaid
C4Context
    title System Context — SantimPay POS Management Platform
    Person(ops, "Back-office / Ops staff", "Inventory, onboarding, approvals, reporting")
    Person(field, "Field agent", "Deploys & swaps POS terminals on site")
    Person(cc, "Call-center agent", "KYC follow-ups, merchant outreach")
    Person(mgr, "Manager / Exec", "Analytics, approvals, KPIs")

    System(posctl, "POS Management Platform (posctl)", "Manages devices, merchants, deployments, workflows, analytics, AI")

    System_Ext(switch, "Payment Switch / Processor", "Source of truth for transactions & settlement")
    System_Ext(kyc_ext, "National ID / KYC verification", "Government / 3rd-party identity checks")
    System_Ext(sms, "SMS / Email gateway", "OTP & notifications")
    System_Ext(llm, "LLM API provider", "AI assistant & generation (abstracted)")
    System_Ext(maps, "Maps / Geocoding", "Branch geolocation, routing")

    Rel(ops, posctl, "Uses web console", "HTTPS")
    Rel(field, posctl, "Uses Flutter app", "HTTPS")
    Rel(cc, posctl, "Uses web console", "HTTPS")
    Rel(mgr, posctl, "Views dashboards", "HTTPS")
    Rel(posctl, switch, "Ingests txn & settlement summaries", "Batch + stream")
    Rel(posctl, kyc_ext, "Verifies merchant identity", "API")
    Rel(posctl, sms, "Sends OTP/notifications", "API")
    Rel(posctl, llm, "AI generation / RAG", "API")
    Rel(posctl, maps, "Geocode branches", "API")
```

## 3.2 Container Diagram (C4 Level 2)

```mermaid
flowchart TB
    subgraph edge["Cloudflare Edge (Zero Trust, WAF, Tunnel)"]
        cf[Cloudflare Tunnel + Access]
    end

    subgraph k8s["Kubernetes on Proxmox"]
        web["Web Console<br/>(React + Vite + MUI)"]
        api["Backend API<br/>(Spring Boot + Spring Modulith)"]
        worker["Async Workers<br/>(outbox relay, schedulers, AI batch)"]
        kc["Keycloak (OIDC)"]
        subgraph data["Stateful (operators)"]
            pg[("PostgreSQL<br/>primary + replicas")]
            redis[("Redis<br/>cache + streams")]
            minio[("MinIO<br/>object store")]
            vec[("pgvector<br/>(in PG)")]
        end
        vault[Vault]
        obs["Observability<br/>Prom/Grafana/Loki/Tempo"]
    end

    flutter["Flutter Field App<br/>(Android/iOS)"]

    cf --> web
    cf --> api
    web -->|REST/OpenAPI| api
    flutter -->|REST/OpenAPI| cf
    api --> pg
    api --> redis
    api --> minio
    api -->|OIDC| kc
    api -->|secrets/dyn-creds| vault
    worker --> pg
    worker --> redis
    worker --> minio
    worker --> vec
    api -.metrics/logs/traces.-> obs
    worker -.metrics/logs/traces.-> obs
```

## 3.3 Component Diagram (C4 Level 3) — inside the Backend API

```mermaid
flowchart LR
    subgraph api["Backend API (modular monolith)"]
        direction TB
        gw["HTTP Interface Layer<br/>(controllers, OpenAPI, authZ guards)"]
        bus["In-process Event Bus<br/>(outbox + Redis Streams)"]
        subgraph mods["Bounded-context modules"]
            identity["identity"]
            merchant["merchant"]
            inventory["inventory"]
            deployment["deployment"]
            kyc["kyc"]
            tasks["tasks"]
            workflow["workflow"]
            followup["followup"]
            analytics["analytics (read models)"]
            health["health (device telemetry)"]
            notif["notifications"]
            ai["ai"]
        end
    end
    gw --> mods
    mods <--> bus
    deployment -. "DeviceAssigned event" .-> inventory
    merchant -. "MerchantApproved event" .-> deployment
    kyc -. "KycApproved event" .-> merchant
    workflow -. "ApprovalGranted event" .-> tasks
    analytics -. "subscribes to all" .-> bus
    ai -. "subscribes to all" .-> bus
```

Each module internally follows Clean/Hexagonal layering:

```mermaid
flowchart TB
    subgraph module["A single module (e.g. deployment)"]
        iface["interface/ (controllers, subscribers)"]
        app["application/ (use cases, ports, CQRS handlers)"]
        domain["domain/ (aggregates, VOs, domain events)"]
        infra["infrastructure/ (repos, adapters)"]
    end
    iface --> app --> domain
    infra --> app
    infra -. implements ports .-> app
```

## 3.4 Deployment Diagram (C4 Level 4)

```mermaid
flowchart TB
    subgraph internet["Internet"]
        users["Ops / Field / Call-center / Mgmt"]
    end
    subgraph cfedge["Cloudflare (Edge)"]
        waf["WAF + DDoS"]
        access["Zero Trust Access"]
        dns["DNS"]
    end
    subgraph dc["On-prem Datacenter"]
        subgraph px["Proxmox VE Cluster (3 nodes + Ceph)"]
            subgraph n1["Node 1"]
                cp1["K8s control-plane"]
                w1["Worker VM"]
            end
            subgraph n2["Node 2"]
                cp2["K8s control-plane"]
                w2["Worker VM"]
            end
            subgraph n3["Node 3"]
                cp3["K8s control-plane"]
                w3["Worker VM"]
            end
            ceph[("Ceph distributed storage")]
        end
        subgraph dr["Node 4 (DR / Backup, separate power+network)"]
            pgbackup[("WAL archive + base backups")]
            miniodr[("MinIO replica")]
        end
        cft["cloudflared (Tunnel daemon)"]
    end

    users --> dns --> waf --> access --> cft
    cft --> px
    px --> ceph
    px -. "WAL streaming + restic" .-> dr
```

## 3.5 Network Diagram

```mermaid
flowchart LR
    subgraph public["Public (Cloudflare)"]
        cfwaf["WAF / Access / Tunnel ingress"]
    end
    subgraph dmz["No inbound ports — outbound tunnel only"]
        cfd["cloudflared pod"]
    end
    subgraph clusternet["K8s cluster network (Cilium CNI)"]
        ingress["Ingress (internal)"]
        subgraph nsapp["ns: app"]
            apipods["api / web / worker pods"]
        end
        subgraph nsdata["ns: data"]
            datapods["pg / redis / minio"]
        end
        subgraph nsplat["ns: platform"]
            platpods["keycloak / vault / harbor"]
        end
        subgraph nsobs["ns: observability"]
            obspods["prom / loki / tempo / grafana"]
        end
    end
    cfwaf --> cfd --> ingress --> apipods
    apipods -->|NetworkPolicy allow| datapods
    apipods -->|NetworkPolicy allow| platpods
    apipods -. deny all else .-> datapods
    obspods -. scrape .-> apipods
```

Key network rules:
- **Zero open inbound ports** on the firewall. All ingress is via the outbound-only Cloudflare
  Tunnel. The datacenter never exposes a public IP.
- **Default-deny NetworkPolicies** (Cilium); each namespace whitelists only required flows.
- **mTLS** between services via Vault-issued PKI (or a service mesh later — not on day one).
- Data namespace is reachable **only** from `app` namespace, never from ingress directly.

## 3.6 Security Diagram (trust zones & controls)

```mermaid
flowchart TB
    subgraph z0["Zone 0 — Internet (untrusted)"]
        u["Users / Devices"]
    end
    subgraph z1["Zone 1 — Cloudflare (edge controls)"]
        c1["WAF rules / Rate limit / Bot mgmt"]
        c2["Zero Trust: device posture + IdP SSO"]
        c3["mTLS origin cert to tunnel"]
    end
    subgraph z2["Zone 2 — Application (authn/authz)"]
        a1["Keycloak OIDC: login, MFA, tokens"]
        a2["API authZ guards: RBAC + permissions"]
        a3["Input validation / output encoding"]
    end
    subgraph z3["Zone 3 — Data (encryption & audit)"]
        d1["PG: TLS, row-level audit triggers"]
        d2["Vault: dynamic DB creds, field encryption (PII)"]
        d3["MinIO: SSE encryption, object lock for evidence"]
        d4["Immutable audit_log + append-only history"]
    end
    u --> z1 --> z2 --> z3
    a1 -. issues tokens .-> a2
    d2 -. encrypts .-> d1
    d2 -. encrypts .-> d3
```

Defense-in-depth summary: edge (WAF/ZT) → identity (Keycloak+MFA) → app authZ (RBAC + fine-grained
permissions) → data (TLS, field-level encryption of PII via Vault transit, immutable audit). Full
control catalogue in [07-security-observability-dr.md](07-security-observability-dr.md).

---

## 4. Domain Model

### 4.1 Strategic DDD — Context Map

```mermaid
flowchart TB
    identity["Identity & Access<br/>(users, roles, permissions, employees)"]
    merchant["Merchant<br/>(merchant, owner, branch)"]
    inventory["Inventory<br/>(POS device, SIM, bank, settlement acct)"]
    deployment["Deployment<br/>(daily deployment, device assignment, lifecycle)"]
    kyc["KYC<br/>(kyc request, verification, updates)"]
    workflow["Workflow & Approvals"]
    tasks["Tasks & Assignment"]
    followup["Follow-up / Call-center"]
    analytics["Analytics & Reporting (read)"]
    health["Device Health Monitoring<br/>(telemetry, fleet status)"]
    notif["Notifications"]
    ai["AI / Intelligence"]

    merchant -->|"upstream: merchant approved"| deployment
    inventory -->|"device available"| deployment
    kyc -->|"kyc approved -> merchant active"| merchant
    workflow -->|"approval gates"| merchant
    workflow -->|"approval gates"| deployment
    tasks -->|"work for"| followup
    deployment -->|"events"| analytics
    merchant -->|"events"| analytics
    inventory -->|"events"| analytics
    health -->|"telemetry + failure signals"| ai
    health -->|"fleet status"| analytics
    analytics -->|"features"| ai
    ai -->|"generated follow-ups / scores"| followup
    notif -->|"subscribes all"| analytics
    identity -->|"actor on every context (shared kernel: UserId)"| merchant
```

**Relationship patterns (context map):**
- `Identity` is a **Shared Kernel** for `UserId`/`EmployeeId` (just IDs + claims; no shared tables).
- `Merchant`, `Inventory`, `Workflow` are **upstream** to `Deployment` (Customer–Supplier).
- `Analytics` and `AI` are **downstream conformist** consumers of everyone's events (read-only).
- `KYC` ↔ external verification provider via an **Anti-Corruption Layer**.

### 4.2 Ubiquitous language (excerpt)
| Term | Meaning |
|---|---|
| **Merchant** | A business onboarded to accept payments via SantimPay. |
| **Merchant Owner** | Legal/beneficial owner; subject of KYC. |
| **Branch** | A physical location of a merchant where terminals operate. |
| **POS Device** | A physical terminal with a serial/IMEI, lifecycle state. |
| **SIM** | Connectivity SIM bound to a device for a period. |
| **Deployment** | The act/record of placing a device at a branch on a date. |
| **Device Assignment** | Current binding of a device → branch/merchant. |
| **KYC Request** | A unit of identity verification work with a state machine. |
| **Follow-up** | A call-center contact attempt with outcome. |
| **Task** | Assignable unit of work for an employee. |
| **Approval** | A workflow gate requiring an authorized decision. |

### 4.3 Tactical DDD — core aggregates

```mermaid
classDiagram
    class Merchant {
        +MerchantId id
        +LegalName name
        +MerchantStatus status
        +TIN taxId
        +activate()
        +suspend(reason)
    }
    class Owner {
        +OwnerId id
        +FullName name
        +NationalId nationalId
    }
    class Branch {
        +BranchId id
        +Address address
        +GeoPoint location
        +BranchStatus status
    }
    Merchant "1" --> "1..*" Owner : owners
    Merchant "1" --> "0..*" Branch : branches

    class PosDevice {
        +DeviceId id
        +Serial serial
        +IMEI imei
        +DeviceStatus status
        +DeviceModel model
        +markFaulty()
        +retire()
    }
    class Sim {
        +SimId id
        +MSISDN msisdn
        +SimStatus status
    }
    PosDevice "1" --> "0..1" Sim : activeSim

    class Deployment {
        +DeploymentId id
        +Date scheduledDate
        +DeploymentStatus status
        +complete(evidence)
    }
    class DeviceAssignment {
        +AssignmentId id
        +DeviceId device
        +BranchId branch
        +Period period
    }
    Deployment "1" --> "1..*" DeviceAssignment

    class KycRequest {
        +KycId id
        +KycStatus status
        +submit()
        +approve(reviewer)
        +reject(reason)
    }

    class ApprovalWorkflow {
        +WorkflowId id
        +WorkflowType type
        +decide(step, actor, decision)
    }
```

**Aggregate boundaries (invariants protected together):**
- `Merchant` aggregate = Merchant + Owners + Branches (a merchant cannot be active without ≥1
  approved owner and ≥1 branch). Large merchants → consider Branch as its own aggregate referenced
  by ID to avoid loading hundreds of branches; design uses **Branch as a separate aggregate** linked
  by `merchant_id` for exactly this scaling reason.
- `PosDevice` aggregate = device + its current SIM binding + lifecycle state machine.
- `Deployment` aggregate = the deployment plan + its device assignments for a date.
- `KycRequest`, `Task`, `FollowUp`, `ApprovalWorkflow` are each their own aggregate.

### 4.4 Key domain events (the integration contract between modules)
| Event | Producer | Primary consumers |
|---|---|---|
| `MerchantOnboarded` | merchant | kyc, analytics, notifications |
| `KycApproved` / `KycRejected` | kyc | merchant, workflow, notifications |
| `MerchantActivated` | merchant | deployment, analytics |
| `DeviceReceivedIntoStock` | inventory | analytics |
| `DeviceAssigned` / `DeviceUnassigned` | deployment | inventory, analytics |
| `DeploymentCompleted` | deployment | tasks, analytics, notifications |
| `DeviceMarkedFaulty` | inventory | deployment, tasks, ai |
| `ApprovalRequested` / `ApprovalGranted` / `ApprovalRejected` | workflow | requesting module, notifications |
| `TaskAssigned` / `TaskCompleted` | tasks | followup, analytics, notifications |
| `FollowUpLogged` | followup | analytics, ai |
| `TransactionSummaryIngested` | analytics | ai |
| `DeviceHealthReported` | health | analytics, ai (failure prediction) |
| `DeviceOfflineDetected` | health | tasks, followup, notifications |
| `KycChangeRequested` / `KycChangeApplied` | kyc | merchant, workflow, notifications |
| `RiskScoreComputed` / `HealthScoreComputed` | ai | merchant, followup, notifications |

### 4.5 Device lifecycle state machine (illustrative)

```mermaid
stateDiagram-v2
    [*] --> InStock: received
    InStock --> Allocated: assigned to deployment
    Allocated --> Deployed: deployment completed
    Deployed --> Faulty: fault reported
    Faulty --> InRepair: RMA opened
    InRepair --> InStock: repaired
    InRepair --> Retired: beyond economic repair
    Deployed --> InStock: recovered / swap
    Deployed --> Retired: end of life
    Retired --> [*]
```

### 4.6 KYC request state machine

```mermaid
stateDiagram-v2
    [*] --> Draft
    Draft --> Submitted: submit
    Submitted --> UnderReview: assigned
    UnderReview --> PendingDocs: more info needed
    PendingDocs --> UnderReview: docs provided
    UnderReview --> Approved: approve
    UnderReview --> Rejected: reject
    Approved --> [*]
    Rejected --> [*]
```
