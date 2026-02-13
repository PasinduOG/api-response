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
 * Filter that generates and manages trace IDs for incoming HTTP requests.
 * <p>
 * This filter generates a unique UUID for each request and stores it in both
 * the request attributes and SLF4J's MDC (Mapped Diagnostic Context) for
 * use in logging throughout the request lifecycle.
 * </p>
 *
 * @author Pasindu OG
 * @version 2.0.0
 * @since 1.0.0
 */
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * Default constructor for Spring filter registration.
     */
    public TraceIdFilter() {
        // Default constructor for Spring filter registration
    }

    /**
     * Generates a trace ID and stores it in request attributes and MDC.
     * <p>
     * The trace ID is cleared from MDC after the request completes.
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
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
