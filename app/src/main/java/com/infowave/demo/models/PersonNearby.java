package com.infowave.demo.models;

/**
 * Model representing a person nearby, with name, distance (e.g. "1.2 km away"), and profile image URL.
 */
public class PersonNearby {
    private final String name;
    private final String distance;
    private final String profileImageUrl;  // Profile image URL, not a drawable resource

    public PersonNearby(String name, String distance, String profileImageUrl) {
        this.name = name;
        this.distance = distance;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
