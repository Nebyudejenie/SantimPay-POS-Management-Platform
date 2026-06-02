package com.santimpay.posctl.shared.web;

import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Maps exceptions to RFC 9457 problem+json (matches the documented error contract in docs/04).
 * Always includes the correlation {@code request_id} so a client error can be traced in Loki/Tempo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE = "https://errors.posctl/";

    @ExceptionHandler(DomainException.NotFoundException.class)
    ProblemDetail handleNotFound(DomainException.NotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), req);
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleDomain(DomainException ex, HttpServletRequest req) {
        HttpStatus status = "conflict".equals(ex.getCode()) ? HttpStatus.CONFLICT
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return problem(status, ex.getCode(), ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.UNPROCESSABLE_ENTITY, "validation",
                "Validation failed", req);
        List<?> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> java.util.Map.of("field", fe.getField(),
                        "code", String.valueOf(fe.getCode()),
                        "message", String.valueOf(fe.getDefaultMessage())))
                .toList();
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "forbidden", "Access denied", req);
    }

    private ProblemDetail problem(HttpStatus status, String type, String detail,
                                  HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(BASE + type));
        pd.setTitle(status.getReasonPhrase());
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("request_id", currentRequestId());
        return pd;
    }

    private String currentRequestId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        Object rid = attrs == null ? null
                : attrs.getAttribute(RequestIdFilter.REQUEST_ID_ATTR, RequestAttributes.SCOPE_REQUEST);
        return rid == null ? null : rid.toString();
    }
}
