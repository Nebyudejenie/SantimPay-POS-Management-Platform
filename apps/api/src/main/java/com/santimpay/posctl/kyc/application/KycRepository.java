package com.santimpay.posctl.kyc.application;

import com.santimpay.posctl.kyc.domain.KycRequest;
import com.santimpay.posctl.kyc.domain.KycStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KycRepository {

    KycRequest save(KycRequest request);

    Optional<KycRequest> findById(UUID id);

    Page<KycRequest> search(KycStatus status, UUID merchantId, Pageable pageable);
}
