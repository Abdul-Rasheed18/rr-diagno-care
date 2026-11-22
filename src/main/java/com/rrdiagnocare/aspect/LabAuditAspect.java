package com.rrdiagnocare.aspect;

import com.rrdiagnocare.dao.AuditLogDao;
import com.rrdiagnocare.model.AuditLog;
import com.rrdiagnocare.security.SecurityUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect:
 *  1. times every service-layer method and logs slow calls;
 *  2. for methods annotated with {@link LabAudited} (lab technician result entry / status updates)
 *     writes an audit_logs row with user, entity, outcome and execution time.
 *
 * Order 0 => this advice wraps the @Transactional advice (which runs at LOWEST_PRECEDENCE), so the audit
 * row is written after the business transaction commits (or is rolled back) and records failures too.
 * (Not HIGHEST_PRECEDENCE: Spring's ExposeInvocationInterceptor must run first for @annotation binding.)
 */
@Aspect
@Component
@Order(0)
public class LabAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(LabAuditAspect.class);
    private static final long SLOW_THRESHOLD_MS = 500;

    private final AuditLogDao auditLogDao;

    public LabAuditAspect(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    @Around(value = "@annotation(labAudited)", argNames = "pjp,labAudited")
    public Object auditLabOperation(ProceedingJoinPoint pjp, LabAudited labAudited) throws Throwable {
        long start = System.nanoTime();
        String username = SecurityUtil.currentUsername();
        Long entityId = firstLongArg(pjp.getArgs());
        AuditLog entry = new AuditLog();
        entry.setUsername(username);
        entry.setAction(labAudited.action());
        entry.setEntityType(labAudited.entityType());
        entry.setEntityId(entityId);
        try {
            Object result = pjp.proceed();
            entry.setOutcome("SUCCESS");
            entry.setDetails(result instanceof String ? (String) result : pjp.getSignature().getName() + " completed");
            return result;
        } catch (Throwable ex) {
            entry.setOutcome("FAILURE");
            entry.setDetails(pjp.getSignature().getName() + " failed: " + ex.getMessage());
            throw ex;
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            entry.setExecutionMs(ms);
            log.info("[AUDIT] user={} action={} {}#{} outcome={} took={}ms", username, entry.getAction(),
                    entry.getEntityType(), entityId, entry.getOutcome(), ms);
            try {
                auditLogDao.insert(entry);
            } catch (Exception e) {
                log.error("Could not persist audit log entry: {}", e.getMessage());
            }
        }
    }

    /** Execution-time logging for every service method. */
    @Around("execution(public * com.rrdiagnocare.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            if (ms >= SLOW_THRESHOLD_MS) {
                log.warn("[PERF] SLOW {} took {}ms", pjp.getSignature().toShortString(), ms);
            } else if (log.isDebugEnabled()) {
                log.debug("[PERF] {} took {}ms", pjp.getSignature().toShortString(), ms);
            }
        }
    }

    private static Long firstLongArg(Object[] args) {
        for (Object a : args) {
            if (a instanceof Long) {
                return (Long) a;
            }
        }
        return null;
    }
}
