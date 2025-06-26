// Friends.java
package com.infowave.demo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Friends implements Parcelable {
    private final String name;
    private final String mutual;
    private final int imageRes;
    private String bio;
    private int friendsCount;
    private int postsCount;
    private int likesCount;

    public Friends(String name, String mutual, int imageRes) {
        this.name = name;
        this.mutual = mutual;
        this.imageRes = imageRes;
        this.bio = "Digital creator | Photography enthusiast";
        this.friendsCount = 128;
        this.postsCount = 42;
        this.likesCount = 1560;
    }

    // Getters
    public String getName() { return name; }
    public String getMutual() { return mutual; }
    public int getImageRes() { return imageRes; }
    public String getBio() { return bio; }
    public int getFriendsCount() { return friendsCount; }
    public int getPostsCount() { return postsCount; }
    public int getLikesCount() { return likesCount; }

    // Parcelable implementation
    protected Friends(Parcel in) {
        name = in.readString();
        mutual = in.readString();
        imageRes = in.readInt();
        bio = in.readString();
        friendsCount = in.readInt();
        postsCount = in.readInt();
        likesCount = in.readInt();
    }

    public static final Creator<Friends> CREATOR = new Creator<Friends>() {
        @Override
        public Friends createFromParcel(Parcel in) {
            return new Friends(in);
        }

        @Override
        public Friends[] newArray(int size) {
            return new Friends[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mutual);
        dest.writeInt(imageRes);
        dest.writeString(bio);
        dest.writeInt(friendsCount);
        dest.writeInt(postsCount);
        dest.writeInt(likesCount);
    }
}