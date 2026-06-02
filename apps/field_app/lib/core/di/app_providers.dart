import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/local/app_database.dart';
import '../../features/deployments/deployments_repository.dart';
import '../../features/sync/sync_engine.dart';
import 'providers.dart';

/// Wiring for the offline store, sync engine and repositories. Kept separate from the low-level
/// `providers.dart` (dio/storage) so the composition root reads top-down.
final appDatabaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase();
  ref.onDispose(db.close);
  return db;
});

final syncEngineProvider = Provider<SyncEngine>((ref) {
  return DioSyncEngine(ref.watch(dioProvider), ref.watch(appDatabaseProvider));
});

final deploymentsRepositoryProvider = Provider<DeploymentsRepository>((ref) {
  return DeploymentsRepository(
    ref.watch(dioProvider),
    ref.watch(appDatabaseProvider),
    ref.watch(syncEngineProvider),
  );
});

/// Number of ops awaiting sync (drives the AppBar badge). Refreshed after each flush/enqueue.
final pendingSyncProvider = FutureProvider<int>((ref) async {
  return ref.watch(syncEngineProvider).pendingCount();
});
