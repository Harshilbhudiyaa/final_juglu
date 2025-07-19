package com.infowave.demo.supabase;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONObject;

public class StoriesRepository {
    public interface StoryCallback {
        void onStoryLoaded(JSONObject story);
        void onError(String message);
    }

    public interface AllStoriesCallback {
        void onStoriesLoaded(JSONArray storiesArr);
        void onError(String message);
    }

    // Get a single story by its ID (for story viewer)
    public static void getStoryById(Context context, String storyId, StoryCallback callback) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/stories?id=eq." + storyId + "&select=*";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                if (arr.length() > 0) {
                    callback.onStoryLoaded(arr.getJSONObject(0));
                } else {
                    callback.onError("Story not found");
                }
            } catch (Exception e) {
                callback.onError("Parse error");
            }
        }, error -> callback.onError("Network error: " + error.getMessage())) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(stringRequest);
    }

    // Universal: fetch stories using any custom filter (used for self+friends)
    public static void getAllStoriesCustom(Context context, String url, AllStoriesCallback callback) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET, url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                callback.onStoriesLoaded(arr);
            } catch (Exception e) {
                callback.onError("Parse error");
            }
        }, error -> callback.onError("Network error: " + error.getMessage())) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(stringRequest);
    }
    public static void getStoriesForMeAndFriends(Context context, String currentUserId, AllStoriesCallback callback) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/friendships"
                + "?or=(user_one.eq." + currentUserId + ",user_two.eq." + currentUserId + ")"
                + "&status=eq.accepted&select=user_one,user_two";

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET, url, response -> {
            // Step 1: Build userId set
            java.util.Set<String> userIdSet = new java.util.HashSet<>();
            userIdSet.add(currentUserId);

            try {
                org.json.JSONArray arr = new org.json.JSONArray(response);
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.optJSONObject(i);
                    if (obj != null) {
                        String userOne = obj.optString("user_one");
                        String userTwo = obj.optString("user_two");
                        if (userOne.equals(currentUserId) && !userTwo.equals(currentUserId)) {
                            userIdSet.add(userTwo);
                        } else if (userTwo.equals(currentUserId) && !userOne.equals(currentUserId)) {
                            userIdSet.add(userOne);
                        }
                    }
                }
            } catch (Exception e) {
                callback.onError("Friend parse error");
                return;
            }

            // Step 2: Fetch stories for all those users
            if (!userIdSet.isEmpty()) {
                StringBuilder inList = new StringBuilder();
                for (String id : userIdSet) {
                    if (inList.length() > 0) inList.append(",");
                    inList.append(id);
                }
                String storiesUrl = SupabaseClient.getBaseUrl()
                        + "/rest/v1/stories?user_id=in.(" + inList + ")&select=*";

                getAllStoriesCustom(context, storiesUrl, callback);
            }
        }, error -> callback.onError("Network error: " + error.getMessage())) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(request);
    }


}
