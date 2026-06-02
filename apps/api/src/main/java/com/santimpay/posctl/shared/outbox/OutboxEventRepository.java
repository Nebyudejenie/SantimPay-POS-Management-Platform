package com.santimpay.posctl.shared.outbox;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("select e from OutboxEvent e where e.dispatched = false order by e.occurredAt asc")
    List<OutboxEvent> findUndispatched(Limit limit);
}
