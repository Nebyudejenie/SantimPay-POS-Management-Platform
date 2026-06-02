terraform {
  required_version = ">= 1.7"
  backend "s3" {
    # MinIO S3-compatible backend (self-hosted state). Configure via backend.hcl (not committed).
    bucket                      = "posctl-tfstate"
    key                         = "dev/terraform.tfstate"
    region                      = "us-east-1"
    skip_credentials_validation = true
    skip_metadata_api_check     = true
    skip_region_validation      = true
    use_path_style              = true
  }
  required_providers {
    proxmox    = { source = "bpg/proxmox", version = "~> 0.66" }
    cloudflare = { source = "cloudflare/cloudflare", version = "~> 4.40" }
  }
}

provider "proxmox" {
  endpoint = var.proxmox_endpoint
  # token from env: PROXMOX_VE_API_TOKEN (sourced from Vault, never in tfvars)
  insecure = false
}

provider "cloudflare" {
  # api token from env CLOUDFLARE_API_TOKEN (Vault)
}

# K8s control-plane + worker VMs (3 nodes for RKE2 HA — see ADR-009).
module "k8s_servers" {
  source         = "../../modules/proxmox-vm"
  name_prefix    = "posctl-dev-cp"
  count_vms      = 3
  cores          = 4
  memory_mb      = 8192
  nodes          = var.proxmox_nodes
  template_id    = var.ubuntu_template_id
  ssh_public_key = var.ssh_public_key
  ip_cidrs       = var.cp_ip_cidrs
  gateway        = var.gateway
}

module "k8s_workers" {
  source         = "../../modules/proxmox-vm"
  name_prefix    = "posctl-dev-worker"
  count_vms      = 3
  cores          = 8
  memory_mb      = 16384
  disk_gb        = 160
  nodes          = var.proxmox_nodes
  template_id    = var.ubuntu_template_id
  ssh_public_key = var.ssh_public_key
  ip_cidrs       = var.worker_ip_cidrs
  gateway        = var.gateway
}

# Cloudflare Tunnel — public access with ZERO inbound ports (see docs/07).
resource "cloudflare_zero_trust_tunnel_cloudflared" "posctl" {
  account_id = var.cloudflare_account_id
  name       = "posctl-dev"
  secret     = var.tunnel_secret
}

resource "cloudflare_record" "app" {
  zone_id = var.cloudflare_zone_id
  name    = "dev.posctl"
  content = "${cloudflare_zero_trust_tunnel_cloudflared.posctl.id}.cfargotunnel.com"
  type    = "CNAME"
  proxied = true
}
