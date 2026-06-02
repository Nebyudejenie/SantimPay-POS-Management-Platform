import 'package:flutter_appauth/flutter_appauth.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../core/config/env.dart';
import '../../core/di/providers.dart';

/// Auth state for the field app. Tokens come from Keycloak via Authorization Code + PKCE
/// (flutter_appauth); the access token is read by the Dio interceptor on every request and the
/// refresh token is used to silently renew. Tokens live in encrypted secure storage.
class AuthState {
  final bool authenticated;
  final bool loading;
  final String? error;
  const AuthState({this.authenticated = false, this.loading = false, this.error});
}

class AuthController extends StateNotifier<AuthState> {
  final FlutterAppAuth _appAuth;
  final FlutterSecureStorage _storage;

  AuthController(this._appAuth, this._storage) : super(const AuthState(loading: true)) {
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    final token = await _storage.read(key: 'access_token');
    state = AuthState(authenticated: token != null);
  }

  Future<void> signIn() async {
    state = const AuthState(loading: true);
    try {
      final result = await _appAuth.authorizeAndExchangeCode(
        AuthorizationTokenRequest(
          Env.oidcClientId,
          Env.oidcRedirect,
          issuer: Env.oidcIssuer,
          scopes: const ['openid', 'profile', 'email'],
          promptValues: const ['login'],
        ),
      );
      await _persist(result);
      state = const AuthState(authenticated: true);
    } catch (e) {
      state = AuthState(error: e.toString());
    }
  }

  Future<void> signOut() async {
    await _storage.deleteAll();
    state = const AuthState(authenticated: false);
  }

  Future<void> _persist(AuthorizationTokenResponse? r) async {
    if (r == null) return;
    if (r.accessToken != null) await _storage.write(key: 'access_token', value: r.accessToken);
    if (r.refreshToken != null) await _storage.write(key: 'refresh_token', value: r.refreshToken);
  }
}

final appAuthProvider = Provider<FlutterAppAuth>((ref) => const FlutterAppAuth());

final authControllerProvider =
    StateNotifierProvider<AuthController, AuthState>((ref) {
  return AuthController(ref.watch(appAuthProvider), ref.watch(secureStorageProvider));
});
