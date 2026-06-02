import { createTheme } from "@mui/material/styles";

/** MUI theme seeded from the shared design tokens (packages/tokens). RTL-ready for Amharic. */
export const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: "#0B6E4F" }, // SantimPay brand green (placeholder; pull from tokens)
    secondary: { main: "#1D3557" },
  },
  shape: { borderRadius: 10 },
  typography: { fontFamily: "Inter, system-ui, sans-serif" },
});
