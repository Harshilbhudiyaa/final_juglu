package com.infowave.demo.models;

public class PersonNearby {
    private String id;
    private String name;
    private String distance;
    private String profileImageUrl;

    private FollowState followState = FollowState.NONE;
    private String friendshipRowId;
    private String requesterId;

    // ---- New primary constructor (4 params) ----
    public PersonNearby(String id, String name, String distance, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.distance = distance;
        this.profileImageUrl = profileImageUrl;
    }

    // ---- Backward compatible constructor (3 params) ----
    // (Old code path: id=null)
    public PersonNearby(String name, String distance, String profileImageUrl) {
        this(null, name, distance, profileImageUrl);
    }

    // ------- Getters / Setters -------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getDistance() { return distance; }
    public String getProfileImageUrl() { return profileImageUrl; }

    public FollowState getFollowState() { return followState; }
    public void setFollowState(FollowState followState) { this.followState = followState; }

    public String getFriendshipRowId() { return friendshipRowId; }
    public void setFriendshipRowId(String friendshipRowId) { this.friendshipRowId = friendshipRowId; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
}
