package com.infowave.demo.models;

public class Post {
    private String author;
    private String timestamp;
    private String content;
    private int likes;
    private int comments;

    public Post(String author, String timestamp, String content, int likes, int comments) {
        this.author = author;
        this.timestamp = timestamp;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
    }

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
} 