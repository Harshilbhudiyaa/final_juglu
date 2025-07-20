package com.infowave.demo.supabase;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

public class LocationRepository {

    public static void updateLocation(Context context, double latitude, double longitude) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/users?select=id&auth_id=eq." + SupabaseClient.getJwtFromPrefs(context);

        JSONObject body = new JSONObject();
        try {
            body.put("latitude", latitude);
            body.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.PATCH, url,
                response -> Log.d("LocationRepo", "Location updated"),
                error -> Log.e("LocationRepo", "Failed: " + error.toString())) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        SupabaseClient.addToRequestQueue(context, request);
    }
}
