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
                return SupabaseClient.getHeaders();
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(stringRequest);
    }

    public static void getAllStories(Context context, AllStoriesCallback callback) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/stories?select=*";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                callback.onStoriesLoaded(arr);
            } catch (Exception e) {
                callback.onError("Parse error");
            }
        }, error -> callback.onError("Network error: " + error.getMessage())) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders();
            }
        };
        SupabaseClient.getInstance(context).getRequestQueue().add(stringRequest);
    }
}
