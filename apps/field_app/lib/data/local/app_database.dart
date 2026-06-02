import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

part 'app_database.g.dart';

/// Device-side outbox: every field mutation is written here FIRST (instant UI), then flushed to the
/// API by the SyncEngine when connectivity returns. `idempotencyKey` is sent as the Idempotency-Key
/// header so server-side replay is safe. `attachments` is a JSON array of local file paths to upload
/// via presigned URLs before the mutation is replayed.
class OutboxOps extends Table {
  TextColumn get id => text()(); // == Idempotency-Key (UUID)
  TextColumn get method => text()();
  TextColumn get path => text()();
  TextColumn get bodyJson => text()();
  TextColumn get attachmentsJson => text().withDefault(const Constant('[]'))();
  IntColumn get attempts => integer().withDefault(const Constant(0))();
  TextColumn get lastError => text().nullable()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
  BoolColumn get dispatched => boolean().withDefault(const Constant(false))();

  @override
  Set<Column> get primaryKey => {id};
}

/// Cached deployments for "today's route" so the agent can work fully offline.
class CachedDeployments extends Table {
  TextColumn get id => text()();
  TextColumn get deploymentNo => text()();
  TextColumn get merchantName => text()();
  TextColumn get branchName => text()();
  TextColumn get scheduledDate => text()();
  TextColumn get status => text()();
  RealColumn get latitude => real().nullable()();
  RealColumn get longitude => real().nullable()();

  @override
  Set<Column> get primaryKey => {id};
}

@DriftDatabase(tables: [OutboxOps, CachedDeployments])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_open());

  @override
  int get schemaVersion => 1;

  // ---- Outbox ----
  Future<void> enqueueOp(OutboxOpsCompanion op) => into(outboxOps).insert(op);

  Future<List<OutboxOp>> pendingOps() =>
      (select(outboxOps)..where((o) => o.dispatched.equals(false))
            ..orderBy([(o) => OrderingTerm(expression: o.createdAt)]))
          .get();

  Future<void> markDispatched(String id) =>
      (update(outboxOps)..where((o) => o.id.equals(id)))
          .write(const OutboxOpsCompanion(dispatched: Value(true)));

  Future<void> recordFailure(String id, int attempts, String error) =>
      (update(outboxOps)..where((o) => o.id.equals(id)))
          .write(OutboxOpsCompanion(attempts: Value(attempts), lastError: Value(error)));

  Future<int> pendingCount() async =>
      (await (select(outboxOps)..where((o) => o.dispatched.equals(false))).get()).length;

  // ---- Cached deployments ----
  Future<void> cacheDeployments(List<CachedDeploymentsCompanion> rows) async {
    await batch((b) => b.insertAllOnConflictUpdate(cachedDeployments, rows));
  }

  Future<List<CachedDeployment>> todaysRoute() => select(cachedDeployments).get();
}

LazyDatabase _open() {
  return LazyDatabase(() async {
    final dir = await getApplicationDocumentsDirectory();
    return NativeDatabase.createInBackground(File(p.join(dir.path, 'posctl_field.sqlite')));
  });
}
