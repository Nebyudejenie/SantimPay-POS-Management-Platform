package com.santimpay.posctl.shared.audit;

import com.santimpay.posctl.shared.security.CurrentUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

/**
 * Supplies the current actor id to JPA auditing ({@code @CreatedBy}/{@code @LastModifiedBy}).
 * Also published to the Postgres session var {@code app.actor_id} by the persistence layer so the
 * database-level audit trigger records the same actor.
 */
@Configuration
public class AuditorAwareImpl {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return CurrentUser::id;
    }
}
