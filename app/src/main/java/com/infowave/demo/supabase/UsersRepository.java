package com.infowave.demo.supabase;

import android.content.Context;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;


import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

public class UsersRepository {

    public interface UserCallback {
        void onSuccess(String result); // userId or message
        void onFailure(String error);
    }

    // 1️⃣ Register new user (without gender/profile_image)
    public static void registerUser(Context ctx, com.infowave.demo.models.User user, UserCallback cb) {
        String url = getBaseUrl() + "/rest/v1/users?on_conflict=username";
        JSONObject body = new JSONObject();
        try {
            body.put("full_name", user.getFullName());
            body.put("username",  user.getUsername());
            body.put("phone",     user.getPhone());
            body.put("password",  user.getPassword());
            body.put("bio",       user.getBio());
            body.put("status",    "active");
        } catch (JSONException e) {
            cb.onFailure(e.getMessage());
            return;
        }
        Log.d("USERS_REPO_REGISTER_URL", url);
        Log.d("USERS_REPO_REGISTER_BODY", body.toString());

        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> cb.onSuccess("upsert_ok"),
                err -> {
                    String e = err.networkResponse != null
                            ? new String(err.networkResponse.data)
                            : err.toString();
                    Log.e("USERS_REPO_REGISTER_ERR", e);
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json"; }
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders();
                h.put("Prefer", "resolution=merge-duplicates");
                Log.d("SUPABASE_HEADERS_REGISTER", h.toString());
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    // 2️⃣ Fetch userId by phone (after registration)
    public static void fetchUserIdByPhone(Context ctx, String phone, UserCallback cb) {
        String url = getBaseUrl() + "/rest/v1/users?phone=eq." + phone + "&select=id";
        Log.d("USERS_REPO_FETCHID_URL", url);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject userObj = response.getJSONObject(0);
                            String userId = userObj.getString("id");
                            cb.onSuccess(userId);
                        } else {
                            cb.onFailure("No user found with this phone.");
                        }
                    } catch (JSONException e) {
                        cb.onFailure(e.getMessage());
                    }
                },
                err -> {
                    String e = err.networkResponse != null
                            ? new String(err.networkResponse.data)
                            : err.toString();
                    Log.e("USERS_REPO_FETCHID_ERR", e);
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders();
                Log.d("SUPABASE_HEADERS_FETCHID", h.toString());
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    // 3️⃣ Update Gender for user (by userId)
    public static void updateGender(Context ctx, String userId, String gender, UserCallback cb) {
        String url = getBaseUrl() + "/rest/v1/users?id=eq." + userId;
        JSONObject body = new JSONObject();
        try {
            body.put("gender", gender);
        } catch (JSONException e) {
            cb.onFailure(e.getMessage());
            return;
        }
        Log.d("USERS_REPO_GENDER_URL", url);
        Log.d("USERS_REPO_GENDER_BODY", body.toString());

        StringRequest req = new StringRequest(Request.Method.PATCH, url,
                resp -> cb.onSuccess("gender_updated"),
                err -> {
                    String e = err.networkResponse != null
                            ? new String(err.networkResponse.data)
                            : err.toString();
                    Log.e("USERS_REPO_GENDER_ERR", e);
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json"; }
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders();
                Log.d("SUPABASE_HEADERS_GENDER", h.toString());
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    // 4️⃣ Update Profile Photo for user (by userId)
    public static void updateProfileImage(Context ctx, String userId, String profileImageUrl, UserCallback cb) {
        String url = getBaseUrl() + "/rest/v1/users?id=eq." + userId;
        JSONObject body = new JSONObject();
        try {
            body.put("profile_image", profileImageUrl);
        } catch (JSONException e) {
            cb.onFailure(e.getMessage());
            return;
        }
        Log.d("USERS_REPO_PROFILEIMG_URL", url);
        Log.d("USERS_REPO_PROFILEIMG_BODY", body.toString());

        StringRequest req = new StringRequest(Request.Method.PATCH, url,
                resp -> cb.onSuccess("profile_image_updated"),
                err -> {
                    String e = err.networkResponse != null
                            ? new String(err.networkResponse.data)
                            : err.toString();
                    Log.e("USERS_REPO_PROFILEIMG_ERR", e);
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json"; }
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders();
                Log.d("SUPABASE_HEADERS_PROFILEIMG", h.toString());
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    private static String getBaseUrl() {
        return SupabaseClient.getBaseUrl();
    }
}
