import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Dev convenience: forward API calls to the local Spring Boot backend.
      "/api": { target: "http://localhost:8080", changeOrigin: true },
    },
  },
  build: { outDir: "dist", sourcemap: true },
});
