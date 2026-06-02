package com.santimpay.posctl.merchant.application;

import com.santimpay.posctl.merchant.domain.Merchant;
import com.santimpay.posctl.merchant.domain.MerchantStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port for the merchant aggregate (hexagonal). The application layer depends on this
 * interface; the Spring Data adapter lives in {@code infrastructure}. Keeping it here means the
 * domain/application has no compile dependency on JPA wiring.
 */
public interface MerchantRepository {

    Merchant save(Merchant merchant);

    Optional<Merchant> findById(UUID id);

    boolean existsByMerchantNo(String merchantNo);

    Page<Merchant> search(String query, MerchantStatus status, Pageable pageable);
}
