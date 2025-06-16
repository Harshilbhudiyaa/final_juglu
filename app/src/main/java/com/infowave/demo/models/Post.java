package com.infowave.demo.models;

public class Post {
    private String author;
    private String timestamp;
    private String content;
    private int likes;
    private int comments;
    private int imageResId;
    private int profileImageResId;
    private boolean isLiked = false;
    public Post(String author, String timestamp, String content, int likes, int comments, int imageResId, int profileImageResId) {

        this.author = author;
        this.timestamp = timestamp;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.imageResId = imageResId;
        this.profileImageResId = profileImageResId;

    }
    public void setLiked(boolean liked) { this.isLiked = liked; }
    public boolean isLiked() { return isLiked; }
    public void setLikes(int likes) { this.likes = likes; }

    public String getAuthor() {
        return author;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public int getLikes() {
        return likes;
    }

    public int getComments() {
        return comments;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }

    public static class Chat {
        public String name;
        public String message;
        public String time;
        public int imageResId;
        public boolean isUnread;

        public Chat(String name, String message, String time, int imageResId, boolean isUnread) {
            this.name = name;
            this.message = message;
            this.time = time;
            this.imageResId = imageResId;
            this.isUnread = isUnread;
        }
    }
}