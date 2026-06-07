package com.eventflow.eventflow.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;
    private final Map<String, RateWindow> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRule rule = ruleFor(request);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + ":" + rule.scope();
        RateWindow window = windows.computeIfAbsent(key, ignored -> new RateWindow(Instant.now().toEpochMilli()));

        if (!window.allow(rule.maxRequests())) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Too many requests. Try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitRule ruleFor(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("POST".equals(method) && (path.equals("/api/v1/login") || path.equals("/api/v1/register") || path.equals("/api/v1/refresh-token") || path.equals("/api/v1/admin/login"))) {
            return new RateLimitRule("auth", 10);
        }

        if ("POST".equals(method) && path.equals("/api/v1/me/change-password")) {
            return new RateLimitRule("password", 5);
        }

        if ("POST".equals(method) && path.equals("/api/v1/registrations")) {
            return new RateLimitRule("registrations", 60);
        }

        if (path.startsWith("/api/")) {
            return new RateLimitRule("api", 300);
        }

        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record RateLimitRule(String scope, int maxRequests) {
    }

    private static final class RateWindow {
        private long startsAt;
        private int requests;

        private RateWindow(long startsAt) {
            this.startsAt = startsAt;
        }

        private synchronized boolean allow(int maxRequests) {
            long now = Instant.now().toEpochMilli();
            if (now - startsAt >= WINDOW_MS) {
                startsAt = now;
                requests = 0;
            }
            requests++;
            return requests <= maxRequests;
        }
    }
}
