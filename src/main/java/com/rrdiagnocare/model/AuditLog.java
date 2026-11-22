package com.rrdiagnocare.model;

import com.rrdiagnocare.util.DateUtil;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Audit trail row written by LabAuditAspect. */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private String outcome = "SUCCESS";
    private long executionMs;
    private LocalDateTime createdAt;

    public String getCreatedAtText() { return DateUtil.format(createdAt); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public long getExecutionMs() { return executionMs; }
    public void setExecutionMs(long executionMs) { this.executionMs = executionMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
