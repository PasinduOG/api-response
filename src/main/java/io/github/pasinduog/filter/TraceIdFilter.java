package io.github.pasinduog.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that generates and manages trace IDs for each HTTP request.
 * <p>
 * This filter extends {@link OncePerRequestFilter} to ensure it executes exactly once per request,
 * even in complex servlet filter chain scenarios. It generates a unique {@link UUID} for each
 * incoming request and stores it in both the request attributes and SLF4J's MDC (Mapped Diagnostic Context).
 * </p>
 * <p>
 * The trace ID is automatically included in {@link io.github.pasinduog.dto.ApiResponse} responses
 * and can be used for distributed tracing and log correlation across microservices.
 * </p>
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Generates a unique UUID for each request</li>
 *   <li>Stores trace ID in request attributes for controller access</li>
 *   <li>Stores trace ID in SLF4J MDC for automatic logging correlation</li>
 *   <li>Automatically cleans up MDC after request completion</li>
 * </ul>
 *
 * @author Pasindu OG
 * @version 2.0.0
 * @since 2.0.0
 * @see OncePerRequestFilter
 */
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * Default constructor for TraceIdFilter.
     * <p>
     * This constructor is automatically invoked by Spring's servlet filter registration mechanism.
     * </p>
     */
    public TraceIdFilter() {
        // Default constructor for Spring filter registration
    }

    /**
     * Processes each HTTP request to generate and manage trace IDs.
     * <p>
     * This method generates a unique UUID, stores it in both the request attributes and SLF4J's MDC,
     * processes the request through the filter chain, and ensures MDC cleanup in a finally block
     * to prevent memory leaks.
     * </p>
     *
     * @param request     The HTTP servlet request being processed.
     * @param response    The HTTP servlet response to be returned.
     * @param filterChain The filter chain to continue request processing.
     * @throws ServletException If an error occurs during servlet processing.
     * @throws IOException      If an I/O error occurs during request/response handling.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        UUID traceId = UUID.randomUUID();
        try {
            request.setAttribute("traceId", traceId);
            MDC.put("traceId", traceId.toString());
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
