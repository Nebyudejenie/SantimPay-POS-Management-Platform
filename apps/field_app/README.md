# posctl_field (Flutter)

Offline-first field agent app — Clean Architecture (core / data / domain / features), Riverpod, Dio,
go_router, Drift (offline), Keycloak OIDC.

## Structure
```
lib/
  core/        config (env), network (dio + interceptors), di (riverpod), router, theme, l10n
  data/        datasources (remote: retrofit; local: drift), models, repositories (impl)
  domain/      entities, repository interfaces, usecases
  features/
    auth/        Keycloak OIDC (flutter_appauth)
    deployments/ today's route, complete deployment (scan + GPS + photos + signature)
    devices/     scan, swap, mark faulty
    sync/        offline outbox + SyncEngine (idempotent replay)
```

## Run
```bash
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # freezed/json/retrofit/drift codegen
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080/api/v1
```

## Key design
- All writes hit a local Drift outbox first; `SyncEngine.flush()` replays with an Idempotency-Key on
  reconnect (server dedupes) — see `lib/features/sync/sync_engine.dart` and docs/05 §8.3.
- Photos/signatures captured offline upload to MinIO via presigned URLs during sync.

## Implemented (this build)
- **Offline store** — `data/local/app_database.dart` (Drift): `OutboxOps` + `CachedDeployments`.
- **Sync engine** — `DioSyncEngine`: enqueue→flush, presigned attachment upload, stable
  Idempotency-Key replay, failure/attempt tracking, pending count.
- **Auth** — `features/auth/auth_controller.dart`: Keycloak OIDC (flutter_appauth, PKCE) → secure
  storage; Dio interceptor attaches the token.
- **Screens** — `login` → `route_today` (offline-cached, pull-to-refresh, pending-sync badge) →
  `complete_deployment` (scan serial/IMEI, GPS + photo, receiver + notes, submit-while-offline).
- **Composition root** — `core/di/app_providers.dart`; router `app/router.dart`; `main.dart` is
  `MaterialApp.router`.

## Codegen required before running (needs the Flutter SDK — not available in this env)
```bash
flutter pub get
dart run build_runner build --delete-conflicting-outputs   # generates Drift .g.dart + freezed
```
