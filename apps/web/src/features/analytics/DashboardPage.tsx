import { Box, Card, CardContent, Grid, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { http } from "@/lib/http";

interface Dashboard {
  totalMerchants: number;
  activeMerchants: number;
  devicesInStock: number;
  devicesDeployed: number;
  devicesFaulty: number;
  deploymentsToday: number;
  openTasks: number;
  pendingKyc: number;
}

function Kpi({ label, value, color }: { label: string; value: number; color?: string }) {
  return (
    <Grid item xs={12} sm={6} md={3}>
      <Card>
        <CardContent>
          <Typography variant="caption" color="text.secondary">{label}</Typography>
          <Typography variant="h4" sx={{ color }}>{value ?? "—"}</Typography>
        </CardContent>
      </Card>
    </Grid>
  );
}

export default function DashboardPage() {
  const { data } = useQuery({
    queryKey: ["dashboard"],
    queryFn: async () => (await http.get<Dashboard>("/analytics/dashboard")).data,
    refetchInterval: 30_000,
  });
  const d = data ?? ({} as Dashboard);

  return (
    <Box>
      <Typography variant="h5" mb={3}>Executive Dashboard</Typography>
      <Grid container spacing={2}>
        <Kpi label="Total merchants" value={d.totalMerchants} />
        <Kpi label="Active merchants" value={d.activeMerchants} color="#2E7D32" />
        <Kpi label="Pending KYC" value={d.pendingKyc} color="#ED6C02" />
        <Kpi label="Deployments today" value={d.deploymentsToday} />
        <Kpi label="Devices deployed" value={d.devicesDeployed} color="#2E7D32" />
        <Kpi label="Devices in stock" value={d.devicesInStock} />
        <Kpi label="Devices faulty" value={d.devicesFaulty} color="#D32F2F" />
        <Kpi label="Open tasks" value={d.openTasks} />
      </Grid>
    </Box>
  );
}
