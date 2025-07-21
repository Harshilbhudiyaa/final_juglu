package com.infowave.demo.models;

public class SectionHeader implements NotificationListItem {
    private final String title;
    public SectionHeader(String title) { this.title = title; }
    public String getTitle() { return title; }
}
