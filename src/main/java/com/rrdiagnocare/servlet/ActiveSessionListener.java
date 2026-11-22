package com.rrdiagnocare.servlet;

import com.rrdiagnocare.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

/** Counts active HTTP sessions (online visitors/users) and publishes the counter in the ServletContext. */
public class ActiveSessionListener implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(ActiveSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        int now = counter(se.getSession().getServletContext()).incrementAndGet();
        log.debug("Session created {} - active sessions: {}", se.getSession().getId(), now);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        AtomicInteger c = counter(se.getSession().getServletContext());
        int now = c.updateAndGet(v -> Math.max(0, v - 1));
        log.debug("Session destroyed {} - active sessions: {}", se.getSession().getId(), now);
    }

    /** Lazily creates the shared counter (thread-safe). */
    public static AtomicInteger counter(ServletContext ctx) {
        Object existing = ctx.getAttribute(AppConstants.CTX_ACTIVE_SESSIONS);
        if (existing instanceof AtomicInteger) {
            return (AtomicInteger) existing;
        }
        synchronized (ActiveSessionListener.class) {
            existing = ctx.getAttribute(AppConstants.CTX_ACTIVE_SESSIONS);
            if (existing instanceof AtomicInteger) {
                return (AtomicInteger) existing;
            }
            AtomicInteger created = new AtomicInteger();
            ctx.setAttribute(AppConstants.CTX_ACTIVE_SESSIONS, created);
            return created;
        }
    }
}
