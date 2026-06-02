import { Box, Button, Chip, Stack, Typography } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { makeResource } from "@/lib/useResource";
import { Can } from "@/lib/rbac";

interface Workflow {
  id: string;
  workflowType: string;
  subjectType: string;
  subjectId: string;
  status: string;
}

const workflows = makeResource<Workflow>("workflows");

export default function WorkflowsPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = workflows.useList({ status: "PENDING", page, limit: 50 });
  const action = workflows.useAction();

  const columns: GridColDef[] = [
    { field: "workflowType", headerName: "Type", width: 200 },
    { field: "subjectType", headerName: "Subject", width: 140 },
    { field: "subjectId", headerName: "Subject ID", flex: 1, minWidth: 220 },
    {
      field: "status", headerName: "Status", width: 120,
      renderCell: (p) => <Chip size="small" label={p.value} color="warning" />,
    },
    {
      field: "actions", headerName: "", width: 220, sortable: false,
      renderCell: (p) => (
        <Can permission="workflow:approve">
          <Stack direction="row" spacing={1}>
            <Button size="small" color="success" variant="contained"
              onClick={() => action.mutate({ id: p.row.id, verb: "approve" })}>Approve</Button>
            <Button size="small" color="error" variant="outlined"
              onClick={() => action.mutate({ id: p.row.id, verb: "reject" })}>Reject</Button>
          </Stack>
        </Can>
      ),
    },
  ];

  return (
    <Box>
      <Typography variant="h5" mb={2}>Pending Approvals</Typography>
      <DataGrid autoHeight rows={data?.data ?? []} columns={columns} loading={isLoading}
        getRowId={(r) => r.id} paginationMode="server" rowCount={data?.page.totalElements ?? 0}
        paginationModel={{ page, pageSize: 50 }} onPaginationModelChange={(m) => setPage(m.page)}
        pageSizeOptions={[50]} />
    </Box>
  );
}
