package com.infowave.demo.models;

public class NotificationEvent {

    public enum Type {
        FOLLOW_REQUEST,
        FOLLOW_ACCEPTED,
        LIKE_POST,
        COMMENT_POST,
        LIKE_STORY,
        COMMENT_STORY
    }

    private String id;
    private Type type;
    private String fromUserId;
    private String fromUserName;
    private String fromUserAvatarUrl;
    private int    fromUserAvatarRes;
    private String message;
    private String createdAt;
    private String thumbnailUrl;
    private String friendshipId;
    private String extraJson;
    private String objectId; // post_id / story_id

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }

    public String getFromUserAvatarUrl() { return fromUserAvatarUrl; }
    public void setFromUserAvatarUrl(String fromUserAvatarUrl) { this.fromUserAvatarUrl = fromUserAvatarUrl; }

    public int getFromUserAvatarRes() { return fromUserAvatarRes; }
    public void setFromUserAvatarRes(int fromUserAvatarRes) { this.fromUserAvatarRes = fromUserAvatarRes; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getFriendshipId() { return friendshipId; }
    public void setFriendshipId(String friendshipId) { this.friendshipId = friendshipId; }

    public String getExtraJson() { return extraJson; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }

    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
}
