package com.santimpay.posctl.followup.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * Call-center / field follow-up contact log (the "2026 POS Phone Call Follow-Up" form). Each row is
 * one contact attempt with its outcome and the next action. References merchant/task/agent by id.
 */
@Getter
@Entity
@Table(name = "follow_ups", schema = "followup")
public class FollowUp extends AggregateRoot<FollowUp> {

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "agent_id")
    private UUID agentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private FollowUpChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome")
    private FollowUpOutcome outcome;

    @Column(name = "notes")
    private String notes;

    @Column(name = "contacted_person")
    private String contactedPerson;

    @Column(name = "contacted_phone")
    private String contactedPhone;

    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated;

    @Column(name = "contacted_at", nullable = false)
    private Instant contactedAt;

    @Column(name = "next_action_at")
    private Instant nextActionAt;

    protected FollowUp() {}

    public static FollowUp log(UUID merchantId, UUID agentId, FollowUpChannel channel,
                               FollowUpOutcome outcome, String notes, String contactedPerson,
                               String contactedPhone, Instant nextActionAt) {
        if (channel == null) {
            throw DomainException.invalidState("channel is required");
        }
        FollowUp f = new FollowUp();
        f.assignIdentityIfAbsent();
        f.merchantId = merchantId;
        f.agentId = agentId;
        f.channel = channel;
        f.outcome = outcome;
        f.notes = notes;
        f.contactedPerson = contactedPerson;
        f.contactedPhone = contactedPhone;
        f.contactedAt = Instant.now();
        f.nextActionAt = nextActionAt;
        f.aiGenerated = false;
        return f;
    }

    /**
     * AI-drafted follow-up: queued for a human to review/contact, not auto-sent. {@code aiGenerated}
     * is true and no outcome/agent is set yet — an agent picks it up, contacts the merchant, and
     * records the real outcome via {@link #log}.
     */
    public static FollowUp aiDraft(UUID merchantId, FollowUpChannel channel, String notes) {
        FollowUp f = new FollowUp();
        f.assignIdentityIfAbsent();
        f.merchantId = merchantId;
        f.channel = channel == null ? FollowUpChannel.CALL : channel;
        f.notes = notes;
        f.contactedAt = Instant.now();
        f.aiGenerated = true;
        return f;
    }
}
