# infra — provision, configure, deploy

Strict separation of concerns (docs/06 §10):
**Terraform makes infrastructure → Ansible configures the OS + installs Kubernetes → ArgoCD deploys
everything else from Git.** No tool crosses into another's job.

```
infra/
  terraform/
    modules/
      proxmox-vm/       # clone an Ubuntu 24.04 cloud-init template into N VMs
      rke2-cluster/     # compose VMs for an HA RKE2 cluster + render the Ansible inventory
    environments/dev/   # dev: VMs + Cloudflare tunnel/DNS + (Keycloak/MinIO/Vault/Harbor) wiring
  ansible/
    site.yml            # hardening -> node_prep -> rke2_server/agent -> argocd_bootstrap
    roles/              # the 5 roles above (CIS hardening, k8s prereqs, RKE2 install, ArgoCD)
    inventories/dev/    # hosts.ini (+ hosts.generated.ini emitted by Terraform), group_vars
  keycloak/             # realm export + role→permission composite script
```

## Bring-up order (dev)
```bash
# 1. Provision VMs + edge (Terraform)
cd terraform/environments/dev && terraform init -backend-config=backend.hcl && terraform apply

# 2. Configure OS + install RKE2 + bootstrap ArgoCD (Ansible) — uses the inventory TF rendered
cd ../../../ansible && ansible-playbook site.yml

# 3. Everything else is GitOps: ArgoCD's app-of-apps reconciles the platform + app.
#    deploy/argocd/apps/* sync in waves: data(1) -> platform+observability(2) -> ingress(3) -> app(5)
```

## What ArgoCD assembles (deploy/argocd/apps/)
| Wave | Apps |
|------|------|
| -1 | namespaces (data/platform/observability/app) with PodSecurity + NetworkPolicy labels |
| 1 | CloudNativePG operator + posctl-pg cluster (HA, PITR→MinIO), Redis, MinIO |
| 2 | Vault + External Secrets, Keycloak, kube-prometheus-stack + Loki + Tempo |
| 3 | ingress-nginx (internal) + cloudflared (outbound tunnel — no inbound ports) |
| 5 | posctl app (api/web/worker) — dev overlay |

## Not runnable in this repo's sandbox
Terraform/Ansible/kubectl/Helm are not installed here and there's no Proxmox/cluster to target. These
manifests are authored to apply cleanly on the real substrate; validate with `terraform validate`,
`ansible-lint`, and `kubeconform` on a provisioning host.
