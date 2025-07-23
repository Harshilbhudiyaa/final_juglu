package com.infowave.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
    private String id;            // Supabase row id (nullable)
    private String senderId;
    private String receiverId;
    protected String content;     // Message content
    private String createdAt;     // UTC timestamp from DB
    private boolean isReceived;   // UI helper
    private int profileImage;     // (UI only, optional/local)

    // Constructor for UI/adapters
    public ChatMessage(String id, String senderId, String receiverId, String content, String createdAt, boolean isReceived, int profileImage) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.createdAt = createdAt;
        this.isReceived = isReceived;
        this.profileImage = profileImage;
    }

    // Minimal constructor (legacy support)
    public ChatMessage(String senderId, String content, String createdAt, boolean isReceived, int profileImage) {
        this(null, senderId, null, content, createdAt, isReceived, profileImage);
    }

    // From JSON (API response)
    public static ChatMessage fromJson(JSONObject obj, String currentUserId) throws JSONException {
        String id = obj.optString("id", null);
        String senderId = obj.optString("sender_id");
        String receiverId = obj.optString("receiver_id");
        String content = obj.optString("content");
        String createdAt = obj.optString("created_at");
        boolean isReceived = !senderId.equals(currentUserId);
        // Profile image: UI only, set as 0 or implement your logic
        return new ChatMessage(id, senderId, receiverId, content, createdAt, isReceived, 0);
    }

    // ====== Getters ======
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
    public boolean isReceived() { return isReceived; }
    public int getProfileImage() { return profileImage; }
    public String getMessage() { return content; }

    // --- Helper: Is this a call-invite message? ---
    public boolean isCallInvite() {
        try {
            String contentStr = getContent();
            if (contentStr == null) return false;
            JSONObject obj = new JSONObject(contentStr);
            return obj.has("room") && obj.has("call_type");
        } catch (Exception e) {
            return false;
        }
    }

    // --- Helper: Get call type ("audio" or "video"), or null if not a call ---
    public String getCallType() {
        try {
            JSONObject obj = new JSONObject(getContent());
            return obj.optString("call_type", null);
        } catch (Exception e) {
            return null;
        }
    }
}
