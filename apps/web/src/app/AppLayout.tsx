import {
  AppBar, Box, Button, Drawer, List, ListItemButton, ListItemText, Toolbar, Typography,
} from "@mui/material";
import { Link as RouterLink, Outlet } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import { NotificationsBell } from "@/features/notifications/NotificationsBell";
import { usePermissions } from "@/lib/rbac";

const DRAWER_WIDTH = 220;

/** Nav item with the permission required to see it (server still enforces). */
const navItems: { label: string; path: string; perm?: string }[] = [
  { label: "Dashboard", path: "/dashboard", perm: "report:read" },
  { label: "Merchants", path: "/merchants", perm: "merchant:read" },
  { label: "Devices", path: "/devices", perm: "device:read" },
  { label: "Deployments", path: "/deployments", perm: "deployment:read" },
  { label: "KYC", path: "/kyc", perm: "kyc:read" },
  { label: "Approvals", path: "/workflows", perm: "workflow:read" },
  { label: "Tasks", path: "/tasks", perm: "task:read" },
  { label: "Follow-ups", path: "/follow-ups", perm: "followup:read" },
  { label: "Reports", path: "/reports", perm: "report:read" },
  { label: "Admin · Users", path: "/admin/users", perm: "user:manage" },
];

export default function AppLayout() {
  const auth = useAuth();
  const perms = usePermissions();
  const visible = navItems.filter((i) => !i.perm || perms.has(i.perm));

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
