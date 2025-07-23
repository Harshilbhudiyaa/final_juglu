package com.infowave.demo.supabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Handle follow / unfollow / accept / cancel and bulk-status fetch.
 * Table: friendships (user_one, user_two, status)
 */
public class FollowRepository {

    private static final String TAG = "FOLLOW_REPO";

    private static String getCurrentUserId(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }

    // --------------------- Interfaces ---------------------

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface BulkStatusCallback {
        /**
         * key: otherUserId
         * value: JSONObject { "status": "pending/accepted", "friendship_id": "...", "user_one": "...", "user_two": "..." }
         */
        void onSuccess(Map<String, JSONObject> map);
        void onError(String error);
    }

    // --------------------- Public Methods ---------------------

    /**
     * Fetch friendship rows for ALL userIds against current user in one shot.
     * We check where current user is either user_one or user_two.
     */
    public static void fetchBulkStatuses(Context ctx, List<String> userIds, BulkStatusCallback cb) {
        String me = getCurrentUserId(ctx);
        if (me == null) {
            cb.onError("User not logged in");
            return;
        }
        if (userIds == null || userIds.isEmpty()) {
            cb.onSuccess(new HashMap<>());
            return;
        }

        // Build OR filter for all other ids
        // or=(and(user_one.eq.me,user_two.in.(ids)),and(user_two.eq.me,user_one.in.(ids)))
        StringBuilder idsStr = new StringBuilder("(");
        for (int i = 0; i < userIds.size(); i++) {
            idsStr.append(userIds.get(i));
            if (i != userIds.size() - 1) idsStr.append(",");
        }
        idsStr.append(")");

        String base = SupabaseClient.getBaseUrl() + "/rest/v1/friendships?select=id,user_one,user_two,status";
        String filter = "&or=("
                + "and(user_one.eq." + me + ",user_two.in." + idsStr + "),"
                + "and(user_two.eq." + me + ",user_one.in." + idsStr + ")"
                + ")";

        String url = base + filter;
        Log.d(TAG, "Bulk status URL: " + url);

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                resp -> {
                    Map<String, JSONObject> map = new HashMap<>();
                    try {
                        for (int i = 0; i < resp.length(); i++) {
                            JSONObject obj = resp.getJSONObject(i);
                            String u1 = obj.optString("user_one");
                            String u2 = obj.optString("user_two");
                            String other = u1.equals(me) ? u2 : u1;
                            map.put(other, obj);
                        }
                        cb.onSuccess(map);
                    } catch (JSONException e) {
                        cb.onError(e.getMessage());
                    }
                },
                err -> {
                    String er = parseVolleyError(err);
                    Log.e(TAG, "bulk status error: " + er);
                    cb.onError(er);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };

        SupabaseClient.addToRequestQueue(ctx, req);
    }

    /**
     * Send follow request: insert row {user_one=me, user_two=other, status='pending'}
     */
    public static void sendFollowRequest(Context ctx, String otherUserId, SimpleCallback cb) {
        String me = getCurrentUserId(ctx);
        if (me == null) { cb.onError("User not logged in"); return; }

        JSONObject body = new JSONObject();
        try {
            body.put("user_one", me);
            body.put("user_two", otherUserId);
            body.put("status", "pending");
        } catch (JSONException e) {
            cb.onError(e.getMessage());
            return;
        }

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/friendships";
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> cb.onSuccess(),
                error -> cb.onError(parseVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders(ctx);
                h.put("Prefer", "return=representation");
                return h;
            }
        };

        SupabaseClient.addToRequestQueue(ctx, req);
    }

    /**
     * Cancel follow request (I sent it) OR Decline (other sent me) -> delete row.
     */
    public static void deleteFriendship(Context ctx, String friendshipId, SimpleCallback cb) {
        if (friendshipId == null) { cb.onError("friendshipId null"); return; }

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/friendships?id=eq." + friendshipId;
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.DELETE,
                url,
                null,
                resp -> cb.onSuccess(),
                error -> cb.onError(parseVolleyError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };

        SupabaseClient.addToRequestQueue(ctx, req);
    }

    /**
     * Accept request: update status='accepted' for given friendship row
     */
    public static void acceptRequest(Context ctx, String friendshipId, SimpleCallback cb) {
        if (friendshipId == null) { cb.onError("friendshipId null"); return; }

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/friendships?id=eq." + friendshipId;
        JSONObject body = new JSONObject();
        try { body.put("status", "accepted"); } catch (JSONException e) { cb.onError(e.getMessage()); return; }

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.PATCH,
                url,
                null,
                resp -> cb.onSuccess(),
                error -> cb.onError(parseVolleyError(error))
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };

        SupabaseClient.addToRequestQueue(ctx, req);
    }

    // --------------------- Helpers ---------------------

    private static String parseVolleyError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data);
        }
        return error.toString();
    }
}
