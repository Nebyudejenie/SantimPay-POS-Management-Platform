terraform {
  required_providers {
    proxmox = { source = "bpg/proxmox", version = "~> 0.66" }
  }
}

# Provisions the VMs for an RKE2 HA cluster (3 servers + N agents) by composing the proxmox-vm
# module. OS config + RKE2 install happen in Ansible (site.yml) — this module only makes the VMs and
# emits the inventory the Ansible run consumes. (TF builds infra; Ansible configures it; ArgoCD
# deploys apps — no overlap.)

variable "env" { type = string }
variable "proxmox_nodes" { type = list(string) }
variable "ubuntu_template_id" { type = number }
variable "ssh_public_key" { type = string }
variable "gateway" { type = string }
variable "server_count" { type = number, default = 3 }
variable "agent_count" { type = number, default = 3 }
variable "server_ip_cidrs" { type = list(string) }
variable "agent_ip_cidrs" { type = list(string) }

module "servers" {
  source         = "../proxmox-vm"
  name_prefix    = "posctl-${var.env}-cp"
  count_vms      = var.server_count
  cores          = 4
  memory_mb      = 8192
  disk_gb        = 80
  nodes          = var.proxmox_nodes
  template_id    = var.ubuntu_template_id
  ssh_public_key = var.ssh_public_key
  ip_cidrs       = var.server_ip_cidrs
  gateway        = var.gateway
}

module "agents" {
  source         = "../proxmox-vm"
  name_prefix    = "posctl-${var.env}-worker"
  count_vms      = var.agent_count
  cores          = 8
  memory_mb      = 16384
  disk_gb        = 160
  nodes          = var.proxmox_nodes
  template_id    = var.ubuntu_template_id
  ssh_public_key = var.ssh_public_key
  ip_cidrs       = var.agent_ip_cidrs
  gateway        = var.gateway
}

# Render an Ansible inventory so `ansible-playbook` can configure exactly what TF built.
resource "local_file" "ansible_inventory" {
  filename = "${path.root}/../../ansible/inventories/${var.env}/hosts.generated.ini"
  content  = <<-EOT
    [k8s_servers]
    %{for ip in module.servers.ip_addresses~}
    ${ip}
    %{endfor~}

    [k8s_workers]
    %{for ip in module.agents.ip_addresses~}
    ${ip}
    %{endfor~}

    [all:vars]
    ansible_user=ubuntu
    ansible_python_interpreter=/usr/bin/python3
  EOT
}

output "server_ips" { value = module.servers.ip_addresses }
output "agent_ips" { value = module.agents.ip_addresses }
