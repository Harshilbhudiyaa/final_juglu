package com.infowave.demo.models;

// RecommendedUser.java
public class RecommendedUser {
    private String name;
    private String interests;
    private int profileImageRes;

    public RecommendedUser(String name, String interests, int profileImageRes) {
        this.name = name;
        this.interests = interests;
        this.profileImageRes = profileImageRes;
    }

    // Getters
    public String getName() { return name; }
    public String getInterests() { return interests; }
    public int getProfileImageRes() { return profileImageRes; }
}