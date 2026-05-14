package org.gfmanca.the_guillotine.security.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * A filter that ensures every request contains a unique correlation ID, which is used for
 * tracing and logging purposes. If a correlation ID is not provided in the request headers,
 * a new one is generated and added to both the logging context and the response headers.
 *
 * This filter extracts the correlation ID from the "X-Correlation-Id" header of an incoming
 * request. If the header is absent or the ID is empty, a new correlation ID is generated
 * using a UUID. The correlation ID is stored in the MDC (Mapped Diagnostic Context) for use
 * in logging throughout the request's lifecycle. The correlation ID is also added to the
 * "X-Correlation-Id" header of the response.
 *
 * Extends:
 * - {@link OncePerRequestFilter}: Ensures the filter is executed only once per request.
 *
 * Responsibilities:
 * - Read the "X-Correlation-Id" header from the incoming request.
 * - Generate a new correlation ID if none exists in the request header.
 * - Populate the MDC with the correlation ID for logging.
 * - Add the correlation ID to the "X-Correlation-Id" header of the response.
 * - Clear the MDC after processing the request to prevent leakage of data between requests.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String correlationId = request.getHeader("X-Correlation-Id");

            if (correlationId == null || correlationId.isBlank()) {
                correlationId = "REQ-" + UUID.randomUUID();
            }

            MDC.put(CORRELATION_ID, correlationId);
            response.setHeader("X-Correlation-Id", correlationId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
