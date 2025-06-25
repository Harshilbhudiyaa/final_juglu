package com.infowave.demo.models;

// models/Post.java
public class Own_posts {
    private String id;
    private int imageUrl;
    // add more fields if needed (likesCount, commentsCount...)

    public Own_posts(String id, int imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }
    public String getId() { return id; }
    public int getImageUrl() { return imageUrl; }
}
