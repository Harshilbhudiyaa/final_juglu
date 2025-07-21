package com.infowave.demo.models;
public class RecommendedUser {
    private String id;
    private String name;
    private String interests;
    private String profileImageUrl;

    public RecommendedUser(String id, String name, String interests, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.interests = interests;
        this.profileImageUrl = profileImageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getInterests() { return interests; }
    public String getProfileImageUrl() { return profileImageUrl; }
}
