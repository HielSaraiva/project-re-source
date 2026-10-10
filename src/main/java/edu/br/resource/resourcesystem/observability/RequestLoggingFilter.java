package edu.br.resource.resourcesystem.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Value("${resource.logging.slow-request-ms:1000}")
    private long slowRequestMs = 1000;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Map<String, String> previous = MDC.getCopyOfContextMap();

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        long started = System.nanoTime();
        boolean failed = false;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException failure) {
            failed = true;
            log.error("event=http_failure failure={}", FailureDetails.describe(failure));
            throw failure;
        } finally {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            Object mapping = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

            String route = mapping == null ? "unmapped" : mapping.toString();
            int status = failed ? 500 : response.getStatus();
            if (status >= 500 || elapsed >= Math.max(1, slowRequestMs))
                log.warn(
                        "event=http_completed method={} route={} status={} durationMs={}",
                        request.getMethod(),
                        route,
                        status,
                        elapsed);
            else
                log.info(
                        "event=http_completed method={} route={} status={} durationMs={}",
                        request.getMethod(),
                        route,
                        status,
                        elapsed);
            if (previous == null) MDC.clear();
            else MDC.setContextMap(previous);
        }
    }
}
