package com.rrdiagnocare.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a lab operation whose execution must be timed and written to the audit trail by {@link LabAuditAspect}.
 * The first Long argument of the method is recorded as the entity id; a String return value becomes the details.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LabAudited {
    /** Audit action code, e.g. RESULT_ENTRY. */
    String action();

    /** Entity type of the first Long argument. */
    String entityType() default "BOOKING";
}
