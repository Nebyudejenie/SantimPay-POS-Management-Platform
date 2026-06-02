import 'package:freezed_annotation/freezed_annotation.dart';

part 'deployment.freezed.dart';
part 'deployment.g.dart';

/// A planned/assigned POS deployment for the field agent's route.
@freezed
class Deployment with _$Deployment {
  const factory Deployment({
    required String id,
    required String deploymentNo,
    required String merchantName,
    required String branchName,
    required String scheduledDate,
    required String status, // planned | in_progress | completed | failed
    double? latitude,
    double? longitude,
  }) = _Deployment;

  factory Deployment.fromJson(Map<String, dynamic> json) => _$DeploymentFromJson(json);
}

/// Result of completing a deployment in the field — queued offline and flushed by the SyncEngine.
@freezed
class CompleteDeploymentInput with _$CompleteDeploymentInput {
  const factory CompleteDeploymentInput({
    required String deploymentId,
    required String deviceSerial,
    required String receivedBy,
    required double latitude,
    required double longitude,
    required List<String> photoPaths, // local paths; uploaded to MinIO on sync
    String? signaturePath,
    String? conversationNotes,
  }) = _CompleteDeploymentInput;

  factory CompleteDeploymentInput.fromJson(Map<String, dynamic> json) =>
      _$CompleteDeploymentInputFromJson(json);
}
