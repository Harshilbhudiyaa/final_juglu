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
import java.util.List;

/**
 * SearchRepository:
 *  - Live search (users)
 *  - People nearby (filtered to 30 km, sorted nearest first)
 *  - Recommended users (excluding those in nearby)
 *
 * अब इसमें follow status को bulk में fetch करके
 * models में set किया जा रहा है।
 */
public class SearchRepository {

    private static final String TAG = "SEARCH_REPO";

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

    // -------------------- Live search --------------------

    public static void searchUsers(Context context, String query, SearchCallback callback) {
        Log.d(TAG, "searchUsers called with query: " + query);
        final String me = getCurrentUserId(context);

        if (query == null || query.trim().isEmpty()) {
            callback.onResults(new ArrayList<>());
            return;
        }

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/users"
                + "?or=(username.ilike.*" + query + "*,full_name.ilike.*" + query + "*)"
                + "&status=eq.active"
                + "&select=id,full_name,username,profile_image,bio";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<UserSearchResult> results = new ArrayList<>();
                    List<String> otherIds = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            String id = o.optString("id");
                            if (id.equals(me)) continue;
                            UserSearchResult u = new UserSearchResult(
                                    id,
                                    o.optString("full_name"),
                                    o.optString("username"),
                                    o.optString("profile_image"),
                                    o.optString("bio")
                            );
                            results.add(u);
                            otherIds.add(id);
                        } catch (Exception e) {
                            Log.e(TAG, "parse user error: " + e.getMessage());
                        }
                    }
                    applyFollowStates(context, otherIds, results, null, null, callback, null, null);
                },
                error -> callback.onError("Network error: " +
                        (error.networkResponse != null && error.networkResponse.data != null
                                ? new String(error.networkResponse.data)
                                : error.toString()))
        ) {
            @Override public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };

        SupabaseClient.addToRequestQueue(context, req);
    }

    // -------------------- People Nearby --------------------

    public static void getPeopleNearby(
            Context context,
            double latitude,
            double longitude,
            double radiusKm,
            PeopleNearbyCallback callback
    ) {
        Log.d(TAG, "getPeopleNearby: lat=" + latitude + ", lon=" + longitude + ", r=" + radiusKm);
        final String me = getCurrentUserId(context);

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

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<PersonNearby> list = new ArrayList<>();
                    List<String> otherIds = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            String id = o.optString("id");
                            if (id.equals(me)) continue;
                            double uLat = o.optDouble("latitude");
                            double uLon = o.optDouble("longitude");
                            double dist = getDistanceKm(latitude, longitude, uLat, uLon);
                            if (dist > radiusKm) continue;  // фильтр по 30 km
                            @SuppressLint("DefaultLocale")
                            String distStr = String.format("%.1f km away", dist);
                            list.add(new PersonNearby(id, o.optString("full_name"), distStr, o.optString("profile_image")));
                            otherIds.add(id);
                        } catch (Exception e) {
                            Log.e(TAG, "parse nearby error: " + e.getMessage());
                        }
                    }
                    // сортировка ближайших первыми
                    Collections.sort(list, (a, b) ->
                            Double.compare(parseDistance(a.getDistance()), parseDistance(b.getDistance()))
                    );
                    applyFollowStates(context, otherIds, null, list, null, null, callback, null);
                },
                error -> callback.onError("Network error: " +
                        (error.networkResponse != null && error.networkResponse.data != null
                                ? new String(error.networkResponse.data)
                                : error.toString()))
        ) {
            @Override public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };

        SupabaseClient.addToRequestQueue(context, req);
    }

    // -------------------- Recommended --------------------

    public static void getRecommendedUsers(Context context, RecommendedUsersCallback callback) {
        final String me = getCurrentUserId(context);
        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/users"
                + "?status=eq.active"
                + "&select=id,full_name,bio,profile_image"
                + "&limit=10";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<RecommendedUser> users = new ArrayList<>();
                    List<String> otherIds = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject o = response.getJSONObject(i);
                            String id = o.optString("id");
                            if (id.equals(me)) continue;
                            users.add(new RecommendedUser(
                                    id,
                                    o.optString("full_name"),
                                    o.optString("bio"),
                                    o.optString("profile_image")
                            ));
                            otherIds.add(id);
                        } catch (Exception e) {
                            Log.e(TAG, "parse rec error: " + e.getMessage());
                        }
                    }
                    Collections.shuffle(users);
                    applyFollowStates(context, otherIds, null, null, users, null, null, callback);
                },
                error -> callback.onError("Network error: " +
                        (error.networkResponse != null && error.networkResponse.data != null
                                ? new String(error.networkResponse.data)
                                : error.toString()))
        ) {
            @Override public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(context);
            }
        };

        SupabaseClient.addToRequestQueue(context, req);
    }

    // -------------------- Helpers --------------------

    private static double getDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // "12.3 km away" → 12.3
    private static double parseDistance(String s) {
        try {
            return Double.parseDouble(s.split(" ")[0]);
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    private static void applyFollowStates(
            Context ctx,
            List<String> ids,
            List<UserSearchResult> sl,
            List<PersonNearby> nl,
            List<RecommendedUser> rl,
            SearchCallback scb,
            PeopleNearbyCallback ncb,
            RecommendedUsersCallback rcb
    ) {
        if (ids == null || ids.isEmpty()) {
            if (scb != null && sl != null) scb.onResults(sl);
            if (ncb != null && nl != null) ncb.onResults(nl);
            if (rcb != null && rl != null) rcb.onResults(rl);
            return;
        }
        FollowRepository.fetchBulkStatuses(ctx, ids, new FollowRepository.BulkStatusCallback() {
            @Override
            public void onSuccess(java.util.Map<String, JSONObject> map) {
                String me = getCurrentUserId(ctx);
                if (sl != null) for (UserSearchResult u : sl) fillFollowForUser(me, map.get(u.getId()), u);
                if (nl != null) for (PersonNearby p : nl) fillFollowForNearby(me, map.get(p.getId()), p);
                if (rl != null) for (RecommendedUser r : rl) fillFollowForRec(me, map.get(r.getId()), r);
                if (scb != null && sl != null) scb.onResults(sl);
                if (ncb != null && nl != null) ncb.onResults(nl);
                if (rcb != null && rl != null) rcb.onResults(rl);
            }
            @Override
            public void onError(String err) {
                Log.e(TAG, "bulk status failed: "+err);
                if (scb != null && sl != null) scb.onResults(sl);
                if (ncb != null && nl != null) ncb.onResults(nl);
                if (rcb != null && rl != null) rcb.onResults(rl);
            }
        });
    }

    private static void fillFollowForUser(String me, JSONObject o, UserSearchResult u) {
        if (o == null) { u.setFollowState(FollowState.NONE); return; }
        String status = o.optString("status","pending"), f=o.optString("id"), u1=o.optString("user_one");
        u.setFriendshipRowId(f);
        u.setRequesterId(u1);
        if ("accepted".equalsIgnoreCase(status)) u.setFollowState(FollowState.FOLLOWING);
        else if (me.equals(u1)) u.setFollowState(FollowState.REQUEST_SENT);
        else u.setFollowState(FollowState.REQUEST_RECEIVED);
    }
    private static void fillFollowForNearby(String me, JSONObject o, PersonNearby p) {
        if (o == null) { p.setFollowState(FollowState.NONE); return; }
        String status = o.optString("status","pending"), f=o.optString("id"), u1=o.optString("user_one");
        p.setFriendshipRowId(f);
        p.setRequesterId(u1);
        if ("accepted".equalsIgnoreCase(status)) p.setFollowState(FollowState.FOLLOWING);
        else if (me.equals(u1)) p.setFollowState(FollowState.REQUEST_SENT);
        else p.setFollowState(FollowState.REQUEST_RECEIVED);
    }
    private static void fillFollowForRec(String me, JSONObject o, RecommendedUser r) {
        if (o == null) { r.setFollowState(FollowState.NONE); return; }
        String status = o.optString("status","pending"), f=o.optString("id"), u1=o.optString("user_one");
        r.setFriendshipRowId(f);
        r.setRequesterId(u1);
        if ("accepted".equalsIgnoreCase(status)) r.setFollowState(FollowState.FOLLOWING);
        else if (me.equals(u1)) r.setFollowState(FollowState.REQUEST_SENT);
        else r.setFollowState(FollowState.REQUEST_RECEIVED);
    }
}
