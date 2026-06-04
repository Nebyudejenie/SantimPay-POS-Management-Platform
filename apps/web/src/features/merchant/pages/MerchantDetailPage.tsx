import {
  Box, Button, Checkbox, Chip, FormControlLabel, Grid, List, ListItem, ListItemText,
  MenuItem, Paper, Stack, TextField, Typography,
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

interface OwnerRow { id: string; fullName: string; nationalId?: string; phone?: string; email?: string; ownershipPct?: number; primary: boolean; }

/** Beneficial owners — the KYC subjects behind the merchant. */
function Owners({ merchantId }: { merchantId: string }) {
  const qc = useQueryClient();
  const { data } = useQuery({
    queryKey: ["owners", merchantId],
    queryFn: async () => (await http.get<OwnerRow[]>(`/merchants/${merchantId}/owners`)).data,
  });
  const empty = { fullName: "", nationalId: "", phone: "", email: "", ownershipPct: "", primary: false };
  const [form, setForm] = useState(empty);
  const add = useMutation({
    mutationFn: async () => (await http.post(`/merchants/${merchantId}/owners`, {
      ...form, ownershipPct: form.ownershipPct ? Number(form.ownershipPct) : null,
    })).data,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["owners", merchantId] }); setForm(empty); },
  });

  return (
    <Paper sx={{ p: 3, mt: 2 }}>
      <Typography variant="subtitle1" gutterBottom>Owners</Typography>
      <List dense>
        {(data ?? []).map((o) => (
          <ListItem key={o.id} secondaryAction={
            <Stack direction="row" spacing={1}>
              {o.ownershipPct != null && <Chip size="small" label={`${o.ownershipPct}%`} />}
              {o.primary && <Chip size="small" color="primary" label="primary" />}
            </Stack>}>
            <ListItemText primary={o.fullName}
              secondary={[o.nationalId, o.phone, o.email].filter(Boolean).join(" · ")} />
          </ListItem>
        ))}
        {(data ?? []).length === 0 && <ListItem><ListItemText secondary="No owners yet." /></ListItem>}
      </List>
      <Can permission="merchant:create">
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1} mt={1} flexWrap="wrap" useFlexGap alignItems="center">
          <TextField size="small" label="Full name" value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
          <TextField size="small" label="National ID" value={form.nationalId}
            onChange={(e) => setForm({ ...form, nationalId: e.target.value })} />
          <TextField size="small" label="Phone" value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <TextField size="small" label="Email" value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <TextField size="small" label="Ownership %" type="number" sx={{ width: 120 }} value={form.ownershipPct}
            onChange={(e) => setForm({ ...form, ownershipPct: e.target.value })} />
          <FormControlLabel control={<Checkbox checked={form.primary}
            onChange={(e) => setForm({ ...form, primary: e.target.checked })} />} label="Primary" />
          <Button variant="contained" disabled={add.isPending || !form.fullName}
            onClick={() => add.mutate()}>Add owner</Button>
        </Stack>
      </Can>
    </Paper>
  );
}

interface BankRef { id: string; code: string; name: string; }
interface SettlementRow { id: string; bankId: string; bankName: string; accountNo: string; accountName: string; currency: string; primary: boolean; verified: boolean; }

/** Settlement (payout) bank accounts — where merchant funds are settled. */
function SettlementAccounts({ merchantId }: { merchantId: string }) {
  const qc = useQueryClient();
  const { data } = useQuery({
    queryKey: ["settlements", merchantId],
    queryFn: async () => (await http.get<SettlementRow[]>(`/merchants/${merchantId}/settlement-accounts`)).data,
  });
  const { data: banks } = useQuery({
    queryKey: ["banks"],
    queryFn: async () => (await http.get<BankRef[]>("/banks")).data,
  });
  const empty = { bankId: "", accountNo: "", accountName: "", currency: "ETB", primary: false };
  const [form, setForm] = useState(empty);
  const add = useMutation({
    mutationFn: async () => (await http.post(`/merchants/${merchantId}/settlement-accounts`, form)).data,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["settlements", merchantId] }); setForm(empty); },
  });

  return (
    <Paper sx={{ p: 3, mt: 2 }}>
      <Typography variant="subtitle1" gutterBottom>Settlement Accounts</Typography>
      <List dense>
        {(data ?? []).map((s) => (
          <ListItem key={s.id} secondaryAction={
            <Stack direction="row" spacing={1}>
              <Chip size="small" label={s.currency} />
              {s.primary && <Chip size="small" color="primary" label="primary" />}
              <Chip size="small" color={s.verified ? "success" : "default"}
                label={s.verified ? "verified" : "unverified"} />
            </Stack>}>
            <ListItemText primary={`${s.bankName} · ${s.accountNo}`} secondary={s.accountName} />
          </ListItem>
        ))}
        {(data ?? []).length === 0 && <ListItem><ListItemText secondary="No settlement accounts yet." /></ListItem>}
      </List>
      <Can permission="merchant:update">
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1} mt={1} flexWrap="wrap" useFlexGap alignItems="center">
          <TextField select size="small" label="Bank" sx={{ minWidth: 180 }} value={form.bankId}
            onChange={(e) => setForm({ ...form, bankId: e.target.value })}>
            {(banks ?? []).map((b) => <MenuItem key={b.id} value={b.id}>{b.name}</MenuItem>)}
          </TextField>
          <TextField size="small" label="Account #" value={form.accountNo}
            onChange={(e) => setForm({ ...form, accountNo: e.target.value })} />
          <TextField size="small" label="Account name" value={form.accountName}
            onChange={(e) => setForm({ ...form, accountName: e.target.value })} />
          <TextField size="small" label="Currency" sx={{ width: 100 }} value={form.currency}
            onChange={(e) => setForm({ ...form, currency: e.target.value })} />
          <FormControlLabel control={<Checkbox checked={form.primary}
            onChange={(e) => setForm({ ...form, primary: e.target.checked })} />} label="Primary" />
          <Button variant="contained"
            disabled={add.isPending || !form.bankId || !form.accountNo || !form.accountName}
            onClick={() => add.mutate()}>Add account</Button>
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

      <Owners merchantId={m.id} />

      <SettlementAccounts merchantId={m.id} />

      <Branches merchantId={m.id} />

      <Can permission="report:read">
        <AiScores merchantId={m.id} />
      </Can>
    </Box>
  );
}
