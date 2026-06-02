import { Box, Button, Stack, TextField, Typography } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Can } from "@/lib/rbac";
import { useMerchants } from "../api/merchantApi";

const columns: GridColDef[] = [
  { field: "merchantNo", headerName: "Merchant #", width: 140 },
  { field: "legalName", headerName: "Legal name", flex: 1, minWidth: 200 },
  { field: "tradeName", headerName: "Trade name", flex: 1, minWidth: 160 },
  { field: "status", headerName: "Status", width: 130 },
  { field: "riskTier", headerName: "Risk", width: 100 },
];

export default function MerchantsListPage() {
  const [q, setQ] = useState("");
  const [page, setPage] = useState(0);
  const { data, isLoading } = useMerchants(q, page);
  const navigate = useNavigate();

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">Merchants</Typography>
        <Can permission="merchant:create">
          <Button variant="contained" onClick={() => navigate("/merchants/new")}>
            Onboard merchant
          </Button>
        </Can>
      </Stack>

      <TextField
        size="small"
        label="Search"
        value={q}
        onChange={(e) => {
          setQ(e.target.value);
          setPage(0);
        }}
        sx={{ mb: 2, width: 320 }}
      />

      <DataGrid
        autoHeight
        rows={data?.data ?? []}
        columns={columns}
        loading={isLoading}
        getRowId={(r) => r.id}
        onRowClick={(p) => navigate(`/merchants/${p.id}`)}
        paginationMode="server"
        rowCount={data?.page.totalElements ?? 0}
        paginationModel={{ page, pageSize: data?.page.limit ?? 50 }}
        onPaginationModelChange={(m) => setPage(m.page)}
        pageSizeOptions={[50]}
      />
    </Box>
  );
}
