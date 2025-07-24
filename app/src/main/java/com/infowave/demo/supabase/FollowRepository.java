package com.infowave.demo.supabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles follow / unfollow / accept / decline and bulk status operations
 * on the `friendships` table in Supabase.
 */
public class FollowRepository {

    private static final String TAG = "FOLLOW_REPO";

    /** Callback for single‑action operations */
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    /** Callback for fetching bulk friendship statuses */
    public interface BulkStatusCallback {
        void onSuccess(Map<String, JSONObject> map);
        void onError(String error);
    }

    /** Optional listener for auto UI refresh after any change */
    public interface OnNeedsRefreshListener {
        void onNeedsRefresh();
    }
    private static OnNeedsRefreshListener refreshListener;
    public static void setOnNeedsRefreshListener(OnNeedsRefreshListener l) {
        refreshListener = l;
    }
    private static void fireRefresh() {
        if (refreshListener != null) refreshListener.onNeedsRefresh();
    }

    // ────────────────────────────────────────────────────────────────────────────────

    /** Send a follow request, but only once per user pair. */
    public static void sendFollowRequest(Context ctx, String otherUserId, SimpleCallback cb) {
        String me = getCurrentUserId(ctx);
        if (me == null) { cb.onError("User not logged in"); return; }

        checkExistingFriendship(ctx, me, otherUserId, new SimpleCheckCallback() {
            @Override
            public void onResult(JSONObject existingRow) {
                if (existingRow != null) {
                    // Already exists → treat as success
                    cb.onSuccess();
                    fireRefresh();
                } else {
                    insertFriendship(ctx, me, otherUserId, cb);
                }
            }
            @Override
            public void onError(String error) {
                cb.onError(error);
            }
        });
    }

    /** Cancel, decline or unfollow by deleting the friendship row. */
    public static void deleteFriendship(Context ctx, String friendshipId, SimpleCallback cb) {
        if (friendshipId == null) { cb.onError("friendshipId null"); return; }

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/friendships?id=eq." + friendshipId;
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.DELETE, url, null,
                resp -> { cb.onSuccess(); fireRefresh(); },
                err  -> cb.onError(parseVolleyError(err))
        ) {
            @Override public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    /** Accept an incoming follow request (set status = 'accepted'). */
    public static void acceptRequest(Context ctx, String friendshipId, SimpleCallback cb) {
        if (friendshipId == null) { cb.onError("friendshipId null"); return; }

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/friendships?id=eq." + friendshipId;
        JSONObject body = new JSONObject();
        try { body.put("status", "accepted"); } catch (JSONException e) { cb.onError(e.getMessage()); return; }

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.PATCH, url, null,
                resp -> { cb.onSuccess(); fireRefresh(); },
                err  -> cb.onError(parseVolleyError(err))
        ) {
            @Override public byte[] getBody() {
                return body.toString().getBytes();
            }
            @Override public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    /** Fetch friendship statuses in bulk for a list of user IDs. */
    public static void fetchBulkStatuses(Context ctx, List<String> userIds, BulkStatusCallback cb) {
        String me = getCurrentUserId(ctx);
        if (me == null) { cb.onError("User not logged in"); return; }
        if (userIds == null || userIds.isEmpty()) {
            cb.onSuccess(new HashMap<>());
            return;
        }

        StringBuilder ids = new StringBuilder("(");
        for (int i = 0; i < userIds.size(); i++) {
            ids.append(userIds.get(i));
            if (i < userIds.size() - 1) ids.append(",");
        }
        ids.append(")");

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/friendships?select=id,user_one,user_two,status"
                + "&or=(and(user_one.eq." + me + ",user_two.in." + ids + "),"
                + "and(user_two.eq." + me + ",user_one.in." + ids + "))";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                resp -> {
                    Map<String, JSONObject> map = new HashMap<>();
                    try {
                        for (int i = 0; i < resp.length(); i++) {
                            JSONObject o = resp.getJSONObject(i);
                            String u1 = o.optString("user_one");
                            String u2 = o.optString("user_two");
                            String other = u1.equals(me) ? u2 : u1;
                            map.put(other, o);
                        }
                        cb.onSuccess(map);
                    } catch (JSONException e) {
                        cb.onError(e.getMessage());
                    }
                },
                err -> cb.onError(parseVolleyError(err))
        ) {
            @Override public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    // ────────────────────────────────────────────────────────────────────────────────

    /** Helper: Check if a friendship row exists in either direction. */
    private interface SimpleCheckCallback {
        void onResult(JSONObject existingRow);
        void onError(String error);
    }

    private static void checkExistingFriendship(
            Context ctx,
            String me,
            String other,
            SimpleCheckCallback cb
    ) {
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/friendships"
                + "?or=(and(user_one.eq." + me + ",user_two.eq." + other + "),"
                + "and(user_one.eq." + other + ",user_two.eq." + me + "))"
                + "&select=id,user_one,user_two,status";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    if (response.length() > 0) {
                        try {
                            cb.onResult(response.getJSONObject(0));
                        } catch (JSONException e) {
                            cb.onError(e.getMessage());
                        }
                    } else {
                        cb.onResult(null);
                    }
                },
                error -> cb.onError(parseVolleyError(error))
        ) {
            @Override public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    /** Helper: Insert a new friendship row with robust parsing to avoid ParseError. */
    private static void insertFriendship(
            Context ctx,
            String me,
            String other,
            SimpleCallback cb
    ) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/friendships";
        JSONObject body = new JSONObject();
        try {
            body.put("user_one", me);
            body.put("user_two", other);
            body.put("status", "pending");
        } catch (JSONException e) {
            cb.onError(e.getMessage());
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST, url, body,
                response -> {
                    cb.onSuccess();
                    fireRefresh();
                },
                error -> {
                    String er = parseVolleyError(error);
                    Log.e(TAG, "sendFollowRequest error: " + er);
                    // Treat duplicates as success
                    if (er.contains("duplicate") || er.contains("Unique") || er.contains("23505")) {
                        cb.onSuccess();
                        fireRefresh();
                    } else {
                        cb.onError(er);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders(ctx);
                h.put("Prefer", "return=representation");
                return h;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse resp) {
                try {
                    String charset = HttpHeaderParser.parseCharset(resp.headers, "utf-8");
                    String json    = resp.data == null
                            ? ""
                            : new String(resp.data, charset).trim();

                    JSONObject obj;
                    if (json.isEmpty()) {
                        obj = new JSONObject();
                    } else if (json.startsWith("[")) {
                        JSONArray arr = new JSONArray(json);
                        obj = (arr.length() > 0 ? arr.getJSONObject(0) : new JSONObject());
                    } else if (json.startsWith("{")) {
                        obj = new JSONObject(json);
                    } else {
                        obj = new JSONObject();
                    }

                    return Response.success(obj, HttpHeaderParser.parseCacheHeaders(resp));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        SupabaseClient.addToRequestQueue(ctx, req);
    }

    /** Helper: Extract a clean error message from VolleyError */
    private static String parseVolleyError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data);
        }
        return error.toString();
    }

    /** Helper: Read current user ID from SharedPreferences */
    private static String getCurrentUserId(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }
}
