import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/di/app_providers.dart';
import '../../data/local/app_database.dart';
import '../auth/auth_controller.dart';

/// "Today's route" — the field agent's planned deployments. Served from the offline cache; a
/// pull-to-refresh re-fetches from the API. The AppBar shows a pending-sync badge.
final todaysRouteProvider = FutureProvider<List<CachedDeployment>>((ref) async {
  return ref.watch(deploymentsRepositoryProvider).todaysRoute();
});

class RouteTodayScreen extends ConsumerWidget {
  const RouteTodayScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final route = ref.watch(todaysRouteProvider);
    final pending = ref.watch(pendingSyncProvider).valueOrNull ?? 0;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Today's route"),
        actions: [
          if (pending > 0)
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: Chip(
                avatar: const Icon(Icons.sync, size: 18),
                label: Text('$pending'),
              ),
            ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => ref.read(authControllerProvider.notifier).signOut(),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async => ref.refresh(todaysRouteProvider.future),
        child: route.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => ListView(children: [Padding(
            padding: const EdgeInsets.all(24), child: Text('Could not load route: $e'))]),
          data: (items) => items.isEmpty
              ? ListView(children: const [Padding(
                  padding: EdgeInsets.all(24), child: Text('No deployments assigned today.'))])
              : ListView.separated(
                  itemCount: items.length,
                  separatorBuilder: (_, __) => const Divider(height: 1),
                  itemBuilder: (_, i) {
                    final d = items[i];
                    return ListTile(
                      title: Text(d.deploymentNo),
                      subtitle: Text('${d.branchName} · ${d.status}'),
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () => context.push('/complete/${d.id}'),
                    );
                  },
                ),
        ),
      ),
    );
  }
}
