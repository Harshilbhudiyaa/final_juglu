package com.infowave.demo.models;

public class Post {
    private String author;
    private String timestamp;
    private String content;
    private int likes;
    private int comments;
    private int imageResId; // fallback for local images
    private int profileImageResId; // fallback for local images
    private String imageUrl;      // NEW: post image URL (from Supabase)
    private String profileUrl;    // NEW: profile image URL (from Supabase)
    private boolean isLiked = false;

    // Constructor for static demo data (local drawables)
    public Post(String author, String timestamp, String content, int likes, int comments, int imageResId, int profileImageResId) {
        this.author = author;
        this.timestamp = timestamp;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.imageResId = imageResId;
        this.profileImageResId = profileImageResId;
        this.imageUrl = null;
        this.profileUrl = null;
    }

    // Constructor for dynamic data (Supabase, network images)
    public Post(String author, String timestamp, String content, int likes, int comments, String imageUrl, String profileUrl) {
        this.author = author;
        this.timestamp = timestamp;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.imageResId = 0;
        this.profileImageResId = 0;
        this.imageUrl = imageUrl;
        this.profileUrl = profileUrl;
    }

    // Getters & Setters
    public String getAuthor() { return author; }
    public String getTimestamp() { return timestamp; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getComments() { return comments; }
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { this.isLiked = liked; }

    // For fallback to local images if needed
    public int getImageResId() { return imageResId; }
    public int getProfileImageResId() { return profileImageResId; }

    // For network images
    public String getImageUrl() { return imageUrl; }
    public String getProfileUrl() { return profileUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
}
