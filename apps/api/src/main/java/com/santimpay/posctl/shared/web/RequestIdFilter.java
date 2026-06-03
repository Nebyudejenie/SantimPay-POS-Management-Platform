package com.santimpay.posctl.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Assigns/propagates a correlation id per request. Echoed in the {@code X-Request-Id} response
 * header, put on the SLF4J MDC (so it appears in every structured log line and Tempo trace) and the
 * Postgres session var {@code app.request_id} (so the DB audit trigger records it too).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "request_id";
    public static final String REQUEST_ID_ATTR = "posctl.request_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String requestId = request.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = "req_" + UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_KEY, requestId);
        // Set directly on the request — never via RequestContextHolder, which throws
        // IllegalStateException when this filter re-runs on the ERROR dispatch (no bound context),
        // turning every error into a 500. (That masked auth 401s + actuator/docs as 500s.)
        request.setAttribute(REQUEST_ID_ATTR, requestId);
        response.setHeader(HEADER, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
