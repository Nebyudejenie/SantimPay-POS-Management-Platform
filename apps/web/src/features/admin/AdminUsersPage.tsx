import {
  Alert, Box, Button, Card, CardContent, Chip, MenuItem, Paper, Stack, TextField, Typography,
} from "@mui/material";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { http } from "@/lib/http";

interface RoleInfo { role: string; permissions: number; permissionList: string[]; }
interface CreatedUser { userId: string; username: string; role: string; permissions: number; temporaryPassword: string; }

/**
 * Super Admin → create users with a role. The backend provisions them in Keycloak with a one-time
 * temporary password (shown once) the user must reset on first login. Guarded by user:manage on the
 * server; this page is also nav-gated to that permission.
 */
export default function AdminUsersPage() {
  const { data: roles } = useQuery({
    queryKey: ["admin", "roles"],
    queryFn: async () => (await http.get<RoleInfo[]>("/admin/roles")).data,
  });

  const [form, setForm] = useState({ email: "", firstName: "", lastName: "", role: "OPS_MANAGER" });
  const [created, setCreated] = useState<CreatedUser | null>(null);

  const create = useMutation({
    mutationFn: async () => (await http.post<CreatedUser>("/admin/users", form)).data,
    onSuccess: (u) => setCreated(u),
  });

  const selectedRole = roles?.find((r) => r.role === form.role);

  return (
    <Box maxWidth={760}>
      <Typography variant="h5" mb={2}>Admin · Create User</Typography>

      {created && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setCreated(null)}>
          <strong>User created:</strong> {created.username} ({created.role}, {created.permissions} permissions).
          <br />Temporary password (give it to the user — shown once):{" "}
          <code style={{ fontSize: "1.1em" }}>{created.temporaryPassword}</code>
          <br />They must change it on first login.
        </Alert>
      )}

      <Paper sx={{ p: 3, mb: 2 }}>
        <Stack spacing={2}>
          <TextField label="Email" type="email" value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <Stack direction="row" spacing={2}>
            <TextField label="First name" fullWidth value={form.firstName}
              onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
            <TextField label="Last name" fullWidth value={form.lastName}
              onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
          </Stack>
          <TextField select label="Role" value={form.role}
            onChange={(e) => setForm({ ...form, role: e.target.value })}>
            {(roles ?? []).map((r) => (
              <MenuItem key={r.role} value={r.role}>{r.role} · {r.permissions} permissions</MenuItem>
            ))}
          </TextField>
          {create.isError && <Alert severity="error">Failed to create user. Check the email isn't already used.</Alert>}
          <Button variant="contained" disabled={create.isPending || !form.email}
            onClick={() => create.mutate()}>
            {create.isPending ? "Creating…" : "Create user"}
          </Button>
        </Stack>
      </Paper>

      {selectedRole && (
        <Card variant="outlined">
          <CardContent>
            <Typography variant="subtitle2" gutterBottom>
              {selectedRole.role} grants these permissions:
            </Typography>
            <Stack direction="row" flexWrap="wrap" gap={0.5}>
              {selectedRole.permissionList.map((p) => <Chip key={p} size="small" label={p} />)}
            </Stack>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}
