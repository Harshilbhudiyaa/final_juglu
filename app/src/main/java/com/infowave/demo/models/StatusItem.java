package com.infowave.demo.models;

public class StatusItem {
    private int imageResId;
    private String label;
    private boolean isAdd;

    public StatusItem(int imageResId, String label, boolean isAdd) {
        this.imageResId = imageResId;
        this.label = label;
        this.isAdd = isAdd;
    }

    public int getImageResId() { return imageResId; }
    public String getLabel() { return label; }
    public boolean isAdd() { return isAdd; }
} 