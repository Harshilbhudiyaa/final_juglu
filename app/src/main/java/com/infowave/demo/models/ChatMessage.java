package com.infowave.demo.models;

public class ChatMessage {
    private String sender;
    private String message;
    private String timestamp;
    private boolean isReceived;
    private int profileImage;

    public ChatMessage(String sender, String message, String timestamp, boolean isReceived, int profileImage) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.isReceived = isReceived;
        this.profileImage = profileImage;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public int getProfileImage() {
        return profileImage;
    }
} 