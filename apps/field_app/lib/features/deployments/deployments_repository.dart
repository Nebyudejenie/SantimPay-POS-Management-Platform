import 'package:dio/dio.dart';
import 'package:drift/drift.dart' show Value;
import 'package:uuid/uuid.dart';

import '../../data/local/app_database.dart';
import '../sync/sync_engine.dart';

/// Offline-first repository for the field deployment flow.
///
/// Reads come from the local Drift cache (refreshed from the API when online); the
/// "complete deployment" write goes through the SyncEngine outbox so it succeeds instantly offline
/// and is replayed idempotently on reconnect.
class DeploymentsRepository {
  final Dio _dio;
  final AppDatabase _db;
  final SyncEngine _sync;
  static const _uuid = Uuid();

  DeploymentsRepository(this._dio, this._db, this._sync);

  /// Today's route — cached for offline use; tries to refresh from the API, falls back to cache.
  Future<List<CachedDeployment>> todaysRoute() async {
    try {
      final res = await _dio.get<Map<String, dynamic>>(
        '/deployments',
        queryParameters: {'date': _today(), 'limit': 200},
      );
      final rows = (res.data!['data'] as List).map((d) {
        final m = d as Map<String, dynamic>;
        return CachedDeploymentsCompanion(
          id: Value(m['id'] as String),
          deploymentNo: Value(m['deploymentNo'] as String? ?? ''),
          merchantName: Value(m['merchantId'] as String? ?? ''),
          branchName: Value(m['branchId'] as String? ?? ''),
          scheduledDate: Value(m['scheduledDate'] as String? ?? ''),
          status: Value(m['status'] as String? ?? 'PLANNED'),
        );
      }).toList();
      await _db.cacheDeployments(rows);
    } on DioException {
      // offline / server unreachable -> serve whatever we cached last time
    }
    return _db.todaysRoute();
  }

  /// Complete a deployment in the field. Queued locally + flushed when online (idempotent).
  Future<void> completeDeployment({
    required String deploymentId,
    required String deviceSerial,
    required String receivedBy,
    required double latitude,
    required double longitude,
    String? conversationNotes,
    List<String> photoPaths = const [],
  }) async {
    await _sync.enqueue(SyncOp(
      id: _uuid.v4(),
      method: 'POST',
      path: '/deployments/$deploymentId:complete',
      body: {
        'deviceSerial': deviceSerial,
        'receivedBy': receivedBy,
        'latitude': latitude,
        'longitude': longitude,
        'conversationNotes': conversationNotes,
      },
      attachments: photoPaths,
    ));
    // Best-effort immediate flush; if offline it stays queued.
    await _sync.flush();
  }

  String _today() {
    final now = DateTime.now();
    return '${now.year.toString().padLeft(4, '0')}-'
        '${now.month.toString().padLeft(2, '0')}-'
        '${now.day.toString().padLeft(2, '0')}';
  }
}
