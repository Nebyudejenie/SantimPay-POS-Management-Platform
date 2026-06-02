import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

import '../../core/di/app_providers.dart';

/// Complete-deployment flow: scan device serial/IMEI → confirm receiver → capture GPS + photo →
/// submit. Submission goes through the offline outbox so it works without connectivity and is
/// replayed idempotently on reconnect.
class CompleteDeploymentScreen extends ConsumerStatefulWidget {
  final String deploymentId;
  const CompleteDeploymentScreen({super.key, required this.deploymentId});

  @override
  ConsumerState<CompleteDeploymentScreen> createState() => _CompleteDeploymentScreenState();
}

class _CompleteDeploymentScreenState extends ConsumerState<CompleteDeploymentScreen> {
  final _receivedBy = TextEditingController();
  final _notes = TextEditingController();
  String? _serial;
  String? _photoPath;
  Position? _pos;
  bool _submitting = false;

  @override
  void dispose() {
    _receivedBy.dispose();
    _notes.dispose();
    super.dispose();
  }

  Future<void> _scan() async {
    final code = await Navigator.of(context).push<String>(
      MaterialPageRoute(builder: (_) => const _ScanPage()),
    );
    if (code != null) setState(() => _serial = code);
  }

  Future<void> _capturePhoto() async {
    final img = await ImagePicker().pickImage(source: ImageSource.camera, imageQuality: 70);
    if (img != null) setState(() => _photoPath = img.path);
  }

  Future<void> _captureGps() async {
    final perm = await Geolocator.requestPermission();
    if (perm == LocationPermission.denied || perm == LocationPermission.deniedForever) return;
    final pos = await Geolocator.getCurrentPosition();
    setState(() => _pos = pos);
  }

  Future<void> _submit() async {
    if (_serial == null || _receivedBy.text.isEmpty || _pos == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Scan a device, capture GPS, and enter the receiver first.')),
      );
      return;
    }
    setState(() => _submitting = true);
    await ref.read(deploymentsRepositoryProvider).completeDeployment(
          deploymentId: widget.deploymentId,
          deviceSerial: _serial!,
          receivedBy: _receivedBy.text,
          latitude: _pos!.latitude,
          longitude: _pos!.longitude,
          conversationNotes: _notes.text,
          photoPaths: _photoPath != null ? [_photoPath!] : const [],
        );
    ref.invalidate(pendingSyncProvider);
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Deployment queued — will sync when online.')),
      );
      context.pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Complete deployment')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _Tile(
            icon: Icons.qr_code_scanner,
            label: _serial == null ? 'Scan device serial / IMEI' : 'Device: $_serial',
            done: _serial != null,
            onTap: _scan,
          ),
          _Tile(
            icon: Icons.my_location,
            label: _pos == null
                ? 'Capture GPS at branch'
                : 'GPS: ${_pos!.latitude.toStringAsFixed(5)}, ${_pos!.longitude.toStringAsFixed(5)}',
            done: _pos != null,
            onTap: _captureGps,
          ),
          _Tile(
            icon: Icons.photo_camera,
            label: _photoPath == null ? 'Capture deployment photo' : 'Photo captured ✓',
            done: _photoPath != null,
            onTap: _capturePhoto,
          ),
          const SizedBox(height: 8),
          TextField(controller: _receivedBy,
              decoration: const InputDecoration(labelText: 'Received by (person at merchant)')),
          const SizedBox(height: 8),
          TextField(controller: _notes, maxLines: 3,
              decoration: const InputDecoration(labelText: 'Conversation notes')),
          const SizedBox(height: 24),
          FilledButton(
            onPressed: _submitting ? null : _submit,
            child: _submitting
                ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                : const Text('Submit deployment'),
          ),
        ],
      ),
    );
  }
}

class _Tile extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool done;
  final VoidCallback onTap;
  const _Tile({required this.icon, required this.label, required this.done, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: Icon(icon, color: done ? Colors.green : null),
        title: Text(label),
        trailing: done ? const Icon(Icons.check_circle, color: Colors.green) : const Icon(Icons.chevron_right),
        onTap: onTap,
      ),
    );
  }
}

/// Minimal full-screen scanner; pops the first decoded value.
class _ScanPage extends StatelessWidget {
  const _ScanPage();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Scan device')),
      body: MobileScanner(
        onDetect: (capture) {
          final code = capture.barcodes.isNotEmpty ? capture.barcodes.first.rawValue : null;
          if (code != null) Navigator.of(context).pop(code);
        },
      ),
    );
  }
}
