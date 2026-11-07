package com.inventory.model;

import java.sql.Timestamp;

public class ActivityLog {
    private int logId;
    private int userId;
    private String actionType;
    private String description;
    private Timestamp timestamp;

    public ActivityLog(int logId, int userId, String actionType, String description, Timestamp timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = timestamp;
    }

    public int getLogId() { return logId; }
    public int getUserId() { return userId; }
    public String getActionType() { return actionType; }
    public String getDescription() { return description; }
    public Timestamp getTimestamp() { return timestamp; }
}