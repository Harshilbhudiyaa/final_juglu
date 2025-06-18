package com.infowave.demo.models;

public class Message {
    private final String content;
    private final boolean isSent;
    private final String timestamp;

    public Message(String content, boolean isSent, String timestamp) {
        this.content = content;
        this.isSent = isSent;
        this.timestamp = timestamp;
    }

    public String getContent() { return content; }
    public boolean isSent() { return isSent; }
    public String getTimestamp() { return timestamp; }
}