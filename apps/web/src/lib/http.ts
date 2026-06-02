import axios from "axios";
import { User } from "oidc-client-ts";

/** Shared Axios instance. Generated API clients are configured to use this base + auth. */
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api/v1",
});

function currentUser(): User | null {
  const authority = import.meta.env.VITE_OIDC_AUTHORITY;
  const clientId = import.meta.env.VITE_OIDC_CLIENT_ID;
  const raw = localStorage.getItem(`oidc.user:${authority}:${clientId}`);
  return raw ? User.fromStorageString(raw) : null;
}

http.interceptors.request.use((config) => {
  const token = currentUser()?.access_token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (r) => r,
  (error) => {
    if (error.response?.status === 401) {
      // token expired / invalid -> bounce to login
      window.location.href = "/";
    }
    return Promise.reject(error);
  },
);
