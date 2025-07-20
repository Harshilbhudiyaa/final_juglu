package com.infowave.demo.supabase;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONObject;

public class ProfileRepository {

    public static class Profile {
        public String name;
        public String bio;
        public String imageUrl;

        public Profile(String name, String bio, String imageUrl) {
            this.name = name;
            this.bio = bio;
            this.imageUrl = imageUrl;
        }
    }

    public interface ProfileCallback {
        void onSuccess(Profile user);
        void onError(String error);
    }

    // ADDED userId param for precise query
    public static void getLoggedInUserProfile(Context context, String userId, ProfileCallback callback) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/users?id=eq." + userId;

        Log.d("PROFILE_REPOSITORY", "Requesting user profile with: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("PROFILE_REPOSITORY", "Profile fetch response: " + response.toString());
                    if (response.length() > 0) {
                        try {
                            JSONObject obj = response.getJSONObject(0);
                            String name = obj.optString("username", "No Name");
                            String bio = obj.optString("bio", "");
                            String imageUrl = obj.optString("profile_image", "");
                            callback.onSuccess(new Profile(name, bio, imageUrl));
                        } catch (Exception e) {
                            Log.e("PROFILE_REPOSITORY", "Parse error: " + e.getMessage());
                            callback.onError("Error parsing user data: " + e.getMessage());
                        }
                    } else {
                        Log.w("PROFILE_REPOSITORY", "User not found for id: " + userId);
                        callback.onError("User not found");
                    }
                },
                error -> {
                    Log.e("PROFILE_REPOSITORY", "Volley error: " + error);
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("PROFILE_REPOSITORY", "Network error body: " + body);
                        callback.onError("Error fetching user: " + body);
                    } else {
                        callback.onError("Error fetching user: " + error.toString());
                    }
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = SupabaseClient.getHeaders(context);
                Log.d("PROFILE_REPOSITORY", "Request headers: " + headers);
                return headers;
            }
        };

        SupabaseClient.addToRequestQueue(context, request);
    }
}
