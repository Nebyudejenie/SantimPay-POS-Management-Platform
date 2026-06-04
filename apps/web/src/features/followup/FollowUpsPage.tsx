import {
  Box, Button, Chip, MenuItem, Paper, Stack, TextField, Typography,
} from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { makeResource } from "@/lib/useResource";
import { Can } from "@/lib/rbac";

interface FollowUp {
  id: string;
  merchantId?: string;
  channel: string;
  outcome?: string;
  notes?: string;
  contactedPerson?: string;
  contactedPhone?: string;
  aiGenerated: boolean;
  contactedAt: string;
}

const followups = makeResource<FollowUp>("follow-ups");
const CHANNELS = ["CALL", "SMS", "EMAIL", "VISIT", "WHATSAPP"];
const OUTCOMES = ["REACHED", "NO_ANSWER", "CALLBACK", "RESOLVED", "ESCALATED"];

/** Call-center workspace: log a contact attempt + see the follow-up history. Uses followup:create/read. */
export default function FollowUpsPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = followups.useList({ page, limit: 50 });
  const create = followups.useCreate();
  const [form, setForm] = useState({
    merchantId: "", channel: "CALL", outcome: "REACHED",
    contactedPerson: "", contactedPhone: "", notes: "",
  });

  const columns: GridColDef[] = [
    { field: "channel", headerName: "Channel", width: 110 },
    {
      field: "outcome", headerName: "Outcome", width: 130,
      renderCell: (p) => p.value ? <Chip size="small" label={p.value}
        color={p.value === "RESOLVED" ? "success" : p.value === "ESCALATED" ? "error" : "default"} /> : null,
    },
    { field: "contactedPerson", headerName: "Contact", width: 160 },
    { field: "contactedPhone", headerName: "Phone", width: 140 },
    { field: "notes", headerName: "Notes", flex: 1, minWidth: 180 },
    {
      field: "aiGenerated", headerName: "Source", width: 100,
      renderCell: (p) => <Chip size="small" label={p.value ? "AI" : "Manual"} variant="outlined" />,
    },
    { field: "contactedAt", headerName: "When", width: 170 },
  ];

  return (
    <Box>
      <Typography variant="h5" mb={2}>Call Center · Follow-ups</Typography>

      <Can permission="followup:create">
        <Paper sx={{ p: 2, mb: 2 }}>
          <Typography variant="subtitle2" mb={1.5}>Log a follow-up</Typography>
          <Stack direction={{ xs: "column", md: "row" }} spacing={2} flexWrap="wrap" useFlexGap>
            <TextField size="small" label="Merchant ID" sx={{ minWidth: 260 }} value={form.merchantId}
              onChange={(e) => setForm({ ...form, merchantId: e.target.value })} />
            <TextField size="small" select label="Channel" sx={{ width: 130 }} value={form.channel}
              onChange={(e) => setForm({ ...form, channel: e.target.value })}>
              {CHANNELS.map((c) => <MenuItem key={c} value={c}>{c}</MenuItem>)}
            </TextField>
            <TextField size="small" select label="Outcome" sx={{ width: 150 }} value={form.outcome}
              onChange={(e) => setForm({ ...form, outcome: e.target.value })}>
              {OUTCOMES.map((o) => <MenuItem key={o} value={o}>{o}</MenuItem>)}
            </TextField>
            <TextField size="small" label="Contact person" value={form.contactedPerson}
              onChange={(e) => setForm({ ...form, contactedPerson: e.target.value })} />
            <TextField size="small" label="Phone" value={form.contactedPhone}
              onChange={(e) => setForm({ ...form, contactedPhone: e.target.value })} />
            <TextField size="small" label="Notes" sx={{ flex: 1, minWidth: 200 }} value={form.notes}
              onChange={(e) => setForm({ ...form, notes: e.target.value })} />
            <Button variant="contained" disabled={create.isPending}
              onClick={() => create.mutate({
                merchantId: form.merchantId || undefined,
                channel: form.channel, outcome: form.outcome,
                contactedPerson: form.contactedPerson, contactedPhone: form.contactedPhone,
                notes: form.notes,
              })}>
              Log
            </Button>
          </Stack>
        </Paper>
      </Can>

      <DataGrid autoHeight rows={data?.data ?? []} columns={columns} loading={isLoading}
        getRowId={(r) => r.id} paginationMode="server" rowCount={data?.page.totalElements ?? 0}
        paginationModel={{ page, pageSize: 50 }} onPaginationModelChange={(m) => setPage(m.page)}
        pageSizeOptions={[50]} />
    </Box>
  );
}
