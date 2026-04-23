package com.librelibraria.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * AuditLog entity for tracking all library actions.
 */
@Entity(tableName = "audit_log")
public class AuditLog {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String action;
    private String details;
    private String userEmail;
    private long timestamp;
    private String entityType;
    private long entityId;
    private boolean isSynced;

    public AuditLog() {
        this.timestamp = System.currentTimeMillis();
        this.isSynced = false;
    }

    public AuditLog(String action, String details) {
        this();
        this.action = action;
        this.details = details;
    }

    public AuditLog(String action, String details, String entityType, long entityId) {
        this(action, details);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public long getEntityId() { return entityId; }
    public void setEntityId(long entityId) { this.entityId = entityId; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public String getFormattedTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm, dd.MM.yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}
