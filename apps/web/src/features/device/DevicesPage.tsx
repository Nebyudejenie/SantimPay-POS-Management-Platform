import { Box, Button, Chip, Stack, TextField, Typography } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { makeResource } from "@/lib/useResource";
import { Can } from "@/lib/rbac";

interface Device {
  id: string;
  serialNo: string;
  terminalId?: string;
  model: string;
  vendor?: string;
  status: string;
}

const devices = makeResource<Device>("devices");

const statusColor: Record<string, "default" | "success" | "warning" | "error"> = {
  IN_STOCK: "default", DEPLOYED: "success", FAULTY: "warning", RETIRED: "error",
};

const columns: GridColDef[] = [
  { field: "serialNo", headerName: "Serial #", width: 160 },
  { field: "terminalId", headerName: "Terminal ID", width: 140 },
  { field: "model", headerName: "Model", flex: 1, minWidth: 140 },
  { field: "vendor", headerName: "Vendor", width: 120 },
  {
    field: "status", headerName: "Status", width: 140,
    renderCell: (p) => <Chip size="small" label={p.value} color={statusColor[p.value] ?? "default"} />,
  },
];

export default function DevicesPage() {
  const [q, setQ] = useState("");
  const [page, setPage] = useState(0);
  const { data, isLoading } = devices.useList({ q: q || undefined, page, limit: 50 });

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">POS Devices</Typography>
        <Can permission="device:create">
          <Button variant="contained" disabled>Import devices</Button>
        </Can>
      </Stack>
      <TextField size="small" label="Search serial / terminal" value={q}
        onChange={(e) => { setQ(e.target.value); setPage(0); }} sx={{ mb: 2, width: 320 }} />
      <DataGrid
        autoHeight rows={data?.data ?? []} columns={columns} loading={isLoading}
        getRowId={(r) => r.id}
        paginationMode="server" rowCount={data?.page.totalElements ?? 0}
        paginationModel={{ page, pageSize: 50 }}
        onPaginationModelChange={(m) => setPage(m.page)} pageSizeOptions={[50]}
      />
    </Box>
  );
}
