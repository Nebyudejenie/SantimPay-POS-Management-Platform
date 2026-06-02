import { Box, Button, Chip, Grid, Paper, Stack, Typography } from "@mui/material";
import { useParams } from "react-router-dom";
import { Can } from "@/lib/rbac";
import { useActivateMerchant, useMerchant } from "../api/merchantApi";

function Field({ label, value }: { label: string; value?: string }) {
  return (
    <Grid item xs={12} sm={6} md={4}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body1">{value ?? "—"}</Typography>
    </Grid>
  );
}

export default function MerchantDetailPage() {
  const { id = "" } = useParams();
  const { data: m, isLoading } = useMerchant(id);
  const activate = useActivateMerchant();

  if (isLoading || !m) return <Typography>Loading…</Typography>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Stack direction="row" spacing={2} alignItems="center">
          <Typography variant="h5">{m.legalName}</Typography>
          <Chip label={m.status} color={m.status === "ACTIVE" ? "success" : "default"} />
        </Stack>
        {m.status !== "ACTIVE" && (
          <Can permission="merchant:approve">
            <Button
              variant="contained"
              disabled={activate.isPending}
              onClick={() => activate.mutate(m.id)}
            >
              Activate
            </Button>
          </Can>
        )}
      </Stack>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={2}>
          <Field label="Merchant #" value={m.merchantNo} />
          <Field label="Trade name" value={m.tradeName} />
          <Field label="TIN" value={m.taxId} />
          <Field label="Category" value={m.category} />
          <Field label="Risk tier" value={m.riskTier} />
          <Field label="Onboarded" value={m.onboardedAt} />
          <Field label="Activated" value={m.activatedAt} />
        </Grid>
      </Paper>
    </Box>
  );
}
