import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../features/auth/auth_controller.dart';
import '../features/auth/login_screen.dart';
import '../features/deployments/complete_deployment_screen.dart';
import '../features/deployments/route_today_screen.dart';

/// App router. Redirects to /login until authenticated; the field flow is /route -> /complete/:id.
final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/route',
    redirect: (context, state) {
      final authed = ref.read(authControllerProvider).authenticated;
      final loggingIn = state.matchedLocation == '/login';
      if (!authed) return loggingIn ? null : '/login';
      if (loggingIn) return '/route';
      return null;
    },
    routes: [
      GoRoute(path: '/login', builder: (_, __) => const LoginScreen()),
      GoRoute(path: '/route', builder: (_, __) => const RouteTodayScreen()),
      GoRoute(
        path: '/complete/:id',
        builder: (_, s) => CompleteDeploymentScreen(deploymentId: s.pathParameters['id']!),
      ),
    ],
  );
});
