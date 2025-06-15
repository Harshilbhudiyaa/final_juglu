package com.infowave.demo.models;

public class Notification {
    public static final int TYPE_LIKE = 1;
    public static final int TYPE_COMMENT = 2;
    public static final int TYPE_FOLLOW = 3;
    public static final int TYPE_MENTION = 4;

    private String user;
    private String action;
    private String timestamp;
    private int type;

    public Notification(String user, String action, String timestamp, int type) {
        this.user = user;
        this.action = action;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public String getAction() {
        return action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getType() {
        return type;
    }
} 