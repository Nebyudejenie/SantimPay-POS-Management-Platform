import { Box, Button, Stack, TextField, Typography } from "@mui/material";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { http } from "@/lib/http";
import { Can } from "@/lib/rbac";

interface MonthlyTxnRow {
  terminalId: string;
  terminalName?: string;
  merchantId: string;
  totalTxnCount: number;
  totalTxnAmount: number;
  santimpayCommission: number;
  totalCommissionBr: number;
  totalCommissionCut: number;
  currency: string;
}

const money = (n: number) => (n ?? 0).toLocaleString(undefined, { minimumFractionDigits: 2 });

/** Finance workspace: monthly transaction + commission report with CSV export. Uses report:read/export. */
export default function ReportsPage() {
  const [month, setMonth] = useState<string>(() => new Date().toISOString().slice(0, 7) + "-01");

  const { data, isLoading } = useQuery({
    queryKey: ["reports", "monthly-txn", month],
    queryFn: async () =>
      (await http.get<MonthlyTxnRow[]>("/analytics/transactions/monthly", { params: { month } })).data,
  });

  const rows = (data ?? []).map((r, i) => ({ id: i, ...r }));

  const columns: GridColDef[] = [
    { field: "terminalId", headerName: "Terminal", width: 130 },
    { field: "terminalName", headerName: "Name", flex: 1, minWidth: 140 },
    { field: "totalTxnCount", headerName: "Txns", width: 90, type: "number" },
    { field: "totalTxnAmount", headerName: "Amount", width: 130, valueFormatter: (v) => money(v as number) },
    { field: "santimpayCommission", headerName: "Commission", width: 130, valueFormatter: (v) => money(v as number) },
    { field: "totalCommissionBr", headerName: "Comm. BR", width: 120, valueFormatter: (v) => money(v as number) },
    { field: "totalCommissionCut", headerName: "Comm. Cut", width: 120, valueFormatter: (v) => money(v as number) },
    { field: "currency", headerName: "Cur", width: 70 },
  ];

  const exportCsv = () => {
    const header = ["terminalId", "terminalName", "merchantId", "totalTxnCount", "totalTxnAmount",
      "santimpayCommission", "totalCommissionBr", "totalCommissionCut", "currency"];
    const lines = [header.join(",")].concat(
      (data ?? []).map((r) => header.map((h) => String((r as never)[h] ?? "")).join(",")));
    const blob = new Blob([lines.join("\n")], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url; a.download = `monthly-transactions-${month}.csv`; a.click();
    URL.revokeObjectURL(url);
  };

  const totalAmt = (data ?? []).reduce((s, r) => s + (r.totalTxnAmount ?? 0), 0);
  const totalComm = (data ?? []).reduce((s, r) => s + (r.santimpayCommission ?? 0), 0);

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">Finance · Monthly Transaction Report</Typography>
        <Can permission="report:export">
          <Button variant="outlined" onClick={exportCsv} disabled={!data?.length}>Export CSV</Button>
        </Can>
      </Stack>

      <Stack direction="row" spacing={2} mb={2} alignItems="center">
        <TextField size="small" type="date" label="Month (any day)" InputLabelProps={{ shrink: true }}
          value={month} onChange={(e) => setMonth(e.target.value)} />
        <Typography variant="body2" color="text.secondary">
          Total volume: <b>{money(totalAmt)}</b> · SantimPay commission: <b>{money(totalComm)}</b>
        </Typography>
      </Stack>

      <DataGrid autoHeight rows={rows} columns={columns} loading={isLoading}
        pageSizeOptions={[25, 50, 100]} initialState={{ pagination: { paginationModel: { pageSize: 25 } } }} />
    </Box>
  );
}
