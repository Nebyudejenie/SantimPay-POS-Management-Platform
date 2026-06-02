package com.santimpay.posctl.followup.application;

import com.santimpay.posctl.followup.domain.FollowUp;
import com.santimpay.posctl.followup.domain.FollowUpChannel;
import com.santimpay.posctl.followup.domain.FollowUpOutcome;
import com.santimpay.posctl.shared.domain.DomainException;
import com.santimpay.posctl.shared.security.CurrentUser;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowUpService {

    private final FollowUpRepository repository;

    @Transactional
    @PreAuthorize("hasAuthority('PERM_followup:create')")
    public FollowUp log(UUID merchantId, FollowUpChannel channel, FollowUpOutcome outcome,
                        String notes, String contactedPerson, String contactedPhone,
                        Instant nextActionAt) {
        UUID agent = CurrentUser.id().orElse(null);
        return repository.save(FollowUp.log(merchantId, agent, channel, outcome, notes,
                contactedPerson, contactedPhone, nextActionAt));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_followup:read')")
    public FollowUp get(UUID id) {
        return repository.findById(id).orElseThrow(() -> DomainException.notFound("FollowUp", id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_followup:read')")
    public Page<FollowUp> search(UUID merchantId, UUID agentId, Pageable pageable) {
        return repository.search(merchantId, agentId, pageable);
    }
}
