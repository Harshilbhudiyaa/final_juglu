package com.infowave.demo.models;

// PersonNearby.java
public class PersonNearby {
    private String name;
    private String distance;
    private int profileImageRes;

    public PersonNearby(String name, String distance, int profileImageRes) {
        this.name = name;
        this.distance = distance;
        this.profileImageRes = profileImageRes;
    }

    // Getters
    public String getName() { return name; }
    public String getDistance() { return distance; }
    public int getProfileImageRes() { return profileImageRes; }
}

