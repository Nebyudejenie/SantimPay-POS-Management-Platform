import 'dart:convert';
import 'dart:io' show File;

import 'package:dio/dio.dart';
import 'package:drift/drift.dart' show Value;

import '../../data/local/app_database.dart';

/// Offline-first sync engine (see docs/05 §8.3).
///
/// Every field write is persisted to a local Drift "outbox" table first (UI is instant), then this
/// engine flushes the queue to the API when connectivity returns. Because each queued mutation
/// carries a stable Idempotency-Key, replays after a dropped connection never double-apply.
///
/// Conflict policy:
///  - reference data: server wins (last sync overwrites local cache);
///  - field-captured evidence (photos/signature/notes): append-only, never conflicts;
///  - assignment conflicts: surfaced to ops as a task rather than silently resolved.
abstract class SyncEngine {
  /// Enqueue a mutation locally (returns immediately).
  Future<void> enqueue(SyncOp op);

  /// Attempt to flush all pending ops; call on app start, on reconnect, and periodically.
  Future<SyncResult> flush();

  /// Count of ops not yet confirmed by the server (drives the UI "pending sync" badge).
  Future<int> pendingCount();
}

class SyncOp {
  final String id; // == Idempotency-Key
  final String method; // POST / PATCH
  final String path; // e.g. /deployments/{id}:complete
  final Map<String, dynamic> body;
  final List<String> attachments; // local file paths -> presigned MinIO upload

  SyncOp({
    required this.id,
    required this.method,
    required this.path,
    required this.body,
    this.attachments = const [],
  });
}

class SyncResult {
  final int succeeded;
  final int failed;
  const SyncResult(this.succeeded, this.failed);
}

/// Drift-backed, Dio-driven implementation. Persists ops locally and replays them with a stable
/// Idempotency-Key. Attachments are uploaded to MinIO via presigned URLs before the mutation is
/// replayed; on success the storage keys are merged into the body so the server links the evidence.
class DioSyncEngine implements SyncEngine {
  final Dio _dio;
  final AppDatabase _db;

  DioSyncEngine(this._dio, this._db);

  @override
  Future<void> enqueue(SyncOp op) async {
    await _db.enqueueOp(OutboxOpsCompanion(
      id: Value(op.id),
      method: Value(op.method),
      path: Value(op.path),
      bodyJson: Value(jsonEncode(op.body)),
      attachmentsJson: Value(jsonEncode(op.attachments)),
    ));
  }

  @override
  Future<int> pendingCount() => _db.pendingCount();

  @override
  Future<SyncResult> flush() async {
    final pending = await _db.pendingOps();
    var ok = 0, failed = 0;

    for (final op in pending) {
      try {
        final body = jsonDecode(op.bodyJson) as Map<String, dynamic>;
        final attachments = (jsonDecode(op.attachmentsJson) as List).cast<String>();

        if (attachments.isNotEmpty) {
          body['attachmentKeys'] = await _uploadAttachments(attachments);
        }

        await _dio.request<dynamic>(
          op.path,
          data: body,
          options: Options(
            method: op.method,
            // Stable key => server dedupes a replayed op (never double-applies).
            headers: {'Idempotency-Key': op.id},
          ),
        );
        await _db.markDispatched(op.id);
        ok++;
      } on DioException catch (e) {
        // 4xx (except 409 conflict) are terminal-ish but we keep them for ops review; 5xx/timeout retry.
        await _db.recordFailure(op.id, op.attempts + 1, e.message ?? e.toString());
        failed++;
      }
    }
    return SyncResult(ok, failed);
  }

  /// Ask the API for presigned PUT URLs, upload each local file, return the resulting storage keys.
  Future<List<String>> _uploadAttachments(List<String> localPaths) async {
    final keys = <String>[];
    for (final path in localPaths) {
      final presign = await _dio.post<Map<String, dynamic>>(
        '/attachments:presign',
        data: {'fileName': path.split('/').last},
      );
      final url = presign.data!['url'] as String;
      final key = presign.data!['key'] as String;
      final bytes = await _readFile(path);
      await Dio().put<void>(url, data: Stream.fromIterable([bytes]),
          options: Options(headers: {'Content-Length': bytes.length}));
      keys.add(key);
    }
    return keys;
  }

  Future<List<int>> _readFile(String path) async {
    // Thin indirection kept separate so it can be faked in tests.
    return await File(path).readAsBytes();
  }
}
