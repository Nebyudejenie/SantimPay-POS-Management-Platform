import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'auth_controller.dart';

class LoginScreen extends ConsumerWidget {
  const LoginScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authControllerProvider);
    return Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text('SantimPay Field',
                  style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
              const SizedBox(height: 8),
              const Text('Sign in to load your deployments'),
              const SizedBox(height: 32),
              if (auth.loading)
                const CircularProgressIndicator()
              else
                FilledButton.icon(
                  icon: const Icon(Icons.login),
                  label: const Text('Sign in with Keycloak'),
                  onPressed: () => ref.read(authControllerProvider.notifier).signIn(),
                ),
              if (auth.error != null)
                Padding(
                  padding: const EdgeInsets.only(top: 16),
                  child: Text(auth.error!, style: const TextStyle(color: Colors.red)),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
