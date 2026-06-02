package com.santimpay.posctl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * posctl — SantimPay POS Management Platform.
 *
 * <p>A modular monolith. Each top-level package under {@code com.santimpay.posctl} (except
 * {@code shared}) is a Spring Modulith application module with hard boundaries verified by
 * {@code ModularityTests}. Cross-module communication happens only via published events (outbox)
 * or named application interfaces — never by reaching into another module's internals.
 *
 * <p>{@code @EnableAsync} is required for {@code @ApplicationModuleListener} (which is {@code @Async}
 * + transactional) to actually run handlers off the caller thread; combined with the Spring Modulith
 * JPA event-publication registry this gives at-least-once, retried cross-module delivery.
 */
@Modulithic(systemName = "posctl")
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableScheduling
@EnableAsync
public class PosctlApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosctlApplication.class, args);
    }
}
