package com.infowave.demo.models;

public class FeatureItem {
    private final int iconRes;
    private final String title;
    private final String description;

    public FeatureItem(int iconRes, String title, String description) {
        this.iconRes = iconRes;
        this.title = title;
        this.description = description;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}