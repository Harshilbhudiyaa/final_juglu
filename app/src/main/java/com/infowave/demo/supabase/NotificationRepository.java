package com.infowave.demo.supabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.infowave.demo.models.NotificationEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationRepository {

    private static final String TAG = "NOTIF_REPO";

    public interface NotificationCallback {
        void onSuccess(List<NotificationEvent> events);
        void onError(String error);
    }

    private static String getCurrentUserId(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }

    public static void fetchNotifications(Context ctx, NotificationCallback cb) {
        String me = getCurrentUserId(ctx);
        if (me == null) {
            cb.onError("User not logged in");
            return;
        }

        List<NotificationEvent> all = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger counter = new AtomicInteger(4);
        StringBuilder errorBuff = new StringBuilder();

        // 1) Follow requests to me (pending)
        String urlFollowReq = SupabaseClient.getBaseUrl()
                + "/rest/v1/friendships"
                + "?user_two=eq." + me
                + "&status=eq.pending"
                + "&select=id,user_one,user_two,status,created_at,users!friendships_user_one_fkey(full_name,profile_image)";

        // 2) Follow accepted (I requested)
        String urlFollowAccepted = SupabaseClient.getBaseUrl()
                + "/rest/v1/friendships"
                + "?user_one=eq." + me
                + "&status=eq.accepted"
                + "&select=id,user_one,user_two,status,created_at,users!friendships_user_two_fkey(full_name,profile_image)";

        // 3) Post engagements (no thumb_url)
        String urlPostEng = SupabaseClient.getBaseUrl()
                + "/rest/v1/post_engagements"
                + "?select=id,user_id,post_id,type,comment,created_at,"
                + "posts!post_engagements_post_id_fkey(user_id),"
                + "users!post_engagements_user_id_fkey(full_name,profile_image)"
                + "&posts.user_id=eq." + me;

        // 4) Story engagements (no thumb_url)
        String urlStoryEng = SupabaseClient.getBaseUrl()
                + "/rest/v1/story_engagements"
                + "?select=id,story_id,user_id,type,comment,created_at,"
                + "stories!story_engagements_story_id_fkey(user_id),"
                + "users!story_engagements_user_id_fkey(full_name,profile_image)"
                + "&stories.user_id=eq." + me;

        Runnable finishIfDone = () -> {
            if (counter.decrementAndGet() == 0) {
                if (errorBuff.length() > 0 && all.isEmpty()) {
                    cb.onError(errorBuff.toString());
                } else {
                    Collections.sort(all, new Comparator<NotificationEvent>() {
                        @Override
                        public int compare(NotificationEvent o1, NotificationEvent o2) {
                            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                        }
                    });
                    cb.onSuccess(all);
                }
            }
        };

        // --- 1
        JsonArrayRequest req1 = new JsonArrayRequest(Request.Method.GET, urlFollowReq, null,
                resp -> { parseFollowRequests(resp, all, true); finishIfDone.run(); },
                err  -> { errorBuff.append(parseErr(err)).append("\n"); finishIfDone.run(); }) {
            @Override public Map<String, String> getHeaders(){ return SupabaseClient.getHeaders(ctx);} };
        SupabaseClient.addToRequestQueue(ctx, req1);

        // --- 2
        JsonArrayRequest req2 = new JsonArrayRequest(Request.Method.GET, urlFollowAccepted, null,
                resp -> { parseFollowRequests(resp, all, false); finishIfDone.run(); },
                err  -> { errorBuff.append(parseErr(err)).append("\n"); finishIfDone.run(); }) {
            @Override public Map<String, String> getHeaders(){ return SupabaseClient.getHeaders(ctx);} };
        SupabaseClient.addToRequestQueue(ctx, req2);

        // --- 3
        JsonArrayRequest req3 = new JsonArrayRequest(Request.Method.GET, urlPostEng, null,
                resp -> { parsePostEngagements(resp, all); finishIfDone.run(); },
                err  -> { errorBuff.append(parseErr(err)).append("\n"); finishIfDone.run(); }) {
            @Override public Map<String, String> getHeaders(){ return SupabaseClient.getHeaders(ctx);} };
        SupabaseClient.addToRequestQueue(ctx, req3);

        // --- 4
        JsonArrayRequest req4 = new JsonArrayRequest(Request.Method.GET, urlStoryEng, null,
                resp -> { parseStoryEngagements(resp, all); finishIfDone.run(); },
                err  -> { errorBuff.append(parseErr(err)).append("\n"); finishIfDone.run(); }) {
            @Override public Map<String, String> getHeaders(){ return SupabaseClient.getHeaders(ctx);} };
        SupabaseClient.addToRequestQueue(ctx, req4);
    }

    // ---------- Parsers ----------
    private static void parseFollowRequests(JSONArray arr, List<NotificationEvent> out, boolean pending) {
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                NotificationEvent ev = new NotificationEvent();
                ev.setId(o.optString("id"));
                ev.setFriendshipId(o.optString("id"));
                ev.setCreatedAt(o.optString("created_at"));
                ev.setFromUserId(o.optString("user_one"));

                JSONObject userObj = o.optJSONObject("users");
                if (userObj != null) {
                    ev.setFromUserName(userObj.optString("full_name", "Someone"));
                    ev.setFromUserAvatarUrl(userObj.optString("profile_image", null));
                } else {
                    ev.setFromUserName("Someone");
                }

                if (pending) {
                    ev.setType(NotificationEvent.Type.FOLLOW_REQUEST);
                    ev.setMessage("started following you");
                } else {
                    ev.setType(NotificationEvent.Type.FOLLOW_ACCEPTED);
                    ev.setMessage("accepted your follow request");
                }
                out.add(ev);
            }
        } catch (Exception e) {
            Log.e(TAG, "parseFollowRequests error: " + e.getMessage());
        }
    }

    private static void parsePostEngagements(JSONArray arr, List<NotificationEvent> out) {
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                NotificationEvent ev = new NotificationEvent();
                ev.setId(o.optString("id"));
                ev.setCreatedAt(o.optString("created_at"));
                ev.setFromUserId(o.optString("user_id"));
                ev.setObjectId(o.optString("post_id", null));

                JSONObject userObj = o.optJSONObject("users");
                if (userObj != null) {
                    ev.setFromUserName(userObj.optString("full_name", "Someone"));
                    ev.setFromUserAvatarUrl(userObj.optString("profile_image", null));
                } else {
                    ev.setFromUserName("Someone");
                }

                // thumbnail not available now
                ev.setThumbnailUrl(null);

                String type = o.optString("type", "like").toLowerCase();
                if ("like".equals(type)) {
                    ev.setType(NotificationEvent.Type.LIKE_POST);
                    ev.setMessage("liked your post");
                } else {
                    ev.setType(NotificationEvent.Type.COMMENT_POST);
                    String cmt = o.optString("comment", "");
                    ev.setMessage("commented: " + cmt);
                }
                out.add(ev);
            }
        } catch (Exception e) {
            Log.e(TAG, "parsePostEngagements error: " + e.getMessage());
        }
    }

    private static void parseStoryEngagements(JSONArray arr, List<NotificationEvent> out) {
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                NotificationEvent ev = new NotificationEvent();
                ev.setId(o.optString("id"));
                ev.setCreatedAt(o.optString("created_at"));
                ev.setFromUserId(o.optString("user_id"));
                ev.setObjectId(o.optString("story_id", null));

                JSONObject userObj = o.optJSONObject("users");
                if (userObj != null) {
                    ev.setFromUserName(userObj.optString("full_name", "Someone"));
                    ev.setFromUserAvatarUrl(userObj.optString("profile_image", null));
                } else {
                    ev.setFromUserName("Someone");
                }

                ev.setThumbnailUrl(null);

                String type = o.optString("type", "like").toLowerCase();
                if ("like".equals(type)) {
                    ev.setType(NotificationEvent.Type.LIKE_STORY);
                    ev.setMessage("liked your story");
                } else {
                    ev.setType(NotificationEvent.Type.COMMENT_STORY);
                    String cmt = o.optString("comment", "");
                    ev.setMessage("replied to your story: " + cmt);
                }
                out.add(ev);
            }
        } catch (Exception e) {
            Log.e(TAG, "parseStoryEngagements error: " + e.getMessage());
        }
    }

    private static String parseErr(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data);
        }
        return error.toString();
    }
}
