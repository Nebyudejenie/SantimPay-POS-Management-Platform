package com.santimpay.posctl.shared.domain;

import lombok.Getter;

/**
 * Base type for domain-rule violations. Mapped to RFC 9457 problem details by the global handler.
 * Use specific subclasses or the provided factories rather than throwing raw runtime exceptions.
 */
@Getter
public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public static DomainException conflict(String message) {
        return new DomainException("conflict", message);
    }

    public static DomainException invalidState(String message) {
        return new DomainException("invalid_state", message);
    }

    public static NotFoundException notFound(String resource, Object id) {
        return new NotFoundException(resource, id);
    }

    /** 404 specialization. */
    public static final class NotFoundException extends DomainException {
        public NotFoundException(String resource, Object id) {
            super("not_found", "%s %s not found".formatted(resource, id));
        }
    }
}
