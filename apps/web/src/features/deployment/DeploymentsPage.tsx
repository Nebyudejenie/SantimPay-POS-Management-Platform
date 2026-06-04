import { Box, Button, Chip, Paper, Stack, TextField, Typography } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { makeResource } from "@/lib/useResource";
import { Can } from "@/lib/rbac";

interface Deployment {
  id: string;
  deploymentNo: string;
  scheduledDate: string;
  status: string;
  receivedBy?: string;
}

const deployments = makeResource<Deployment>("deployments");

const columns: GridColDef[] = [
  { field: "deploymentNo", headerName: "Deployment #", width: 160 },
  { field: "scheduledDate", headerName: "Date", width: 130 },
  { field: "receivedBy", headerName: "Received by", flex: 1, minWidth: 160 },
  {
    field: "status", headerName: "Status", width: 150,
    renderCell: (p) => <Chip size="small" label={p.value}
      color={p.value === "COMPLETED" ? "success" : p.value === "FAILED" ? "error" : "default"} />,
  },
];

export default function DeploymentsPage() {
  const [date, setDate] = useState("");
  const [page, setPage] = useState(0);
  const { data, isLoading } = deployments.useList({ date: date || undefined, page, limit: 50 });
  const create = deployments.useCreate();

  const [form, setForm] = useState({
    deploymentNo: "", scheduledDate: new Date().toISOString().slice(0, 10),
    merchantId: "", branchId: "",
  });

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">Daily Deployments</Typography>
        <TextField size="small" type="date" label="Filter date" InputLabelProps={{ shrink: true }}
          value={date} onChange={(e) => { setDate(e.target.value); setPage(0); }} />
      </Stack>

      <Can permission="deployment:create">
        <Paper sx={{ p: 2, mb: 2 }}>
          <Typography variant="subtitle2" mb={1.5}>Plan a deployment</Typography>
          <Stack direction={{ xs: "column", md: "row" }} spacing={2} flexWrap="wrap" useFlexGap>
            <TextField size="small" label="Deployment #" value={form.deploymentNo}
              onChange={(e) => setForm({ ...form, deploymentNo: e.target.value })} />
            <TextField size="small" type="date" label="Scheduled date" InputLabelProps={{ shrink: true }}
              value={form.scheduledDate} onChange={(e) => setForm({ ...form, scheduledDate: e.target.value })} />
            <TextField size="small" label="Merchant ID" sx={{ minWidth: 260 }} value={form.merchantId}
              onChange={(e) => setForm({ ...form, merchantId: e.target.value })} />
            <TextField size="small" label="Branch ID" sx={{ minWidth: 260 }} value={form.branchId}
              onChange={(e) => setForm({ ...form, branchId: e.target.value })} />
            <Button variant="contained" disabled={create.isPending || !form.merchantId || !form.branchId}
              onClick={() => create.mutate({
                deploymentNo: form.deploymentNo || undefined,
                scheduledDate: form.scheduledDate,
                merchantId: form.merchantId, branchId: form.branchId,
              })}>
              {create.isPending ? "Planning…" : "Plan"}
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
