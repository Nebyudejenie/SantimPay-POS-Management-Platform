import { Box, Chip, Stack, TextField, Typography } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { makeResource } from "@/lib/useResource";

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

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">Daily Deployments</Typography>
        <TextField size="small" type="date" label="Date" InputLabelProps={{ shrink: true }}
          value={date} onChange={(e) => { setDate(e.target.value); setPage(0); }} />
      </Stack>
      <DataGrid autoHeight rows={data?.data ?? []} columns={columns} loading={isLoading}
        getRowId={(r) => r.id} paginationMode="server" rowCount={data?.page.totalElements ?? 0}
        paginationModel={{ page, pageSize: 50 }} onPaginationModelChange={(m) => setPage(m.page)}
        pageSizeOptions={[50]} />
    </Box>
  );
}
