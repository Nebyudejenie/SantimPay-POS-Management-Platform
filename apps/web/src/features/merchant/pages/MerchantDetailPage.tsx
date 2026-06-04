import {
  Box, Button, Chip, Grid, List, ListItem, ListItemText, Paper, Stack, TextField, Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useParams } from "react-router-dom";
import { Can } from "@/lib/rbac";
import { http } from "@/lib/http";
import { useActivateMerchant, useMerchant } from "../api/merchantApi";

interface BranchRow { id: string; branchNo: string; name: string; city?: string; region?: string; status: string; }

/** Branch intake — DATA_ENCODER's core surface (list + add branches to a merchant). */
function Branches({ merchantId }: { merchantId: string }) {
  const qc = useQueryClient();
  const { data } = useQuery({
    queryKey: ["branches", merchantId],
    queryFn: async () => (await http.get<BranchRow[]>(`/merchants/${merchantId}/branches`)).data,
  });
  const [form, setForm] = useState({ branchNo: "", name: "", city: "", region: "", contactPhone: "" });
  const add = useMutation({
    mutationFn: async () => (await http.post(`/merchants/${merchantId}/branches`, form)).data,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["branches", merchantId] });
      setForm({ branchNo: "", name: "", city: "", region: "", contactPhone: "" }); },
  });

  return (
    <Paper sx={{ p: 3, mt: 2 }}>
      <Typography variant="subtitle1" gutterBottom>Branches</Typography>
      <List dense>
        {(data ?? []).map((b) => (
          <ListItem key={b.id} secondaryAction={<Chip size="small" label={b.status} />}>
            <ListItemText primary={`${b.branchNo} · ${b.name}`}
              secondary={[b.city, b.region].filter(Boolean).join(", ")} />
          </ListItem>
        ))}
        {(data ?? []).length === 0 && <ListItem><ListItemText secondary="No branches yet." /></ListItem>}
      </List>
      <Can permission="merchant:create">
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1} mt={1} flexWrap="wrap" useFlexGap>
          <TextField size="small" label="Branch #" value={form.branchNo}
            onChange={(e) => setForm({ ...form, branchNo: e.target.value })} />
          <TextField size="small" label="Name" value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <TextField size="small" label="City" value={form.city}
            onChange={(e) => setForm({ ...form, city: e.target.value })} />
          <TextField size="small" label="Region" value={form.region}
            onChange={(e) => setForm({ ...form, region: e.target.value })} />
          <TextField size="small" label="Phone" value={form.contactPhone}
            onChange={(e) => setForm({ ...form, contactPhone: e.target.value })} />
          <Button variant="contained" disabled={add.isPending || !form.branchNo || !form.name}
            onClick={() => add.mutate()}>Add branch</Button>
        </Stack>
      </Can>
    </Paper>
  );
}

interface AiScore { scoreType: string; value: number; band: string; modelVersion: string; computedAt: string; }

function bandColor(band: string): "default" | "success" | "warning" | "error" {
  return band === "low" ? "success" : band === "high" ? "error" : "warning";
}

function AiScores({ merchantId }: { merchantId: string }) {
  const { data } = useQuery({
    queryKey: ["ai", "scores", merchantId],
    queryFn: async () =>
      (await http.get<AiScore[]>("/ai/scores", { params: { subjectType: "merchant", id: merchantId } })).data,
  });
  if (!data?.length) return null;
  return (
    <Paper sx={{ p: 3, mt: 2 }}>
      <Typography variant="subtitle1" gutterBottom>AI Scores</Typography>
      <Stack direction="row" spacing={3} flexWrap="wrap" useFlexGap>
        {data.map((s) => (
          <Box key={s.scoreType}>
            <Typography variant="caption" color="text.secondary">{s.scoreType}</Typography>
            <Stack direction="row" spacing={1} alignItems="center">
              <Typography variant="h6">{(s.value * 100).toFixed(0)}%</Typography>
              <Chip size="small" label={s.band} color={bandColor(s.band)} />
            </Stack>
            <Typography variant="caption" color="text.secondary">{s.modelVersion}</Typography>
          </Box>
        ))}
      </Stack>
    </Paper>
  );
}

function Field({ label, value }: { label: string; value?: string }) {
  return (
    <Grid item xs={12} sm={6} md={4}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body1">{value ?? "—"}</Typography>
    </Grid>
  );
}

export default function MerchantDetailPage() {
  const { id = "" } = useParams();
  const { data: m, isLoading } = useMerchant(id);
  const activate = useActivateMerchant();

  if (isLoading || !m) return <Typography>Loading…</Typography>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Stack direction="row" spacing={2} alignItems="center">
          <Typography variant="h5">{m.legalName}</Typography>
          <Chip label={m.status} color={m.status === "ACTIVE" ? "success" : "default"} />
        </Stack>
        {m.status !== "ACTIVE" && (
          <Can permission="merchant:approve">
            <Button
              variant="contained"
              disabled={activate.isPending}
              onClick={() => activate.mutate(m.id)}
            >
              Activate
            </Button>
          </Can>
        )}
      </Stack>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={2}>
          <Field label="Merchant #" value={m.merchantNo} />
          <Field label="Trade name" value={m.tradeName} />
          <Field label="TIN" value={m.taxId} />
          <Field label="Category" value={m.category} />
          <Field label="Risk tier" value={m.riskTier} />
          <Field label="Onboarded" value={m.onboardedAt} />
          <Field label="Activated" value={m.activatedAt} />
        </Grid>
      </Paper>

      <Branches merchantId={m.id} />

      <Can permission="report:read">
        <AiScores merchantId={m.id} />
      </Can>
    </Box>
  );
}
