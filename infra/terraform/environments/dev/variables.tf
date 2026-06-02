variable "proxmox_endpoint" { type = string }
variable "proxmox_nodes" { type = list(string) }
variable "ubuntu_template_id" { type = number }
variable "ssh_public_key" { type = string }
variable "cp_ip_cidrs" { type = list(string) }
variable "worker_ip_cidrs" { type = list(string) }
variable "gateway" { type = string }
variable "cloudflare_account_id" { type = string }
variable "cloudflare_zone_id" { type = string }
variable "tunnel_secret" { type = string, sensitive = true }
