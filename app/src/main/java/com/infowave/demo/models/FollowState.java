package com.infowave.demo.models;

public enum FollowState {
    NONE,               // No relationship (show "Follow")
    REQUEST_SENT,       // I sent request (show "Requested / Cancel")
    REQUEST_RECEIVED,   // Other sent me request (show "Accept / Decline")
    FOLLOWING           // Accepted (show "Following / Unfollow")
}
