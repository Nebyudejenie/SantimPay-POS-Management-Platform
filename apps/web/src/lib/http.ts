import axios from "axios";
import { User } from "oidc-client-ts";
import { runtimeConfig } from "./auth";

/** Shared Axios instance. Generated API clients are configured to use this base + auth. */
export const http = axios.create({
  baseURL: runtimeConfig.apiBaseUrl,
});

function currentUser(): User | null {
  const raw = localStorage.getItem(
    `oidc.user:${runtimeConfig.oidcAuthority}:${runtimeConfig.oidcClientId}`,
  );
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
