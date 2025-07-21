package com.infowave.demo.models;

public class NotificationItem implements NotificationListItem {
    public enum Action { FOLLOW_REQUEST, FOLLOWING, ACCEPTED, LIKE, STORY }
    private final String userName;
    private final String description;     // e.g. "started following you"
    private final String timeAgo;         // e.g. "3h", "1d"
    private final int avatarResId;        // drawable resource
    private final Action actionType;

    public NotificationItem(String userName, String description,
                            String timeAgo, int avatarResId,
                            Action actionType) {
        this.userName    = userName;
        this.description = description;
        this.timeAgo     = timeAgo;
        this.avatarResId = avatarResId;
        this.actionType  = actionType;
    }
    // getters
    public String getUserName()    { return userName; }
    public String getDescription() { return description; }
    public String getTimeAgo()     { return timeAgo; }
    public int    getAvatarResId() { return avatarResId; }
    public Action getActionType()  { return actionType; }
}
