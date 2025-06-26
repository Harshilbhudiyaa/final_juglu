package com.infowave.demo.supabase;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.infowave.demo.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class UsersRepository {

    public interface UserCallback {
        void onSuccess(String userId);
        void onFailure(String error);
    }

    /**
     * ➊ Registers (or upserts) a user record.
     */
    public static void registerUser(Context ctx, User user, UserCallback cb) {
        String url = getBaseUrl()
                + "/rest/v1/users?on_conflict=username";  // upsert by username

        JSONObject body = new JSONObject();
        try {
            body.put("full_name",   user.getFullName());
            body.put("username",    user.getUsername());
            body.put("phone",       user.getPhone());
            body.put("password",    user.getPassword());
            body.put("bio",         user.getBio());
            body.put("status",      "active");
        } catch (JSONException e) {
            cb.onFailure(e.getMessage());
            return;
        }

        StringRequest req = new StringRequest(
                Request.Method.POST, url,
                resp -> cb.onSuccess("upsert_ok"),
                err -> {
                    String e = err.networkResponse != null
                            ? new String(err.networkResponse.data)
                            : err.toString();
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json"; }
            @Override public Map<String,String> getHeaders() {
                Map<String,String> h = SupabaseClient.getHeaders();
                h.put("Prefer", "resolution=merge-duplicates");
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    /**
     * ➋ Checks if a user with given phone exists.
     *     • onSuccess → returns userId
     *     • onFailure → "NOT_FOUND" if none, or error message
     */
    public static void checkUserExists(Context ctx, String phone, UserCallback cb) {
        String url = getBaseUrl()
                + "/rest/v1/users?select=id&phone=eq." + phone;

        StringRequest req = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        if (arr.length() > 0) {
                            String id = arr.getJSONObject(0).getString("id");
                            cb.onSuccess(id);
                        } else {
                            cb.onFailure("NOT_FOUND");
                        }
                    } catch (Exception e) {
                        cb.onFailure(e.getMessage());
                    }
                },
                err -> {
                    String e = err.networkResponse != null
                            ? new String(err.networkResponse.data)
                            : err.toString();
                    cb.onFailure(e);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                // grab your preconfigured headers (with anon-key & auth)
                Map<String, String> headers = SupabaseClient.getHeaders();
                // ensure JSON
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    private static String getBaseUrl() {
        return SupabaseClient.getBaseUrl();
    }
}
