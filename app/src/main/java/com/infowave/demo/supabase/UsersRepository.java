// UsersRepository.java
package com.infowave.demo.supabase;

import android.content.Context;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.infowave.demo.models.User;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

public class UsersRepository {

    public interface UserCallback {
        void onSuccess(String userId);
        void onFailure(String error);
    }

    public static void registerUser(Context ctx, User user, UserCallback cb) {
        String url = getBaseUrl()
                + "/rest/v1/users?on_conflict=username";  // ← upsert on username

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

        StringRequest req = new StringRequest(Request.Method.POST, url,
                resp -> {
                    // Supabase returns empty body on success
                    // If you need the new id, you'd fetch it separately.
                    cb.onSuccess("upsert_ok");
                },
                err -> {
                    String e = err.networkResponse!=null
                            ? new String(err.networkResponse.data)
                            : err.toString();
                    cb.onFailure("Error: " + e);
                }
        ) {
            @Override public byte[] getBody() {
                return body.toString().getBytes();
            }
            @Override public String getBodyContentType() {
                return "application/json";
            }
            @Override public Map<String,String> getHeaders() {
                Map<String,String> h = SupabaseClient.getHeaders();
                h.put("Prefer", "resolution=merge-duplicates");  // ← turns insert into upsert
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
