package com.tamdao.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    
    // Simple thread-safe in-memory rate limiter per IP address
    private final Map<String, RequestTracker> ipRequestMap = new ConcurrentHashMap<>();

    private static class RequestTracker {
        final AtomicInteger count = new AtomicInteger(0);
        final long resetTime;

        RequestTracker() {
            // Reset window is set to 60 seconds from creation
            this.resetTime = System.currentTimeMillis() + 60000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > resetTime;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        
        // Clean up or retrieve tracking entry
        RequestTracker tracker = ipRequestMap.compute(clientIp, (ip, currentTracker) -> {
            if (currentTracker == null || currentTracker.isExpired()) {
                return new RequestTracker();
            }
            return currentTracker;
        });

        int currentCount = tracker.count.incrementAndGet();
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("Too Many Requests! (Hành động bị giới hạn Rate Limit: tối đa 5 requests/phút)");
            return false;
        }

        // Add headers to response showing rate limit status
        response.addHeader("X-Rate-Limit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(MAX_REQUESTS_PER_MINUTE - currentCount));
        
        return true;
    }
}
