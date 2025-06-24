package com.infowave.demo.models;

public class StatusItem {
    private String imageUrl;
    private String label;
    private boolean isAdd;
    private String storyId;

    // For dynamic stories from Supabase (recommended)
    public StatusItem(String imageUrl, String label, boolean isAdd, String storyId) {
        this.imageUrl = imageUrl;
        this.label = label;
        this.isAdd = isAdd;
        this.storyId = storyId;
    }

    // (Optional) For static local images, keep the old constructor if you want
    private int imageResId = -1;
    public StatusItem(int imageResId, String label, boolean isAdd) {
        this.imageResId = imageResId;
        this.label = label;
        this.isAdd = isAdd;
    }

    // Getters
    public String getImageUrl() { return imageUrl; }
    public String getLabel() { return label; }
    public boolean isAdd() { return isAdd; }
    public String getStoryId() { return storyId; }
    public int getImageResId() { return imageResId; }
}
