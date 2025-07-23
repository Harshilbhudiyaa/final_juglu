package com.infowave.demo.supabase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.infowave.demo.models.FollowState;
import com.infowave.demo.models.PersonNearby;
import com.infowave.demo.models.RecommendedUser;
import com.infowave.demo.models.UserSearchResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * SearchRepository:
 *  - Live search (users)
 *  - People nearby
 *  - Recommended users
 *
 * अब इसमें follow status को bulk में fetch करके
 * models में set किया जा रहा है।
 */
public class SearchRepository {

    private static final String TAG = "SEARCH_REPO";

    // Helper to get current user id from SharedPreferences (same everywhere)
    private static String getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }

    // -------------------- Interfaces --------------------
    public interface SearchCallback {
        void onResults(List<UserSearchResult> results);
        void onError(String error);
    }

    public interface PeopleNearbyCallback {
        void onResults(List<PersonNearby> people);
        void onError(String error);
    }

    public interface RecommendedUsersCallback {
        void onResults(List<RecommendedUser> users);
        void onError(String error);
    }

    // -------------------- PUBLIC APIs --------------------

    // Live user search (username/full_name)
    public static void searchUsers(Context context, String query, SearchCallback callback) {
        Log.d(TAG, "searchUsers called with query: " + query);

        final String currentUserId = getCurrentUserId(context);

        if (query == null || query.trim().isEmpty()) {
            Log.d(TAG, "Query is empty, returning empty results.");
            callback.onResults(new ArrayList<>());
            return;
        }

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/users"
                + "?or=(username.ilike.*" + query + "*,full_name.ilike.*" + query + "*)"
                + "&status=eq.active"
                + "&select=id,full_name,username,profile_image,bio";

        Log.d(TAG, "Supabase Search URL: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "Search response: " + response);
                    List<UserSearchResult> results = new ArrayList<>();
                    List<String> otherIds = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.optString("id");
                            if (id != null && id.equals(currentUserId)) continue; // Exclude self

                            UserSearchResult usr = new UserSearchResult(
                                    id,
                                    obj.optString("full_name"),
                                    obj.optString("username"),
                                    obj.optString("profile_image"),
                                    obj.optString("bio")
                            );
                            results.add(usr);
                            otherIds.add(id);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user at index " + i + ": " + e.getMessage());
                        }
                    }
                    Log.d(TAG, "Parsed " + results.size() + " user(s). Now fetch follow statuses...");

                    // Fetch bulk follow statuses and then callback
                    applyFollowStates(context, otherIds, results, null, null, callback, null, null);
                },
                error -> {
                    String err = (error.networkResponse != null && error.networkResponse.data != null)
                            ? new String(error.networkResponse.data)
                            : error.toString();
                    Log.e(TAG, "Volley error: " + err);
                    callback.onError("Network error: " + err);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = SupabaseClient.getHeaders(context);
                Log.d(TAG, "Request headers: " + headers);
                return headers;
            }
        };

        Log.d(TAG, "Adding search request to Volley queue.");
        SupabaseClient.addToRequestQueue(context, request);
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
                    Log.d("PEOPLE_NEARBY_REPO", "Nearby response: " + response);
                    List<PersonNearby> people = new ArrayList<>();
                    List<String> otherIds = new ArrayList<>();

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

                            PersonNearby pn = new PersonNearby(id, name, distanceStr, profileImg);
                            people.add(pn);
                            otherIds.add(id);

                            Log.d("PEOPLE_NEARBY_REPO", "Parsed: " + name + " - " + distanceStr);
                        } catch (Exception e) {
                            Log.e("PEOPLE_NEARBY_REPO", "Error parsing nearby: " + e.getMessage());
                        }
                    }
                    Log.d("PEOPLE_NEARBY_REPO", "Parsed " + people.size() + " nearby user(s). Fetch follow states...");

                    applyFollowStates(context, otherIds, null, people, null, null, callback, null);
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
                Log.d("PEOPLE_NEARBY_REPO", "Request headers: " + headers);
                return headers;
            }
        };

        Log.d("PEOPLE_NEARBY_REPO", "Adding nearby users request to Volley queue.");
        SupabaseClient.addToRequestQueue(context, request);
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
                    List<String> otherIds = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.optString("id");
                            if (id != null && id.equals(currentUserId)) continue; // Exclude self
                            RecommendedUser ru = new RecommendedUser(
                                    id,
                                    obj.optString("full_name"),
                                    obj.optString("bio"),
                                    obj.optString("profile_image")
                            );
                            users.add(ru);
                            otherIds.add(id);
                        } catch (Exception e) {
                            Log.e("RECOMMENDED_USERS", "Error parsing: " + e.getMessage());
                        }
                    }
                    // Shuffle the list for random order
                    Collections.shuffle(users);
                    Log.d("RECOMMENDED_USERS", "Parsed " + users.size() + " users. Fetch follow states...");

                    applyFollowStates(context, otherIds, null, null, users, null, null, callback);
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
                Log.d("RECOMMENDED_USERS", "Request headers: " + headers);
                return headers;
            }
        };

        Log.d("RECOMMENDED_USERS", "Adding request to Volley queue.");
        SupabaseClient.addToRequestQueue(context, request);
    }

    // -------------------- PRIVATE HELPERS --------------------

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

    /**
     * सभी lists के लिए follow status एक ही जगह apply.
     * कोई list null हो सकती है, दूसरी non-null.
     */
    private static void applyFollowStates(
            Context ctx,
            List<String> otherIds,
            List<UserSearchResult> searchList,
            List<PersonNearby> nearbyList,
            List<RecommendedUser> recList,
            SearchCallback searchCb,
            PeopleNearbyCallback nearbyCb,
            RecommendedUsersCallback recCb
    ) {
        if (otherIds == null || otherIds.isEmpty()) {
            // कोई IDs नहीं हैं, direct callback
            if (searchCb != null && searchList != null) searchCb.onResults(searchList);
            if (nearbyCb != null && nearbyList != null) nearbyCb.onResults(nearbyList);
            if (recCb != null && recList != null) recCb.onResults(recList);
            return;
        }

        FollowRepository.fetchBulkStatuses(ctx, otherIds, new FollowRepository.BulkStatusCallback() {
            @Override
            public void onSuccess(java.util.Map<String, JSONObject> map) {
                String me = getCurrentUserId(ctx);

                // USER SEARCH
                if (searchList != null) {
                    for (UserSearchResult u : searchList) {
                        fillFollowFieldsForUser(me, map.get(u.getId()), u);
                    }
                }

                // NEARBY
                if (nearbyList != null) {
                    for (PersonNearby p : nearbyList) {
                        fillFollowFieldsForNearby(me, map.get(p.getId()), p);
                    }
                }

                // RECOMMENDED
                if (recList != null) {
                    for (RecommendedUser r : recList) {
                        fillFollowFieldsForRecommended(me, map.get(r.getId()), r);
                    }
                }

                // Fire original callbacks
                if (searchCb != null && searchList != null) searchCb.onResults(searchList);
                if (nearbyCb != null && nearbyList != null) nearbyCb.onResults(nearbyList);
                if (recCb != null && recList != null) recCb.onResults(recList);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Bulk status fetch failed: " + error);
                // Even on error, return list with default NONE states (fallback)
                if (searchCb != null && searchList != null) searchCb.onResults(searchList);
                if (nearbyCb != null && nearbyList != null) nearbyCb.onResults(nearbyList);
                if (recCb != null && recList != null) recCb.onResults(recList);
            }
        });
    }

    // Determine & fill follow state for different models
    private static void fillFollowFieldsForUser(String me, JSONObject obj, UserSearchResult u) {
        if (obj == null) {
            u.setFollowState(FollowState.NONE);
            return;
        }
        String status = obj.optString("status", "pending");
        String fId = obj.optString("id");
        String u1 = obj.optString("user_one");
        String u2 = obj.optString("user_two");

        u.setFriendshipRowId(fId);
        u.setRequesterId(u1);

        if ("accepted".equalsIgnoreCase(status)) {
            u.setFollowState(FollowState.FOLLOWING);
        } else { // pending
            if (me.equals(u1)) {
                u.setFollowState(FollowState.REQUEST_SENT);
            } else {
                u.setFollowState(FollowState.REQUEST_RECEIVED);
            }
        }
    }

    private static void fillFollowFieldsForNearby(String me, JSONObject obj, PersonNearby p) {
        if (obj == null) {
            p.setFollowState(FollowState.NONE);
            return;
        }
        String status = obj.optString("status", "pending");
        String fId = obj.optString("id");
        String u1 = obj.optString("user_one");
        String u2 = obj.optString("user_two");

        p.setFriendshipRowId(fId);
        p.setRequesterId(u1);

        if ("accepted".equalsIgnoreCase(status)) {
            p.setFollowState(FollowState.FOLLOWING);
        } else {
            if (me.equals(u1)) {
                p.setFollowState(FollowState.REQUEST_SENT);
            } else {
                p.setFollowState(FollowState.REQUEST_RECEIVED);
            }
        }
    }

    private static void fillFollowFieldsForRecommended(String me, JSONObject obj, RecommendedUser r) {
        if (obj == null) {
            r.setFollowState(FollowState.NONE);
            return;
        }
        String status = obj.optString("status", "pending");
        String fId = obj.optString("id");
        String u1 = obj.optString("user_one");
        String u2 = obj.optString("user_two");

        r.setFriendshipRowId(fId);
        r.setRequesterId(u1);

        if ("accepted".equalsIgnoreCase(status)) {
            r.setFollowState(FollowState.FOLLOWING);
        } else {
            if (me.equals(u1)) {
                r.setFollowState(FollowState.REQUEST_SENT);
            } else {
                r.setFollowState(FollowState.REQUEST_RECEIVED);
            }
        }
    }
}
