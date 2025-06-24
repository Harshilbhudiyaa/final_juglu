package com.infowave.demo.supabase;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;

public class PostsRepository {

    public interface AllPostsCallback {
        void onPostsLoaded(JSONArray postsArr);
        void onError(String message);
    }

    /**
     * Fetch posts from Supabase for all user IDs (current user + friends)
     * @param context   Context (for Volley queue)
     * @param userIds   List of user IDs whose posts you want
     * @param callback  Callback for data or error
     */
    public static void getPostsForUsers(Context context, java.util.List<String> userIds, AllPostsCallback callback) {
        if (userIds == null || userIds.isEmpty()) {
            callback.onPostsLoaded(new JSONArray());
            return;
        }
        // Build comma-separated list for IN clause
        StringBuilder inList = new StringBuilder();
        for (String id : userIds) {
            if (inList.length() > 0) inList.append(",");
            inList.append(id);
        }
        // Query posts, inner join users for author info, newest first
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/posts?user_id=in.(" + inList + ")"
                + "&select=*,users!inner(id,full_name,profile_image)"
                + "&order=created_at.desc";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                callback.onPostsLoaded(arr);
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
