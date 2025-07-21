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
        Log.d("LocationRepo", "updateLocation called: " + latitude + ", " + longitude);

        String jwt = SupabaseClient.getJwtFromPrefs(context);
        String userId = SupabaseClient.getUserIdFromPrefs(context);

        if (jwt == null || userId == null) {
            Log.e("LocationRepo", "JWT or userId missing, cannot update location.");
            return;
        }

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/users?id=eq." + userId;

        JSONObject body = new JSONObject();
        try {
            body.put("latitude", latitude);
            body.put("longitude", longitude);
        } catch (JSONException e) {
            Log.e("LocationRepo", "JSON Error: " + e.getMessage());
            return;
        }

        // DIAGNOSTIC LOGGING
        Log.d("LocationRepo", "JWT: " + jwt);
        Log.d("LocationRepo", "UserID: " + userId);
        Log.d("LocationRepo", "PATCH URL: " + url);
        Log.d("LocationRepo", "PATCH BODY: " + body.toString());

        StringRequest request = new StringRequest(Request.Method.PATCH, url,
                response -> {
                    Log.i("LocationRepo", "Location updated successfully: " + response);
                },
                error -> {
                    Log.e("LocationRepo", "Failed to update location: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("LocationRepo", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("LocationRepo", "Error Response: " + new String(error.networkResponse.data));
                    }
                }) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = SupabaseClient.getHeaders(context);
                headers.put("Authorization", "Bearer " + jwt);
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                headers.put("Prefer", "return=representation");
                // headers.put("Content-Profile", "public"); // For Supabase REST
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        SupabaseClient.addToRequestQueue(context, request);
    }
}