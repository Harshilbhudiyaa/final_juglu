package com.infowave.demo.models;

public class UserSearchResult {
    private String id;
    private String fullName;
    private String username;
    private String profileImage;
    private String bio;

    // CORRECT: 5 parameters!
    public UserSearchResult(String id, String fullName, String username, String profileImage, String bio) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.profileImage = profileImage;
        this.bio = bio;
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getProfileImage() { return profileImage; }
    public String getBio() { return bio; }
}
