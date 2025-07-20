package com.infowave.demo.supabase;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRepository {

    public static class Profile {
        public String id;
        public String fullName;
        public String username;
        public String phone;
        public String email;    // <--- added
        public String bio;
        public String imageUrl;
        public String gender;
        public Boolean isPrivate;

        public Profile(String id, String fullName, String username, String phone, String email,
                       String bio, String imageUrl, String gender, Boolean isPrivate) {
            this.id = id;
            this.fullName = fullName;
            this.username = username;
            this.phone = phone;
            this.email = email;
            this.bio = bio;
            this.imageUrl = imageUrl;
            this.gender = gender;
            this.isPrivate = isPrivate;
        }
    }

    public interface ProfileCallback {
        void onSuccess(Profile user);
        void onError(String error);
    }

    public interface UpdateProfileCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Fetch user by userId
    public static void getLoggedInUserProfile(Context context, String userId, ProfileCallback callback) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/users?id=eq." + userId;
        Log.d("PROFILE_REPOSITORY", "[getLoggedInUserProfile] URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("PROFILE_REPOSITORY", "[getLoggedInUserProfile] Response: " + response.toString());
                    if (response.length() > 0) {
                        try {
                            JSONObject obj = response.getJSONObject(0);
                            Log.d("PROFILE_REPOSITORY", "[getLoggedInUserProfile] Raw JSONObject: " + obj.toString());
                            Profile profile = new Profile(
                                    obj.optString("id"),
                                    obj.optString("full_name", ""),
                                    obj.optString("username", ""),
                                    obj.optString("phone", ""),
                                    obj.optString("real_email", ""),      // <--- fetch email
                                    obj.optString("bio", ""),
                                    obj.optString("profile_image", ""),
                                    obj.optString("gender", ""),
                                    obj.has("is_private") && !obj.isNull("is_private") ? obj.getBoolean("is_private") : false
                            );
                            callback.onSuccess(profile);
                        } catch (Exception e) {
                            Log.e("PROFILE_REPOSITORY", "[getLoggedInUserProfile] Parse error: " + e.getMessage(), e);
                            callback.onError("Error parsing user data: " + e.getMessage());
                        }
                    } else {
                        Log.w("PROFILE_REPOSITORY", "[getLoggedInUserProfile] User not found for id: " + userId);
                        callback.onError("User not found");
                    }
                },
                error -> {
                    Log.e("PROFILE_REPOSITORY", "[getLoggedInUserProfile] Volley error: " + error);
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("PROFILE_REPOSITORY", "[getLoggedInUserProfile] Network error body: " + body);
                        callback.onError("Error fetching user: " + body);
                    } else {
                        callback.onError("Error fetching user: " + error.toString());
                    }
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = SupabaseClient.getHeaders(context);
                Log.d("PROFILE_REPOSITORY", "[getLoggedInUserProfile] Request headers: " + headers);
                return headers;
            }
        };

        Log.d("PROFILE_REPOSITORY", "[getLoggedInUserProfile] Adding request to queue...");
        SupabaseClient.addToRequestQueue(context, request);
    }

    // Update user profile (now includes email)
    public static void updateUserProfile(Context context, String userId,
                                         String fullName, String username, String email, String bio, String imageUrl,
                                         String gender, Boolean isPrivate,
                                         UpdateProfileCallback callback) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/users?id=eq." + userId;

        Log.d("PROFILE_REPOSITORY", "[updateUserProfile] PATCH URL: " + url);

        JSONObject body = new JSONObject();
        try {
            if (fullName != null) body.put("full_name", fullName);
            if (username != null) body.put("username", username);
            if (email != null) body.put("real_email", email);      // <--- add email
            if (bio != null) body.put("bio", bio);
            if (imageUrl != null) body.put("profile_image", imageUrl);
            if (gender != null) body.put("gender", gender);
            if (isPrivate != null) body.put("is_private", isPrivate);
            Log.d("PROFILE_REPOSITORY", "[updateUserProfile] PATCH body: " + body.toString());
        } catch (JSONException e) {
            Log.e("PROFILE_REPOSITORY", "[updateUserProfile] Error creating JSON body: " + e.getMessage(), e);
            callback.onFailure("Error creating JSON: " + e.getMessage());
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    Log.d("PROFILE_REPOSITORY", "[updateUserProfile] PATCH success, empty or minimal response: " + response);
                    callback.onSuccess();
                },
                error -> {
                    Log.e("PROFILE_REPOSITORY", "[updateUserProfile] Volley error: " + error);
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String bodyStr = new String(error.networkResponse.data);
                        Log.e("PROFILE_REPOSITORY", "[updateUserProfile] Network error body: " + bodyStr);
                        callback.onFailure("Error updating profile: " + bodyStr);
                    } else {
                        callback.onFailure("Error updating profile: " + error.toString());
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = SupabaseClient.getHeaders(context);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                Log.d("PROFILE_REPOSITORY", "[updateUserProfile] PATCH headers: " + headers);
                return headers;
            }
        };

        Log.d("PROFILE_REPOSITORY", "[updateUserProfile] Adding PATCH request to queue...");
        SupabaseClient.addToRequestQueue(context, request);
    }
}
