package com.infowave.demo.supabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

public class UsersRepository {

    public interface UserCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }
    public interface AuthLoginCallback {
        void onSuccess(String jwtToken, String userId);
        void onFailure(String error);
    }

    // --------------------- REGISTRATION ------------------------
    public static void registerUserWithAuth(Context ctx, com.infowave.demo.models.User user, String phone, String realEmail, UserCallback cb) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String backendPassword = phone + "_" + timestamp;
        String backendEmail = phone + "_" + timestamp + "@gmail.com";

        String url = SupabaseClient.getBaseUrl() + "/auth/v1/signup";
        JSONObject body = new JSONObject();
        try {
            body.put("email", backendEmail);
            body.put("password", backendPassword);
            body.put("email_confirm", true);  // skip email confirmation
        } catch (JSONException e) {
            cb.onFailure(e.getMessage());
            return;
        }

        Log.d("SUPABASE_AUTH_REGISTER_URL", url);
        Log.d("SUPABASE_AUTH_REGISTER_BODY", body.toString());

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                resp -> {
                    try {
                        String authId = resp.getJSONObject("user").getString("id");
                        Log.d("SUPABASE_AUTH_ID", authId);

                        String jwt = null;
                        if (resp.has("session") && !resp.isNull("session")) {
                            jwt = resp.getJSONObject("session").getString("access_token");
                            Log.d("SUPABASE_JWT_TOKEN", jwt);
                            saveJwtToPrefs(ctx, jwt);

                            // Continue as normal
                            insertUserProfile(ctx, user, authId, realEmail, backendEmail, backendPassword, cb);
                        } else {
                            Log.w("SUPABASE_JWT_TOKEN", "No JWT token found in response. Trying login workaround.");

                            // --- WORKAROUND: Call login endpoint immediately ---
                            String loginUrl = SupabaseClient.getBaseUrl() + "/auth/v1/token?grant_type=password";
                            JSONObject loginBody = new JSONObject();
                            try {
                                loginBody.put("email", backendEmail);
                                loginBody.put("password", backendPassword);
                            } catch (JSONException e) {
                                cb.onFailure(e.getMessage());
                                return;
                            }
                            JsonObjectRequest loginReq = new JsonObjectRequest(Request.Method.POST, loginUrl, loginBody,
                                    loginResp -> {
                                        try {
                                            String jwtLogin = loginResp.getString("access_token");
                                            Log.d("SUPABASE_JWT_TOKEN", jwtLogin);
                                            saveJwtToPrefs(ctx, jwtLogin);
                                            // Continue as normal after JWT obtained via login
                                            insertUserProfile(ctx, user, authId, realEmail, backendEmail, backendPassword, cb);
                                        } catch (Exception e) {
                                            cb.onFailure("Login workaround failed: " + e.getMessage());
                                        }
                                    },
                                    loginErr -> {
                                        String e = loginErr.networkResponse != null ? new String(loginErr.networkResponse.data) : loginErr.toString();
                                        Log.e("SUPABASE_LOGIN_ERROR", e);
                                        cb.onFailure("Login workaround error: " + e);
                                    }
                            ) {
                                @Override public Map<String, String> getHeaders() {
                                    Map<String, String> h = new HashMap<>();
                                    h.put("apikey", SupabaseClient.getAnonKey());
                                    h.put("Authorization", "Bearer " + SupabaseClient.getAnonKey());
                                    h.put("Content-Type", "application/json");
                                    return h;
                                }
                            };
                            loginReq.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
                            SupabaseClient.addToRequestQueue(ctx, loginReq);
                        }
                    } catch (Exception e) {
                        cb.onFailure("Auth success, but parse failed: " + e.getMessage());
                    }
                },
                err -> {
                    String e = err.networkResponse != null ? new String(err.networkResponse.data) : err.toString();
                    Log.e("SUPABASE_AUTH_ERROR", e);
                    cb.onFailure("Auth Error: " + e);
                }
        ) {
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("apikey", SupabaseClient.getAnonKey());
                h.put("Authorization", "Bearer " + SupabaseClient.getAnonKey());
                h.put("Content-Type", "application/json");
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    private static void insertUserProfile(Context ctx, com.infowave.demo.models.User user, String authId,
                                          String realEmail, String backendEmail, String backendPassword,
                                          UserCallback cb) {
        String url = getBaseUrl() + "/rest/v1/users?on_conflict=username";
        JSONObject body = new JSONObject();
        try {
            body.put("auth_id", authId);
            body.put("full_name", user.getFullName());
            body.put("username", user.getUsername());
            body.put("phone", user.getPhone());
            body.put("bio", user.getBio());
            body.put("real_email", realEmail);          // NEW
            body.put("backend_email", backendEmail);    // NEW
            body.put("backend_password", backendPassword); // NEW
            body.put("status", "active");
        } catch (JSONException e) {
            cb.onFailure(e.getMessage());
            return;
        }

        Log.d("USERS_REPO_REGISTER_URL", url);
        Log.d("USERS_REPO_REGISTER_BODY", body.toString());

        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> cb.onSuccess("upsert_ok"),
                err -> {
                    String e = err.networkResponse != null ? new String(err.networkResponse.data) : err.toString();
                    Log.e("USERS_REPO_REGISTER_ERR", e);
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json"; }
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders(ctx);
                h.put("Prefer", "resolution=merge-duplicates");
                Log.d("SUPABASE_HEADERS_REGISTER", h.toString());
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    // --------------------- OTP-BASED LOGIN (JWT ONLY) ------------------------
    public static void loginWithOtp(Context ctx, String phone, String otp, UserCallback cb) {
        String otpUrl = SupabaseClient.getBaseUrl() + "/rest/v1/phone_otps?phone=eq." + phone + "&select=otp";
        Log.d("LOGIN_FLOW", "[OTP] Checking OTP at: " + otpUrl);
        JsonArrayRequest otpReq = new JsonArrayRequest(
                Request.Method.GET, otpUrl, null,
                otpArr -> {
                    Log.d("LOGIN_FLOW", "[OTP] Response: " + otpArr.toString());
                    try {
                        if (otpArr.length() == 0) {
                            Log.e("LOGIN_FLOW", "[OTP] No OTP found for phone " + phone);
                            cb.onFailure("No OTP found. Please request again.");
                            return;
                        }
                        String savedOtp = otpArr.getJSONObject(0).getString("otp");
                        Log.d("LOGIN_FLOW", "[OTP] Received in DB: " + savedOtp + " | User entered: " + otp);
                        if (!savedOtp.equals(otp)) {
                            Log.e("LOGIN_FLOW", "[OTP] Invalid OTP entered.");
                            cb.onFailure("Invalid OTP.");
                            return;
                        }
                        // 2. Fetch backend_email and password from users
                        String userUrl = SupabaseClient.getBaseUrl() + "/rest/v1/users?phone=eq." + phone + "&select=id,backend_email,backend_password";
                        Log.d("LOGIN_FLOW", "[USER] Fetching user at: " + userUrl);
                        JsonArrayRequest userReq = new JsonArrayRequest(
                                Request.Method.GET, userUrl, null,
                                userArr -> {
                                    Log.d("LOGIN_FLOW", "[USER] Response: " + userArr.toString());
                                    try {
                                        if (userArr.length() == 0) {
                                            Log.e("LOGIN_FLOW", "[USER] No user found for phone " + phone);
                                            cb.onFailure("User not found. Please register.");
                                            return;
                                        }
                                        JSONObject obj = userArr.getJSONObject(0);
                                        String userId = obj.getString("id");
                                        String backendEmail = obj.getString("backend_email");
                                        String backendPassword = obj.getString("backend_password");
                                        Log.d("LOGIN_FLOW", "[USER] Got backendEmail: " + backendEmail + ", backendPassword: " + backendPassword);
                                        // 3. Supabase Auth Login
                                        loginWithEmailAndPassword(ctx, backendEmail, backendPassword, new UserCallback() {
                                            @Override
                                            public void onSuccess(String jwt) {
                                                Log.d("LOGIN_FLOW", "[AUTH] Login successful, JWT: " + jwt);
                                                // === SAVE user_id to juglu_prefs ===
                                                SharedPreferences jugluPrefs = ctx.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
                                                jugluPrefs.edit().putString("user_id", userId).apply();
                                                cb.onSuccess(userId);
                                            }
                                            @Override
                                            public void onFailure(String error) {
                                                Log.e("LOGIN_FLOW", "[AUTH] Login error: " + error);
                                                cb.onFailure(error);
                                            }
                                        });
                                    } catch (Exception e) {
                                        Log.e("LOGIN_FLOW", "[USER] JSON parsing error: " + e.getMessage());
                                        cb.onFailure("User fetch error: " + e.getMessage());
                                    }
                                },
                                error -> {
                                    String e = error.networkResponse != null ? new String(error.networkResponse.data) : error.toString();
                                    Log.e("LOGIN_FLOW", "[USER] Lookup error: " + e);
                                    cb.onFailure("User lookup failed: " + e);
                                }
                        ) {
                            @Override
                            public Map<String, String> getHeaders() {
                                return SupabaseClient.getHeaders(ctx);
                            }
                        };
                        SupabaseClient.addToRequestQueue(ctx, userReq);

                    } catch (Exception e) {
                        Log.e("LOGIN_FLOW", "[OTP] Parse error: " + e.getMessage());
                        cb.onFailure("OTP parse error: " + e.getMessage());
                    }
                },
                error -> {
                    String e = error.networkResponse != null ? new String(error.networkResponse.data) : error.toString();
                    Log.e("LOGIN_FLOW", "[OTP] Check error: " + e);
                    cb.onFailure("OTP check error: " + e);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.addToRequestQueue(ctx, otpReq);
    }

    // Actual Supabase Auth email/password login. Saves JWT if success, prints response/errors
    private static void loginWithEmailAndPassword(Context ctx, String backendEmail, String backendPassword, UserCallback cb) {
        String loginUrl = SupabaseClient.getBaseUrl() + "/auth/v1/token?grant_type=password";
        JSONObject loginBody = new JSONObject();
        try {
            loginBody.put("email", backendEmail);
            loginBody.put("password", backendPassword);
        } catch (JSONException e) {
            Log.e("LOGIN_FLOW", "[AUTH] JSON error: " + e.getMessage());
            cb.onFailure(e.getMessage());
            return;
        }
        Log.d("LOGIN_FLOW", "[AUTH] Logging in via: " + loginUrl + " | Body: " + loginBody);

        JsonObjectRequest loginReq = new JsonObjectRequest(Request.Method.POST, loginUrl, loginBody,
                loginResp -> {
                    Log.d("LOGIN_FLOW", "[AUTH] Response: " + loginResp.toString());
                    try {
                        String jwt = loginResp.getString("access_token");
                        Log.d("LOGIN_FLOW", "[AUTH] JWT: " + jwt);
                        saveJwtToPrefs(ctx, jwt);
                        cb.onSuccess(jwt);
                    } catch (Exception e) {
                        Log.e("LOGIN_FLOW", "[AUTH] Parse error: " + e.getMessage());
                        cb.onFailure("Login parse failed: " + e.getMessage());
                    }
                },
                loginErr -> {
                    String e = loginErr.networkResponse != null ? new String(loginErr.networkResponse.data) : loginErr.toString();
                    Log.e("LOGIN_FLOW", "[AUTH] Login failed: " + e);
                    cb.onFailure("Login failed: " + e);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("apikey", SupabaseClient.getAnonKey());
                h.put("Authorization", "Bearer " + SupabaseClient.getAnonKey());
                h.put("Content-Type", "application/json");
                return h;
            }
        };
        SupabaseClient.addToRequestQueue(ctx, loginReq);
    }

    // --------------------- USER UTILITIES ------------------------
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
                    String e = err.networkResponse != null ? new String(err.networkResponse.data) : err.toString();
                    Log.e("USERS_REPO_FETCHID_ERR", e);
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders(ctx);
                Log.d("SUPABASE_HEADERS_FETCHID", h.toString());
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

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
                    String e = err.networkResponse != null ? new String(err.networkResponse.data) : err.toString();
                    Log.e("USERS_REPO_GENDER_ERR", e);
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public byte[] getBody() { return body.toString().getBytes(); }
            @Override public String getBodyContentType() { return "application/json"; }
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders(ctx);
                Log.d("SUPABASE_HEADERS_GENDER", h.toString());
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    public static void updateProfileImage(Context ctx, String userId, String profileImageUrl, UserCallback cb) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/users?id=eq." + userId;
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
                resp -> {
                    Log.d("USERS_REPO_PROFILEIMG_RESP", resp);
                    cb.onSuccess("profile_image_updated");
                },
                err -> {
                    String e = err.networkResponse != null && err.networkResponse.data != null
                            ? new String(err.networkResponse.data)
                            : err.toString();
                    Log.e("USERS_REPO_PROFILEIMG_ERR", e);
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders(ctx);
                Log.d("SUPABASE_HEADERS_PROFILEIMG", h.toString());
                return h;
            }
        };
        req.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(7000, 1, 1f));
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    private static String getBaseUrl() {
        return SupabaseClient.getBaseUrl();
    }

    // ===== Helper to save JWT securely in SharedPreferences =====
    private static void saveJwtToPrefs(Context ctx, String jwt) {
        SharedPreferences prefs = ctx.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("jwt_token", jwt).apply();
        Log.d("LOGIN_FLOW", "[AUTH] JWT token saved in SharedPreferences.");
    }
}
