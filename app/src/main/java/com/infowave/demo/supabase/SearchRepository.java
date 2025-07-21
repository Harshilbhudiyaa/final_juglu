package com.infowave.demo.supabase;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.infowave.demo.models.UserSearchResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchRepository {

    public interface SearchCallback {
        void onResults(List<UserSearchResult> results);
        void onError(String error);
    }

    // Live user search (username/full_name)
    public static void searchUsers(Context context, String query, SearchCallback callback) {
        Log.d("SEARCH_REPO", "searchUsers called with query: " + query);

        if (query == null || query.trim().isEmpty()) {
            Log.d("SEARCH_REPO", "Query is empty, returning empty results.");
            callback.onResults(new ArrayList<>());
            return;
        }

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/users"
                + "?or=(username.ilike.*" + query + "*,full_name.ilike.*" + query + "*)"
                + "&status=eq.active"
                + "&select=id,full_name,username,profile_image,bio";

        Log.d("SEARCH_REPO", "Supabase Search URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("SEARCH_REPO", "Search response: " + response.toString());
                    List<UserSearchResult> results = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            Log.d("SEARCH_REPO", "Parsing user at index " + i + ": " + obj.toString());
                            results.add(new UserSearchResult(
                                    obj.optString("id"),
                                    obj.optString("full_name"),
                                    obj.optString("username"),
                                    obj.optString("profile_image"),
                                    obj.optString("bio")
                            ));
                        } catch (Exception e) {
                            Log.e("SEARCH_REPO", "Error parsing user at index " + i + ": " + e.getMessage());
                        }
                    }
                    Log.d("SEARCH_REPO", "Parsed " + results.size() + " user(s) from results.");
                    callback.onResults(results);
                },
                error -> {
                    String err = (error.networkResponse != null && error.networkResponse.data != null)
                            ? new String(error.networkResponse.data)
                            : error.toString();
                    Log.e("SEARCH_REPO", "Volley error: " + err);
                    callback.onError("Network error: " + err);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = SupabaseClient.getHeaders(context);
                Log.d("SEARCH_REPO", "Request headers: " + headers.toString());
                return headers;
            }
        };

        Log.d("SEARCH_REPO", "Adding request to Volley queue.");
        SupabaseClient.addToRequestQueue(context, request);
    }
}
