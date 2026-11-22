package com.rrdiagnocare.servlet;

import com.rrdiagnocare.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/** Logs every request URI with its HTTP status and processing time; counts total requests in the ServletContext. */
public class RequestAuditFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestAuditFilter.class);
    private long slowThresholdMs = 1000;
    private AtomicLong totalRequests;

    @Override
    public void init(FilterConfig config) {
        String threshold = config.getInitParameter("slowRequestThresholdMs");
        if (threshold != null) {
            try {
                slowThresholdMs = Long.parseLong(threshold.trim());
            } catch (NumberFormatException ignored) {
                // keep default
            }
        }
        totalRequests = new AtomicLong();
        config.getServletContext().setAttribute(AppConstants.CTX_TOTAL_REQUESTS, totalRequests);
        log.info("RequestAuditFilter initialised (slow threshold {} ms)", slowThresholdMs);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        if (uri.startsWith("/assets/") || uri.equals("/favicon.ico")) {
            chain.doFilter(req, res);
            return;
        }
        long start = System.nanoTime();
        totalRequests.incrementAndGet();
        try {
            chain.doFilter(req, res);
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            String user = request.getRemoteUser() == null ? "-" : request.getRemoteUser();
            String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
            if (ms >= slowThresholdMs) {
                log.warn("[REQ] SLOW {} {}{} -> {} user={} {}ms", request.getMethod(), uri, query, response.getStatus(), user, ms);
            } else {
                log.info("[REQ] {} {}{} -> {} user={} {}ms", request.getMethod(), uri, query, response.getStatus(), user, ms);
            }
        }
    }

    @Override
    public void destroy() {
        log.info("RequestAuditFilter destroyed after {} requests", totalRequests == null ? 0 : totalRequests.get());
    }
}
