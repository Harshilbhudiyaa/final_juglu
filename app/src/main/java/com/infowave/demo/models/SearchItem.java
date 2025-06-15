package com.infowave.demo.models;

public class SearchItem {
    private int profileImage;
    private String name;
    private String username;

    public SearchItem(int profileImage, String name, String username) {
        this.profileImage = profileImage;
        this.name = name;
        this.username = username;
    }

    public int getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(int profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return "@" + username;
    }
} 