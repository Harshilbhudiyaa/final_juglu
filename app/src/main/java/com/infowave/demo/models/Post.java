package com.infowave.demo.models;

public class Post {
    private String id; // Post ID from Supabase
    private String author;
    private String timestamp;
    private String content;
    private int likes;
    private int comments;
    private int imageResId; // fallback for local images
    private int profileImageResId; // fallback for local images
    private String imageUrl;      // post image URL (from Supabase)
    private String profileUrl;    // profile image URL (from Supabase)
    private boolean isLiked = false;

    // Constructor for static demo data (local drawables)
    public Post(String author, String timestamp, String content, int likes, int comments, int imageResId, int profileImageResId) {
        this.id = null;
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
    public Post(String id, String author, String timestamp, String content, int likes, int comments, String imageUrl, String profileUrl) {
        this.id = id;
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

    // ---- GETTERS & SETTERS ----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuthor() { return author; }
    public String getTimestamp() { return timestamp; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; } // <--- ADD THIS
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { this.isLiked = liked; }
    public int getImageResId() { return imageResId; }
    public int getProfileImageResId() { return profileImageResId; }
    public String getImageUrl() { return imageUrl; }
    public String getProfileUrl() { return profileUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
}
