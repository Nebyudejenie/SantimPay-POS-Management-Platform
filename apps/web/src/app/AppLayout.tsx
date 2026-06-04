import {
  AppBar, Box, Button, Drawer, List, ListItemButton, ListItemText, Toolbar, Typography,
} from "@mui/material";
import { Link as RouterLink, Outlet } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import { NotificationsBell } from "@/features/notifications/NotificationsBell";
import { usePermissions } from "@/lib/rbac";

const DRAWER_WIDTH = 220;

/** Nav item; visible if the user has ANY of `perms` (server still enforces). */
const navItems: { label: string; path: string; perms: string[] }[] = [
  { label: "Dashboard", path: "/dashboard", perms: ["report:read"] },
  { label: "Merchants", path: "/merchants", perms: ["merchant:read"] },
  { label: "Devices", path: "/devices", perms: ["device:read"] },
  { label: "Deployments", path: "/deployments", perms: ["deployment:read"] },
  { label: "KYC", path: "/kyc", perms: ["kyc:read"] },
  { label: "Approvals", path: "/workflows", perms: ["workflow:read"] },
  { label: "Tasks", path: "/tasks", perms: ["task:read"] },
  // Follow-ups: visible to anyone who can read OR log them (call-center + data-encoder).
  { label: "Follow-ups", path: "/follow-ups", perms: ["followup:read", "followup:create"] },
  { label: "Reports", path: "/reports", perms: ["report:read"] },
  { label: "Admin · Users", path: "/admin/users", perms: ["user:manage"] },
];

export default function AppLayout() {
  const auth = useAuth();
  const perms = usePermissions();
  const visible = navItems.filter((i) => i.perms.length === 0 || i.perms.some((p) => perms.has(p)));

  return (
    <Box sx={{ display: "flex" }}>
      <AppBar position="fixed" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar sx={{ justifyContent: "space-between" }}>
          <Typography variant="h6">SantimPay POS</Typography>
          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
            <NotificationsBell />
            <Button color="inherit" onClick={() => void auth.signoutRedirect()}>Sign out</Button>
          </Box>
        </Toolbar>
      </AppBar>
      <Drawer variant="permanent"
        sx={{ width: DRAWER_WIDTH, flexShrink: 0, ["& .MuiDrawer-paper"]: { width: DRAWER_WIDTH, boxSizing: "border-box" } }}>
        <Toolbar />
        <Box sx={{ overflow: "auto" }}>
          <List>
            {visible.map((item) => (
              <ListItemButton key={item.path} component={RouterLink} to={item.path}>
                <ListItemText primary={item.label} />
              </ListItemButton>
            ))}
          </List>
        </Box>
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, p: 3, mt: 8 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
