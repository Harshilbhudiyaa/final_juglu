package com.infowave.demo.models;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
    private String id;
    private String senderId;
    private String receiverId;
    protected String content;    // message text or json payload for call
    private String createdAt;
    private boolean isReceived;
    private int profileImage;

    // NEW: Media fields
    private String type;      // "text", "image", "video", "audio"
    private String mediaUrl;  // link to media file, or null

    // ==== Constructors ====

    public ChatMessage(String id, String senderId, String receiverId, String content, String createdAt, boolean isReceived, int profileImage, String type, String mediaUrl) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.createdAt = createdAt;
        this.isReceived = isReceived;
        this.profileImage = profileImage;
        this.type = type;
        this.mediaUrl = mediaUrl;
    }

    // Backward-compatible constructor for pure text/call messages
    public ChatMessage(String id, String senderId, String receiverId, String content, String createdAt, boolean isReceived, int profileImage) {
        this(id, senderId, receiverId, content, createdAt, isReceived, profileImage, "text", null);
    }

    // Minimal constructor (legacy)
    public ChatMessage(String senderId, String content, String createdAt, boolean isReceived, int profileImage) {
        this(null, senderId, null, content, createdAt, isReceived, profileImage, "text", null);
    }

    // --- From JSON (API) ---
    public static ChatMessage fromJson(JSONObject obj, String currentUserId) throws JSONException {
        String id = obj.optString("id", null);
        String senderId = obj.optString("sender_id");
        String receiverId = obj.optString("receiver_id");
        String content = obj.optString("content");
        String createdAt = obj.optString("created_at");
        boolean isReceived = !senderId.equals(currentUserId);

        // Detect type and media url
        String type = obj.optString("type", "text");
        String mediaUrl = obj.optString("media_url", null);

        return new ChatMessage(id, senderId, receiverId, content, createdAt, isReceived, 0, type, mediaUrl);
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

    // === Media support ===
    public String getType() { return type != null ? type : "text"; }
    public String getMediaUrl() { return mediaUrl; }

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
