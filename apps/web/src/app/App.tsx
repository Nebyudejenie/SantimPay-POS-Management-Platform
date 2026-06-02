import { Box, Button, CircularProgress, Stack, Typography } from "@mui/material";
import { useAuth } from "react-oidc-context";
import { RouterProvider } from "react-router-dom";
import { router } from "./routes";

/**
 * Auth gate. Until the user is authenticated against Keycloak we show a sign-in screen; the rest of
 * the app (router) is private. Token attachment to API calls happens in lib/http.ts.
 */
export default function App() {
  const auth = useAuth();

  if (auth.isLoading) {
    return (
      <Stack alignItems="center" justifyContent="center" height="100vh">
        <CircularProgress />
      </Stack>
    );
  }

  if (auth.error) {
    return <Typography color="error">Auth error: {auth.error.message}</Typography>;
  }

  if (!auth.isAuthenticated) {
    return (
      <Stack alignItems="center" justifyContent="center" height="100vh" spacing={3}>
        <Typography variant="h4">SantimPay POS Console</Typography>
        <Button variant="contained" size="large" onClick={() => void auth.signinRedirect()}>
          Sign in with Keycloak
        </Button>
      </Stack>
    );
  }

  return (
    <Box>
      <RouterProvider router={router} />
    </Box>
  );
}
