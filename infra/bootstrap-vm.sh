#!/usr/bin/env bash
# Run FROM THIS MACHINE after the deploy key is authorized on the VM.
# Verifies access, gathers facts, and reports what the VM needs for the posctl dev bring-up.
set -uo pipefail
VM=posctl-vm
echo "== whoami =="; ssh $VM 'echo "$(whoami)@$(hostname) | $(. /etc/os-release; echo $PRETTY_NAME) | kernel $(uname -r)"'
echo "== resources =="; ssh $VM 'echo "CPU: $(nproc) cores | RAM: $(free -h|awk "/Mem:/{print \$2}") | Disk: $(df -h / | awk "NR==2{print \$4\" free of \"\$2}")"'
echo "== sudo =="; ssh $VM 'sudo -n true 2>/dev/null && echo "passwordless sudo: YES" || echo "passwordless sudo: NO (will prompt)"'
echo "== tooling present =="; ssh $VM 'for t in docker kubectl rke2 helm ansible git curl; do printf "%-8s " $t; command -v $t >/dev/null && $t --version 2>/dev/null|head -1 || echo MISSING; done'
echo "== ports listening =="; ssh $VM 'ss -tlnp 2>/dev/null | awk "NR==1||/:(22|80|443|6443|9345)\\s/" || true'
