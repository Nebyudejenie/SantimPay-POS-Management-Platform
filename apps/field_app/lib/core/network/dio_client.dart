import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:uuid/uuid.dart';

import '../config/env.dart';

/// Configured Dio instance. Attaches the Keycloak access token and an Idempotency-Key on every
/// mutating request (critical for the flaky-network retry path — the server dedupes by this key so a
/// deployment is never completed twice). The Retrofit-generated API client is built on top of this.
Dio buildDio(FlutterSecureStorage storage) {
  final dio = Dio(BaseOptions(
    baseUrl: Env.apiBaseUrl,
    connectTimeout: const Duration(seconds: 15),
    receiveTimeout: const Duration(seconds: 30),
  ));

  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) async {
      final token = await storage.read(key: 'access_token');
      if (token != null) options.headers['Authorization'] = 'Bearer $token';
      if (options.method != 'GET') {
        options.headers.putIfAbsent('Idempotency-Key', () => const Uuid().v4());
      }
      handler.next(options);
    },
  ));
  return dio;
}
