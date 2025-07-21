package com.infowave.demo.supabase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.infowave.demo.models.PersonNearby;
import com.infowave.demo.models.UserSearchResult;
import com.infowave.demo.models.RecommendedUser;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchRepository {

    // Helper to get current user id from SharedPreferences (same everywhere)
    private static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }

    public interface SearchCallback {
        void onResults(List<UserSearchResult> results);
        void onError(String error);
    }

    // Live user search (username/full_name)
    public static void searchUsers(Context context, String query, SearchCallback callback) {
        Log.d("SEARCH_REPO", "searchUsers called with query: " + query);

        final String currentUserId = getCurrentUserId(context);

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
                            String id = obj.optString("id");
                            if (id != null && id.equals(currentUserId)) continue; // Exclude self
                            results.add(new UserSearchResult(
                                    id,
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

    public interface PeopleNearbyCallback {
        void onResults(List<PersonNearby> people);
        void onError(String error);
    }

    public static void getPeopleNearby(
            Context context,
            double latitude,
            double longitude,
            double radiusKm,
            PeopleNearbyCallback callback
    ) {
        Log.d("PEOPLE_NEARBY_REPO", "getPeopleNearby called with lat: " + latitude + ", lng: " + longitude + ", radius: " + radiusKm);

        final String currentUserId = getCurrentUserId(context);

        double latDelta = radiusKm / 111.0;
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/users"
                + "?latitude=gte." + (latitude - latDelta)
                + "&latitude=lte." + (latitude + latDelta)
                + "&longitude=gte." + (longitude - lonDelta)
                + "&longitude=lte." + (longitude + lonDelta)
                + "&status=eq.active"
                + "&select=id,full_name,latitude,longitude,profile_image";

        Log.d("PEOPLE_NEARBY_REPO", "Supabase Nearby URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("PEOPLE_NEARBY_REPO", "Nearby response: " + response.toString());
                    List<PersonNearby> people = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.optString("id");
                            if (id != null && id.equals(currentUserId)) continue; // Exclude self
                            String name = obj.optString("full_name");
                            double userLat = obj.optDouble("latitude");
                            double userLng = obj.optDouble("longitude");
                            String profileImg = obj.optString("profile_image");
                            double distance = getDistanceKm(latitude, longitude, userLat, userLng);
                            @SuppressLint("DefaultLocale")
                            String distanceStr = String.format("%.1f km away", distance);
                            people.add(new PersonNearby(name, distanceStr, profileImg));
                            Log.d("PEOPLE_NEARBY_REPO", "Parsed: " + name + " - " + distanceStr);
                        } catch (Exception e) {
                            Log.e("PEOPLE_NEARBY_REPO", "Error parsing nearby: " + e.getMessage());
                        }
                    }
                    Log.d("PEOPLE_NEARBY_REPO", "Parsed " + people.size() + " nearby user(s) from results.");
                    callback.onResults(people);
                },
                error -> {
                    String err = (error.networkResponse != null && error.networkResponse.data != null)
                            ? new String(error.networkResponse.data)
                            : error.toString();
                    Log.e("PEOPLE_NEARBY_REPO", "Volley error (nearby): " + err);
                    callback.onError("Network error: " + err);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = SupabaseClient.getHeaders(context);
                Log.d("PEOPLE_NEARBY_REPO", "Request headers: " + headers.toString());
                return headers;
            }
        };

        Log.d("PEOPLE_NEARBY_REPO", "Adding nearby users request to Volley queue.");
        SupabaseClient.addToRequestQueue(context, request);
    }

    private static double getDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    // --- RECOMMENDED USERS SECTION ---

    public interface RecommendedUsersCallback {
        void onResults(List<RecommendedUser> users);
        void onError(String error);
    }

    public static void getRecommendedUsers(Context context, RecommendedUsersCallback callback) {
        final String currentUserId = getCurrentUserId(context);

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/users"
                + "?status=eq.active"
                + "&select=id,full_name,bio,profile_image"
                + "&limit=10";

        Log.d("RECOMMENDED_USERS", "URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<RecommendedUser> users = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.optString("id");
                            if (id != null && id.equals(currentUserId)) continue; // Exclude self
                            users.add(new RecommendedUser(
                                    id,
                                    obj.optString("full_name"),
                                    obj.optString("bio"),
                                    obj.optString("profile_image")
                            ));
                        } catch (Exception e) {
                            Log.e("RECOMMENDED_USERS", "Error parsing: " + e.getMessage());
                        }
                    }
                    // Shuffle the list for random order
                    Collections.shuffle(users);
                    callback.onResults(users);
                },
                error -> {
                    String err = (error.networkResponse != null && error.networkResponse.data != null)
                            ? new String(error.networkResponse.data)
                            : error.toString();
                    Log.e("RECOMMENDED_USERS", "Volley error: " + err);
                    callback.onError("Network error: " + err);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = SupabaseClient.getHeaders(context);
                Log.d("RECOMMENDED_USERS", "Request headers: " + headers.toString());
                return headers;
            }
        };

        Log.d("RECOMMENDED_USERS", "Adding request to Volley queue.");
        SupabaseClient.addToRequestQueue(context, request);
    }
}
