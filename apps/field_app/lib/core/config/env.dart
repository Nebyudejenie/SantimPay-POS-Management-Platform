/// Build-time configuration (pass with --dart-define).
/// Example: flutter run --dart-define=API_BASE_URL=https://api.posctl.santimpay.com/api/v1
class Env {
  static const apiBaseUrl =
      String.fromEnvironment('API_BASE_URL', defaultValue: 'http://10.0.2.2:8080/api/v1');
  static const oidcIssuer = String.fromEnvironment('OIDC_ISSUER',
      defaultValue: 'http://10.0.2.2:8081/realms/posctl');
  static const oidcClientId =
      String.fromEnvironment('OIDC_CLIENT_ID', defaultValue: 'posctl-field');
  static const oidcRedirect = String.fromEnvironment('OIDC_REDIRECT',
      defaultValue: 'com.santimpay.posctl:/oauth2redirect');
}
