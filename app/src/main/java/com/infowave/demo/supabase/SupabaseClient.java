package com.infowave.demo.supabase;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import java.util.HashMap;
import java.util.Map;

public class SupabaseClient {
    private static final String SUPABASE_URL = "https://rlpfejzenyampilyqzbd.supabase.co"; // <-- your project URL
    private static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJscGZlanplbnlhbXBpbHlxemJkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTAwNjEyNjcsImV4cCI6MjA2NTYzNzI2N30.HOi6OqNqR-oteYyttL9i4GcpHj_ndJ1kPXVMFDxIvXs"; // <-- your anon key

    private static SupabaseClient instance;
    protected RequestQueue requestQueue;

    private SupabaseClient(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context);
        }
        return instance;
    }
    // Generic GET from any table
    public void getTable(String tableName, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String url = SUPABASE_URL + "/rest/v1/" + tableName + "?select=*";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", ANON_KEY);
                headers.put("Authorization", "Bearer " + ANON_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }

    public static String getBaseUrl() {
        return SUPABASE_URL;
    }

    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("apikey", ANON_KEY);
        headers.put("Authorization", "Bearer " + ANON_KEY);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public static void addToRequestQueue(Context context, Request<?> request) {
        getInstance(context).getRequestQueue().add(request);
    }
    public static String getAnonKey() {
        return ANON_KEY;
    }

}

