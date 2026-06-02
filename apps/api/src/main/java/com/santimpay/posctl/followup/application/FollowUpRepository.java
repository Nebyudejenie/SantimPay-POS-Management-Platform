package com.santimpay.posctl.followup.application;

import com.santimpay.posctl.followup.domain.FollowUp;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowUpRepository {

    FollowUp save(FollowUp followUp);

    Optional<FollowUp> findById(UUID id);

    Page<FollowUp> search(UUID merchantId, UUID agentId, Pageable pageable);
}
