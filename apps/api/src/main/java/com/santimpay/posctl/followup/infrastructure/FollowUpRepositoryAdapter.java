package com.santimpay.posctl.followup.infrastructure;

import com.santimpay.posctl.followup.application.FollowUpRepository;
import com.santimpay.posctl.followup.domain.FollowUp;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class FollowUpRepositoryAdapter implements FollowUpRepository {

    private final FollowUpJpaRepository jpa;

    @Override
    public FollowUp save(FollowUp followUp) {
        return jpa.save(followUp);
    }

    @Override
    public Optional<FollowUp> findById(UUID id) {
        return jpa.findById(id).filter(f -> !f.getAudit().isDeleted());
    }

    @Override
    public Page<FollowUp> search(UUID merchantId, UUID agentId, Pageable pageable) {
        return jpa.search(merchantId, agentId, pageable);
    }
}
