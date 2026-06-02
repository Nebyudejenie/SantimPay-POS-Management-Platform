package com.santimpay.posctl.merchant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.santimpay.posctl.merchant.application.MerchantService;
import com.santimpay.posctl.merchant.application.OnboardMerchantCommand;
import com.santimpay.posctl.merchant.domain.Merchant;
import com.santimpay.posctl.shared.outbox.OutboxEventRepository;
import com.santimpay.posctl.support.IntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Walking-skeleton test — locks Sprint 1's Definition of Done end to end:
 * onboard a merchant -> row persisted -> domain event captured in the transactional outbox -> the
 * DB audit trigger records the change. (The Redis relay then publishes the outbox row; covered by a
 * separate relay test.)
 */
class MerchantFlowIntegrationTest extends IntegrationTest {

    @Autowired MerchantService merchantService;
    @Autowired OutboxEventRepository outboxRepository;
    @Autowired JdbcTemplate jdbc;

    @Test
    @WithMockUser(authorities = {"PERM_merchant:create", "PERM_merchant:read"})
    void onboard_persistsMerchant_writesOutbox_andAudits() {
        Merchant created = merchantService.onboard(new OnboardMerchantCommand(
                "M-IT-001", "Integration Trading PLC", "ITP", "TIN-IT", "retail"));

        // 1) persisted + readable
        assertThat(merchantService.get(created.getId()).getMerchantNo()).isEqualTo("M-IT-001");

        // 2) domain event captured in the outbox within the same transaction
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(outboxRepository.findAll())
                        .anyMatch(e -> e.getEventType().equals("MerchantOnboarded")
                                && e.getAggregateId().equals(created.getId())));

        // 3) DB audit trigger recorded the insert
        Integer auditRows = jdbc.queryForObject(
                "select count(*) from audit.audit_log where schema_name='merchant' "
                        + "and table_name='merchants' and row_id=?",
                Integer.class, created.getId());
        assertThat(auditRows).isGreaterThanOrEqualTo(1);
    }
}
