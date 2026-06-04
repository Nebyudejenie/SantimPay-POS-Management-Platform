import { Box, Button, Chip, Grid, Paper, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { Can } from "@/lib/rbac";
import { http } from "@/lib/http";
import { useActivateMerchant, useMerchant } from "../api/merchantApi";

interface AiScore { scoreType: string; value: number; band: string; modelVersion: string; computedAt: string; }

function bandColor(band: string): "default" | "success" | "warning" | "error" {
  return band === "low" ? "success" : band === "high" ? "error" : "warning";
}

function AiScores({ merchantId }: { merchantId: string }) {
  const { data } = useQuery({
    queryKey: ["ai", "scores", merchantId],
    queryFn: async () =>
      (await http.get<AiScore[]>("/ai/scores", { params: { subjectType: "merchant", id: merchantId } })).data,
  });
  if (!data?.length) return null;
  return (
    <Paper sx={{ p: 3, mt: 2 }}>
      <Typography variant="subtitle1" gutterBottom>AI Scores</Typography>
      <Stack direction="row" spacing={3} flexWrap="wrap" useFlexGap>
        {data.map((s) => (
          <Box key={s.scoreType}>
            <Typography variant="caption" color="text.secondary">{s.scoreType}</Typography>
            <Stack direction="row" spacing={1} alignItems="center">
              <Typography variant="h6">{(s.value * 100).toFixed(0)}%</Typography>
              <Chip size="small" label={s.band} color={bandColor(s.band)} />
            </Stack>
            <Typography variant="caption" color="text.secondary">{s.modelVersion}</Typography>
          </Box>
        ))}
      </Stack>
    </Paper>
  );
}

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

      <Can permission="report:read">
        <AiScores merchantId={m.id} />
      </Can>
    </Box>
  );
}
