package com.infowave.demo.models;

public class Friend {
    private String name;
    private String mutual;
    private int imageRes;

    public Friend(String name, String mutual, int imageRes) {
        this.name = name;
        this.mutual = mutual;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getMutual() { return mutual; }
    public int getImageRes() { return imageRes; }
}
