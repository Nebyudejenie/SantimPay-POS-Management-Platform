# ---- Build ----
FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
COPY package.json pnpm-lock.yaml* ./
# No lockfile committed yet -> plain install (generates one in-image). Commit pnpm-lock.yaml later
# to make builds reproducible + restore --frozen-lockfile.
RUN pnpm install --no-frozen-lockfile
COPY . .
RUN npx vite build

# ---- Serve (static, via nginx-unprivileged, non-root) ----
FROM nginxinc/nginx-unprivileged:1.27-alpine AS runtime
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
