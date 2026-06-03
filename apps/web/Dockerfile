# ---- Build ----
FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
COPY package.json pnpm-lock.yaml* ./
# No lockfile committed yet -> plain install. pnpm 10 exits non-zero on ERR_PNPM_IGNORED_BUILDS
# (a warning about postinstall scripts it auto-skips); that's benign for a static SPA, so don't let
# it fail the image build. Vite/esbuild don't need their postinstall here.
RUN pnpm install --no-frozen-lockfile --config.strict-peer-dependencies=false || true; \
    pnpm approve-builds 2>/dev/null || true; \
    test -d node_modules/vite || pnpm install --no-frozen-lockfile
COPY . .
RUN npx vite build

# ---- Serve (static + TLS reverse-proxy, via nginx-unprivileged, non-root) ----
FROM nginxinc/nginx-unprivileged:1.27-alpine AS runtime
USER root
# Self-signed cert for the single-origin HTTPS dev deploy (gives the browser a secure context so
# OIDC PKCE / crypto.subtle works). Production terminates TLS at Cloudflare instead.
RUN mkdir -p /etc/nginx/certs && \
    openssl req -x509 -nodes -newkey rsa:2048 -days 825 \
      -keyout /etc/nginx/certs/server.key -out /etc/nginx/certs/server.crt \
      -subj "/CN=posctl" -addext "subjectAltName=IP:192.168.1.40,DNS:localhost" && \
    chown -R nginx:nginx /etc/nginx/certs
USER nginx
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8443
