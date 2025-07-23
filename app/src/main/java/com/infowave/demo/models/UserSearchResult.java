package com.infowave.demo.models;

public class UserSearchResult {
    private String id;
    private String fullName;
    private String username;
    private String profileImage;
    private String bio;

    // new fields
    private FollowState followState = FollowState.NONE;
    private String friendshipRowId;   // if exists
    private String requesterId;       // who initiated (user_one)

    public UserSearchResult(String id, String fullName, String username,
                            String profileImage, String bio) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.profileImage = profileImage;
        this.bio = bio;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getProfileImage() { return profileImage; }
    public String getBio() { return bio; }

    public FollowState getFollowState() { return followState; }
    public void setFollowState(FollowState followState) { this.followState = followState; }

    public String getFriendshipRowId() { return friendshipRowId; }
    public void setFriendshipRowId(String friendshipRowId) { this.friendshipRowId = friendshipRowId; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
}
