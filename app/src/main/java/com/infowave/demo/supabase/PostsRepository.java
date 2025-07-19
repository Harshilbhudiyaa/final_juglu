package com.infowave.demo.supabase;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;

public class PostsRepository {

    public interface AllPostsCallback {
        void onPostsLoaded(JSONArray postsArr);
        void onError(String message);
    }
    public interface LikeCallback {
        void onSuccess();
        void onError(String message);
    }
    public interface LikesCountCallback {
        void onResult(int count, boolean isLikedByMe);
        void onError(String message);
    }
    public interface CommentCallback {
        void onSuccess();
        void onError(String message);
    }
    public interface CommentsCallback {
        void onResult(JSONArray comments);
        void onError(String message);
    }
    public interface PostEngagementCallback {
        void onLoaded(int likeCount, int commentCount, boolean likedByMe);
    }

    // Fetch posts for given user IDs
    public static void getPostsForUsers(Context ctx, java.util.List<String> userIds, AllPostsCallback callback) {
        if (userIds == null || userIds.isEmpty()) {
            callback.onPostsLoaded(new JSONArray());
            return;
        }
        StringBuilder inList = new StringBuilder();
        for (String id : userIds) {
            if (inList.length() > 0) inList.append(",");
            inList.append(id);
        }
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
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.getInstance(ctx).getRequestQueue().add(stringRequest);
    }

    // ------------------------- LIKES (LIKE/UNLIKE/COUNT) -------------------------

    // Add a like (Runnable version)
    public static void likePost(Context ctx, String postId, String userId, Runnable onSuccess, Runnable onError) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/post_engagements";
        JSONObject body = new JSONObject();
        try {
            body.put("post_id", postId);
            body.put("user_id", userId);
            body.put("type", "like");
        } catch (Exception ignored) {}
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                r -> onSuccess.run(),
                e -> onError.run()
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.getInstance(ctx).getRequestQueue().add(req);
    }

    // Add a like (LikeCallback version)
    public static void likePost(Context ctx, String postId, String userId, LikeCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("post_id", postId);
            body.put("type", "like");

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                    SupabaseClient.getBaseUrl() + "/rest/v1/post_engagements",
                    body,
                    response -> callback.onSuccess(),
                    error -> callback.onError("Like failed")) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = SupabaseClient.getHeaders(ctx);
                    headers.put("Prefer", "return=minimal");
                    return headers;
                }
            };
            SupabaseClient.getInstance(ctx).getRequestQueue().add(req);
        } catch (Exception e) {
            callback.onError("Internal error");
        }
    }

    // Remove a like (Runnable version)
    public static void unlikePost(Context ctx, String postId, String userId, Runnable onSuccess, Runnable onError) {
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/post_engagements?post_id=eq." + postId
                + "&user_id=eq." + userId
                + "&type=eq.like";
        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                r -> onSuccess.run(),
                e -> onError.run()
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.getInstance(ctx).getRequestQueue().add(req);
    }

    // Remove a like (LikeCallback version)
    public static void unlikePost(Context ctx, String postId, String userId, LikeCallback callback) {
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/post_engagements?post_id=eq." + postId
                + "&user_id=eq." + userId
                + "&type=eq.like";
        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                response -> callback.onSuccess(),
                error -> callback.onError("Unlike failed")) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = SupabaseClient.getHeaders(ctx);
                headers.put("Prefer", "return=minimal");
                return headers;
            }
        };
        SupabaseClient.getInstance(ctx).getRequestQueue().add(req);
    }

    // Get number of likes for a post and whether the current user liked it
    public static void getLikesCountAndState(Context ctx, String postId, String userId, LikesCountCallback callback) {
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/post_engagements?post_id=eq." + postId + "&type=eq.like&select=user_id";
        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                int count = arr.length();
                boolean isLiked = false;
                for (int i = 0; i < arr.length(); i++) {
                    String uid = arr.getJSONObject(i).optString("user_id");
                    if (uid.equals(userId)) {
                        isLiked = true;
                        break;
                    }
                }
                callback.onResult(count, isLiked);
            } catch (Exception e) {
                callback.onError("Parse error");
            }
        }, error -> callback.onError("Fetch failed")) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.getInstance(ctx).getRequestQueue().add(req);
    }

    // ------------------------- COMMENTS (ADD/GET) -------------------------

    // Add a comment
    public static void addComment(Context ctx, String postId, String userId, String comment, CommentCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("post_id", postId);
            body.put("type", "comment");
            body.put("comment", comment);

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                    SupabaseClient.getBaseUrl() + "/rest/v1/post_engagements",
                    body,
                    response -> callback.onSuccess(),
                    error -> callback.onError("Comment failed")) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = SupabaseClient.getHeaders(ctx);
                    headers.put("Prefer", "return=minimal");
                    return headers;
                }
            };
            SupabaseClient.getInstance(ctx).getRequestQueue().add(req);
        } catch (Exception e) {
            callback.onError("Internal error");
        }
    }

    // Get all comments for a post (returns JSONArray)
    public static void getComments(Context ctx, String postId, CommentsCallback callback) {
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/post_engagements?post_id=eq." + postId + "&type=eq.comment&select=*";
        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                callback.onResult(arr);
            } catch (Exception e) {
                callback.onError("Parse error");
            }
        }, error -> callback.onError("Fetch failed")) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.getInstance(ctx).getRequestQueue().add(req);
    }

    // Fetch like count, comment count, and if user liked
    public static void getPostEngagements(Context ctx, String postId, String userId, PostEngagementCallback cb) {
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/post_engagements?post_id=eq." + postId;

        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                int likeCount = 0, commentCount = 0;
                boolean likedByMe = false;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String type = obj.optString("type", "");
                    if ("like".equals(type)) {
                        likeCount++;
                        if (userId.equals(obj.optString("user_id", ""))) likedByMe = true;
                    }
                    if ("comment".equals(type)) commentCount++;
                }
                cb.onLoaded(likeCount, commentCount, likedByMe);
            } catch (Exception e) {
                cb.onLoaded(0, 0, false);
            }
        }, err -> cb.onLoaded(0, 0, false)) {
            @Override public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.getInstance(ctx).getRequestQueue().add(req);
    }
}
