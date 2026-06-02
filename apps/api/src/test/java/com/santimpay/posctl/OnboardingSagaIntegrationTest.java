package com.santimpay.posctl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.santimpay.posctl.deployment.application.CompleteDeploymentCommand;
import com.santimpay.posctl.deployment.application.DeploymentService;
import com.santimpay.posctl.deployment.application.PlanDeploymentCommand;
import com.santimpay.posctl.deployment.domain.Deployment;
import com.santimpay.posctl.inventory.application.DeviceService;
import com.santimpay.posctl.inventory.application.ReceiveDeviceCommand;
import com.santimpay.posctl.inventory.domain.DeviceStatus;
import com.santimpay.posctl.inventory.domain.PosDevice;
import com.santimpay.posctl.kyc.application.KycService;
import com.santimpay.posctl.kyc.domain.KycRequest;
import com.santimpay.posctl.kyc.domain.KycStatus;
import com.santimpay.posctl.merchant.application.MerchantService;
import com.santimpay.posctl.merchant.application.OnboardMerchantCommand;
import com.santimpay.posctl.merchant.domain.Merchant;
import com.santimpay.posctl.merchant.domain.MerchantStatus;
import com.santimpay.posctl.support.IntegrationTest;
import com.santimpay.posctl.workflow.application.WorkflowRepository;
import com.santimpay.posctl.workflow.application.WorkflowService;
import com.santimpay.posctl.workflow.domain.WorkflowInstance;
import com.santimpay.posctl.workflow.domain.WorkflowStatus;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * End-to-end proof of the core operational saga across all 5 modules, exercised through the real
 * application services with async {@code @ApplicationModuleListener} event handling (Spring Modulith
 * event publication registry) over real Postgres + Redis (Testcontainers). This is the test that
 * guards the whole onboarding→activation→deployment flow against regressions.
 */
class OnboardingSagaIntegrationTest extends IntegrationTest {

    @Autowired MerchantService merchants;
    @Autowired KycService kyc;
    @Autowired WorkflowService workflows;
    @Autowired WorkflowRepository workflowRepository;
    @Autowired DeploymentService deployments;
    @Autowired DeviceService devices;

    @Test
    @WithMockUser(authorities = {
            "PERM_merchant:create", "PERM_merchant:read", "PERM_merchant:approve",
            "PERM_kyc:read", "PERM_kyc:review", "PERM_kyc:approve",
            "PERM_workflow:read", "PERM_workflow:approve",
            "PERM_device:create", "PERM_device:read",
            "PERM_deployment:create", "PERM_deployment:read", "PERM_deployment:complete"})
    void fullOnboardingAndDeploymentSaga() {
        // 1) Onboard merchant -> kyc listener opens a SUBMITTED KycRequest (async).
        Merchant merchant = merchants.onboard(new OnboardMerchantCommand(
                "M-SAGA-1", "Saga Trading PLC", "Saga", "TIN-S", "retail"));

        KycRequest kycReq = await().atMost(Duration.ofSeconds(10)).until(
                () -> kyc.search(KycStatus.SUBMITTED, merchant.getId(),
                                org.springframework.data.domain.PageRequest.of(0, 1))
                        .stream().findFirst().orElse(null),
                r -> r != null);

        // 2) Review + approve KYC -> initiates a MERCHANT_ACTIVATION workflow (PENDING).
        kyc.assignToMe(kycReq.getId());
        kyc.approve(kycReq.getId());

        WorkflowInstance wf = await().atMost(Duration.ofSeconds(10)).until(
                () -> workflows.search(WorkflowStatus.PENDING, "merchant",
                                org.springframework.data.domain.PageRequest.of(0, 10))
                        .stream().filter(w -> w.getSubjectId().equals(merchant.getId()))
                        .findFirst().orElse(null),
                w -> w != null);

        // 3) A different actor approves the workflow -> ApprovalGranted -> merchant activates (async).
        workflows.approve(wf.getId(), "verified");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(merchants.get(merchant.getId()).getStatus())
                        .isEqualTo(MerchantStatus.ACTIVE));

        // 4) Receive a device, plan + complete a deployment -> DeviceAssigned -> device DEPLOYED.
        PosDevice device = devices.receive(new ReceiveDeviceCommand(
                "SN-SAGA-1", "PAX-A920", "PAX", "T-SAGA-1", "IMEI-SAGA-1"));
        UUID branchId = UUID.randomUUID(); // a real branch id in production; synthetic here

        Deployment deployment = deployments.plan(new PlanDeploymentCommand(
                "D-SAGA-1", LocalDate.now(), merchant.getId(), branchId, null));
        deployments.complete(new CompleteDeploymentCommand(
                deployment.getId(), device.getId(), "Receiver", 9.0, 38.7, "delivered", "trello-1"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(devices.get(device.getId()).getStatus())
                        .isEqualTo(DeviceStatus.DEPLOYED));
    }
}
