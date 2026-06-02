import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app/router.dart';

void main() {
  runApp(const ProviderScope(child: PosctlFieldApp()));
}

class PosctlFieldApp extends ConsumerWidget {
  const PosctlFieldApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    return MaterialApp.router(
      title: 'SantimPay Field',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: const Color(0xFF0B6E4F), // brand green (shared tokens)
      ),
      routerConfig: router,
    );
  }
}
