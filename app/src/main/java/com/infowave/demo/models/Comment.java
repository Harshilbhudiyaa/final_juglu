package com.infowave.demo.models;

public class Comment {
    private String username;
    private String comment;
    private String time;
    private int profileRes;

    public Comment(String username, String comment, String time, int profileRes) {
        this.username = username;
        this.comment = comment;
        this.time = time;
        this.profileRes = profileRes;
    }

    public String getUsername() { return username; }
    public String getComment() { return comment; }
    public String getTime() { return time; }
    public int getProfileRes() { return profileRes; }
}
