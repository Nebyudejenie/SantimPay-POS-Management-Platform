import { zodResolver } from "@hookform/resolvers/zod";
import { Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { useOnboardMerchant } from "../api/merchantApi";
import { onboardMerchantSchema, type OnboardMerchantInput } from "../types";

export default function OnboardMerchantPage() {
  const navigate = useNavigate();
  const onboard = useOnboardMerchant();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<OnboardMerchantInput>({ resolver: zodResolver(onboardMerchantSchema) });

  const onSubmit = handleSubmit(async (values) => {
    const created = await onboard.mutateAsync(values);
    navigate(`/merchants/${created.id}`);
  });

  return (
    <Box maxWidth={640}>
      <Typography variant="h5" mb={2}>
        Onboard merchant
      </Typography>
      <Paper sx={{ p: 3 }}>
        <form onSubmit={onSubmit}>
          <Stack spacing={2}>
            <TextField label="Merchant #" {...register("merchantNo")}
              error={!!errors.merchantNo} helperText={errors.merchantNo?.message} />
            <TextField label="Legal name" {...register("legalName")}
              error={!!errors.legalName} helperText={errors.legalName?.message} />
            <TextField label="Trade name" {...register("tradeName")} />
            <TextField label="TIN" {...register("taxId")} />
            <TextField label="Category" {...register("category")} />
            <Button type="submit" variant="contained" disabled={onboard.isPending}>
              Create
            </Button>
          </Stack>
        </form>
      </Paper>
    </Box>
  );
}
