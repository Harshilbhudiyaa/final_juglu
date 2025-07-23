package com.infowave.demo.models;

public class RecommendedUser {
    private String id;
    private String name;
    private String interestsOrBio;
    private String profileImageUrl;

    // follow info
    private FollowState followState = FollowState.NONE;
    private String friendshipRowId;
    private String requesterId;

    public RecommendedUser(String id, String name, String interestsOrBio, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.interestsOrBio = interestsOrBio;
        this.profileImageUrl = profileImageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getInterests() { return interestsOrBio; }
    public String getProfileImageUrl() { return profileImageUrl; }

    public FollowState getFollowState() { return followState; }
    public void setFollowState(FollowState followState) { this.followState = followState; }

    public String getFriendshipRowId() { return friendshipRowId; }
    public void setFriendshipRowId(String friendshipRowId) { this.friendshipRowId = friendshipRowId; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
}
