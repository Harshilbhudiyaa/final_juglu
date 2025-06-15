package com.infowave.demo.model;

public class Chat {
    public String name;
    public String message;
    public String time;
    public int imageResId;
    public boolean isUnread;

    public Chat(String name, String message, String time, int imageResId, boolean isUnread) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.imageResId = imageResId;
        this.isUnread = isUnread;
    }
} 