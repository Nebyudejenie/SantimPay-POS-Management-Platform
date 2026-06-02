import { Box, Button, Chip, Stack, Typography } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useState } from "react";
import { makeResource } from "@/lib/useResource";
import { Can } from "@/lib/rbac";

interface Task {
  id: string;
  title: string;
  taskType?: string;
  priority: string;
  status: string;
  source: string;
}

const tasks = makeResource<Task>("tasks");

const prioColor: Record<string, "default" | "warning" | "error"> = {
  LOW: "default", MEDIUM: "default", HIGH: "warning", URGENT: "error",
};

export default function TasksPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = tasks.useList({ page, limit: 50 });
  const action = tasks.useAction();

  const columns: GridColDef[] = [
    { field: "title", headerName: "Title", flex: 1, minWidth: 240 },
    { field: "taskType", headerName: "Type", width: 130 },
    {
      field: "priority", headerName: "Priority", width: 110,
      renderCell: (p) => <Chip size="small" label={p.value} color={prioColor[p.value] ?? "default"} />,
    },
    { field: "status", headerName: "Status", width: 130 },
    { field: "source", headerName: "Source", width: 100 },
    {
      field: "actions", headerName: "", width: 130, sortable: false,
      renderCell: (p) => (
        <Can permission="task:update">
          <Button size="small" variant="outlined" disabled={p.row.status === "DONE"}
            onClick={() => action.mutate({ id: p.row.id, verb: "complete" })}>Complete</Button>
        </Can>
      ),
    },
  ];

  return (
    <Box>
      <Typography variant="h5" mb={2}>Tasks</Typography>
      <DataGrid autoHeight rows={data?.data ?? []} columns={columns} loading={isLoading}
        getRowId={(r) => r.id} paginationMode="server" rowCount={data?.page.totalElements ?? 0}
        paginationModel={{ page, pageSize: 50 }} onPaginationModelChange={(m) => setPage(m.page)}
        pageSizeOptions={[50]} />
    </Box>
  );
}
