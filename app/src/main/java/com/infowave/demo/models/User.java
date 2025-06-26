package com.infowave.demo.models;

public class User {
    private String fullName;
    private String username;
    private String phone;
    private String password;
    private String bio;

    public User(String fullName, String username, String phone, String password, String bio) {
        this.fullName = fullName;
        this.username = username;
        this.phone = phone;
        this.password = password;
        this.bio = bio;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getBio() {
        return bio;
    }
}
