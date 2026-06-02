import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../network/dio_client.dart';

/// Composition root (Riverpod). Feature repositories depend on these.
final secureStorageProvider =
    Provider<FlutterSecureStorage>((ref) => const FlutterSecureStorage());

final dioProvider = Provider<Dio>((ref) {
  return buildDio(ref.watch(secureStorageProvider));
});

/// Connectivity-driven flag the SyncEngine listens to (see features/sync).
final isOnlineProvider = StateProvider<bool>((ref) => true);
