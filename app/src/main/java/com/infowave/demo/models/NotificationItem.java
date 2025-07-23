package com.infowave.demo.models;

public class NotificationItem implements NotificationListItem {

    public enum Action {
        FOLLOW_REQUEST, FOLLOWING, ACCEPTED,
        LIKE, STORY,
        LIKE_POST, COMMENT_POST, LIKE_STORY, COMMENT_STORY
    }

    private final String userName;
    private final String description;
    private final String timeAgo;
    private final int avatarResId;
    private final Action actionType;

    private String avatarUrl;
    private String thumbUrl;
    private String friendshipId;
    private String rawId;
    private String isoCreatedAt;

    public NotificationItem(String userName, String description,
                            String timeAgo, int avatarResId,
                            Action actionType) {
        this.userName    = userName;
        this.description = description;
        this.timeAgo     = timeAgo;
        this.avatarResId = avatarResId;
        this.actionType  = actionType;
    }

    public NotificationItem setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl; return this;
    }
    public NotificationItem setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl; return this;
    }
    public NotificationItem setFriendshipId(String friendshipId) {
        this.friendshipId = friendshipId; return this;
    }
    public NotificationItem setRawId(String rawId) {
        this.rawId = rawId; return this;
    }
    public NotificationItem setIsoCreatedAt(String isoCreatedAt) {
        this.isoCreatedAt = isoCreatedAt; return this;
    }

    public String getUserName()      { return userName; }
    public String getDescription()   { return description; }
    public String getTimeAgo()       { return timeAgo; }
    public int    getAvatarResId()   { return avatarResId; }
    public Action getActionType()    { return actionType; }
    public String getAvatarUrl()     { return avatarUrl; }
    public String getThumbUrl()      { return thumbUrl; }
    public String getFriendshipId()  { return friendshipId; }
    public String getRawId()         { return rawId; }
    public String getIsoCreatedAt()  { return isoCreatedAt; }
}
