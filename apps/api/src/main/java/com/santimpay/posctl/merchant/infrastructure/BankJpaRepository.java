package com.santimpay.posctl.merchant.infrastructure;

import com.santimpay.posctl.merchant.domain.Bank;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankJpaRepository extends JpaRepository<Bank, UUID> {
    List<Bank> findByActiveTrueOrderByName();
}
