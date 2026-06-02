import { Box, Button, Chip, Stack, Typography } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { makeResource } from "@/lib/useResource";
import { Can } from "@/lib/rbac";

interface KycRequest {
  id: string;
  merchantId: string;
  requestType: string;
  status: string;
}

const kyc = makeResource<KycRequest>("kyc-requests");

export default function KycPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = kyc.useList({ page, limit: 50 });
  const action = kyc.useAction();

  const columns: GridColDef[] = [
    { field: "merchantId", headerName: "Merchant", flex: 1, minWidth: 220 },
    { field: "requestType", headerName: "Type", width: 140 },
    {
      field: "status", headerName: "Status", width: 150,
      renderCell: (p) => <Chip size="small" label={p.value}
        color={p.value === "APPROVED" ? "success" : p.value === "REJECTED" ? "error" : "warning"} />,
    },
    {
      field: "actions", headerName: "", width: 220, sortable: false,
      renderCell: (p) => (
        <Can permission="kyc:approve">
          <Stack direction="row" spacing={1}>
            <Button size="small" variant="outlined"
              onClick={() => action.mutate({ id: p.row.id, verb: "assign" })}>Assign me</Button>
            <Button size="small" variant="contained" color="success"
              onClick={() => action.mutate({ id: p.row.id, verb: "approve" })}>Approve</Button>
          </Stack>
        </Can>
      ),
    },
  ];

  return (
    <Box>
      <Typography variant="h5" mb={2}>KYC Requests</Typography>
      <DataGrid autoHeight rows={data?.data ?? []} columns={columns} loading={isLoading}
        getRowId={(r) => r.id} paginationMode="server" rowCount={data?.page.totalElements ?? 0}
        paginationModel={{ page, pageSize: 50 }} onPaginationModelChange={(m) => setPage(m.page)}
        pageSizeOptions={[50]} />
    </Box>
  );
}
