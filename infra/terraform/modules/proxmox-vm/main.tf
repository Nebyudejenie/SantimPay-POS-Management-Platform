terraform {
  required_providers {
    proxmox = {
      source  = "bpg/proxmox"
      version = "~> 0.66"
    }
  }
}

# Clones a cloud-init Ubuntu 24.04 template into N VMs across the Proxmox cluster nodes.
variable "name_prefix" { type = string }
variable "count_vms" { type = number }
variable "cores" { type = number, default = 4 }
variable "memory_mb" { type = number, default = 8192 }
variable "disk_gb" { type = number, default = 80 }
variable "nodes" { type = list(string) } # proxmox node names to spread across
variable "template_id" { type = number }
variable "ssh_public_key" { type = string }
variable "ip_cidrs" { type = list(string) } # static ips, one per vm
variable "gateway" { type = string }

resource "proxmox_virtual_environment_vm" "vm" {
  count     = var.count_vms
  name      = "${var.name_prefix}-${count.index + 1}"
  node_name = element(var.nodes, count.index % length(var.nodes))

  clone {
    vm_id = var.template_id
    full  = true
  }

  cpu { cores = var.cores }
  memory { dedicated = var.memory_mb }

  disk {
    datastore_id = "ceph"
    interface    = "scsi0"
    size         = var.disk_gb
  }

  network_device { bridge = "vmbr0" }

  initialization {
    ip_config {
      ipv4 {
        address = element(var.ip_cidrs, count.index)
        gateway = var.gateway
      }
    }
    user_account {
      username = "ubuntu"
      keys     = [var.ssh_public_key]
    }
  }

  agent { enabled = true }
}

output "ip_addresses" {
  value = [for c in proxmox_virtual_environment_vm.vm : c.initialization[0].ip_config[0].ipv4[0].address]
}
