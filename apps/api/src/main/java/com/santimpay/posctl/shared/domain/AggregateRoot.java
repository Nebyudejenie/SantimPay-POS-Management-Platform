package com.santimpay.posctl.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * Base class for aggregate roots.
 *
 * <p>Extends Spring Data's {@link AbstractAggregateRoot} so that domain events registered via
 * {@link #raise(Object)} are published when the aggregate is saved — these are then captured by the
 * outbox and relayed (see {@code shared.outbox}). Carries the UUIDv7 identity and {@link AuditMetadata}.
 *
 * @param <T> the concrete aggregate type (self-type for fluent event registration)
 */
@Getter
@MappedSuperclass
public abstract class AggregateRoot<T extends AggregateRoot<T>> extends AbstractAggregateRoot<T> {

    @jakarta.persistence.Id
    @jakarta.persistence.Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Embedded
    private final AuditMetadata audit = new AuditMetadata();

    /** Optimistic-locking version. On the mapped-superclass because JPA forbids @Version in @Embeddable. */
    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Transient
    private final transient List<Object> raisedEvents = new ArrayList<>();

    protected AggregateRoot() {
        // for JPA
    }

    protected AggregateRoot(UUID id) {
        this.id = id;
    }

    /** Assigns a time-ordered UUIDv7 if none is set yet (used by factories). */
    protected void assignIdentityIfAbsent() {
        if (this.id == null) {
            this.id = UuidV7.generate();
        }
    }

    /** Register a domain event to be dispatched on persist; also tracked for the outbox. */
    protected T raise(Object event) {
        this.raisedEvents.add(event);
        return andEvent(event);
    }

    @Transient
    public List<Object> getRaisedEvents() {
        return Collections.unmodifiableList(raisedEvents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregateRoot<?> other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
