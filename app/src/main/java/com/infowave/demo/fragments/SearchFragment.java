package com.infowave.demo.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.infowave.demo.FriendProfileActivity;
import com.infowave.demo.R;
import com.infowave.demo.adapters.PersonNearbyAdapter;
import com.infowave.demo.adapters.RecommendedUserAdapter;
import com.infowave.demo.adapters.UserSearchAdapter;
import com.infowave.demo.models.PersonNearby;
import com.infowave.demo.models.RecommendedUser;
import com.infowave.demo.models.UserSearchResult;
import com.infowave.demo.supabase.SearchRepository;
import com.infowave.demo.supabase.SupabaseClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView rvPeopleNearby, rvRecommended, rvSearchResults;
    private PersonNearbyAdapter nearbyAdapter;
    private RecommendedUserAdapter recommendedAdapter;
    private UserSearchAdapter userSearchAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchInput;
    private ImageButton clearButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchInput        = view.findViewById(R.id.search_input);
        clearButton        = view.findViewById(R.id.clear_button);
        rvPeopleNearby     = view.findViewById(R.id.rvPeopleNearby);
        rvRecommended      = view.findViewById(R.id.rvRecommended);
        rvSearchResults    = view.findViewById(R.id.rvSearchResults);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);

        setupSearch();
        setupAdapters();
        setupRecyclerViews();

        loadNearbyPeople();      // Dynamic nearby users
        loadRecommendedUsers();  // Dynamic recommended users

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshAllData);
        }
        return view;
    }

    private void refreshAllData() {
        performLiveUserSearch(searchInput != null ? searchInput.getText().toString().trim() : "");
        loadNearbyPeople();
        loadRecommendedUsers();
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
    }

    private void setupSearch() {
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (clearButton != null) {
                        clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                    performLiveUserSearch(s.toString().trim());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (clearButton != null && searchInput != null) {
            clearButton.setOnClickListener(v -> searchInput.setText(""));
        }
    }

    private void performLiveUserSearch(String query) {
        Log.d("SEARCH_FRAGMENT", "Performing live search for: " + query);

        if (query.isEmpty()) {
            // Hide search results, show normal content
            if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
            if (rvPeopleNearby  != null) rvPeopleNearby.setVisibility(View.VISIBLE);
            if (rvRecommended   != null) rvRecommended.setVisibility(View.VISIBLE);
            return;
        }

        if (getContext() == null) return;

        SearchRepository.searchUsers(getContext(), query, new SearchRepository.SearchCallback() {
            @Override
            public void onResults(List<UserSearchResult> results) {
                Log.d("SEARCH_FRAGMENT", "Search results: " + results.size());
                if (rvSearchResults != null && userSearchAdapter != null) {
                    userSearchAdapter.setResults(results);
                    rvSearchResults.setVisibility(View.VISIBLE);
                }
                if (rvPeopleNearby  != null) rvPeopleNearby.setVisibility(View.GONE);
                if (rvRecommended   != null) rvRecommended.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                Log.e("SEARCH_FRAGMENT", "Search error: " + error);
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdapters() {
        // Nearby adapter
        nearbyAdapter = new PersonNearbyAdapter(getContext(), new ArrayList<>(), person -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Clicked: " + person.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Recommended adapter
        recommendedAdapter = new RecommendedUserAdapter(getContext(), new ArrayList<>(), user -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Open profile: " + user.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // Search results adapter
        userSearchAdapter = new UserSearchAdapter(getContext(), new ArrayList<>(), user -> {
            if (getContext() != null && user != null) {
                Intent i = new Intent(getContext(), FriendProfileActivity.class);
                i.putExtra("userId", user.getId());
                startActivity(i);
            }
        });
    }

    private void setupRecyclerViews() {
        if (rvPeopleNearby != null) {
            rvPeopleNearby.setLayoutManager(new LinearLayoutManager(
                    getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
            ));
            rvPeopleNearby.setAdapter(nearbyAdapter);
        }

        if (rvRecommended != null) {
            rvRecommended.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecommended.setAdapter(recommendedAdapter);
        }

        if (rvSearchResults != null) {
            rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSearchResults.setAdapter(userSearchAdapter);
            rvSearchResults.setVisibility(View.GONE);
        }
    }

    // Fetch nearby people dynamically from Supabase
    private void loadNearbyPeople() {
        if (getContext() == null) return;
        Log.d("SEARCH_FRAGMENT", "Loading current user location...");

        // Fetch current user's lat/lng first
        String userId = getCurrentUserId(getContext());
        if (userId == null || userId.isEmpty()) {
            Log.e("SEARCH_FRAGMENT", "User ID not found in SharedPreferences.");
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/users?id=eq." + userId
                + "&select=latitude,longitude";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("SEARCH_FRAGMENT", "User location response: " + response);
                    if (response.length() == 0) {
                        Log.e("SEARCH_FRAGMENT", "User location not found in DB.");
                        Toast.makeText(getContext(), "Location not set. Please update your profile.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject obj = response.getJSONObject(0);
                        double latitude = obj.optDouble("latitude", 0.0);
                        double longitude = obj.optDouble("longitude", 0.0);

                        if (latitude == 0.0 && longitude == 0.0) {
                            Toast.makeText(getContext(), "Please enable location or update profile.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d("SEARCH_FRAGMENT", "Calling getPeopleNearby with lat=" + latitude + " lng=" + longitude);

                        // Call repository function to load nearby users
                        SearchRepository.getPeopleNearby(
                                getContext(),
                                latitude,
                                longitude,
                                500.0, // radius in km
                                new SearchRepository.PeopleNearbyCallback() {
                                    @Override
                                    public void onResults(List<PersonNearby> people) {
                                        Log.d("SEARCH_FRAGMENT", "Loaded " + people.size() + " nearby people.");
                                        if (nearbyAdapter != null) {
                                            nearbyAdapter.setNearbyList(people);
                                        }
                                    }
                                    @Override
                                    public void onError(String error) {
                                        Log.e("SEARCH_FRAGMENT", "Failed to load nearby people: " + error);
                                        Toast.makeText(getContext(), "Error loading nearby people: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    } catch (Exception e) {
                        Log.e("SEARCH_FRAGMENT", "Error parsing user location: " + e.getMessage());
                        Toast.makeText(getContext(), "Error loading location.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("SEARCH_FRAGMENT", "Error fetching user location: " + error);
                    Toast.makeText(getContext(), "Error fetching user location", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(getContext());
            }
        };

        SupabaseClient.addToRequestQueue(getContext(), request);
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    // Helper: fetch userId from SharedPreferences
    private String getCurrentUserId(Context context) {
        return context.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE)
                .getString("user_id", null);
    }

    // Fetch "Recommended for you" users dynamically from Supabase
    private void loadRecommendedUsers() {
        if (getContext() == null) return;
        Log.d("SEARCH_FRAGMENT", "Loading recommended users...");

        SearchRepository.getRecommendedUsers(getContext(), new SearchRepository.RecommendedUsersCallback() {
            @Override
            public void onResults(List<RecommendedUser> users) {
                Log.d("SEARCH_FRAGMENT", "Loaded " + users.size() + " recommended users.");
                if (recommendedAdapter != null) {
                    recommendedAdapter.setRecommendedList(users);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("SEARCH_FRAGMENT", "Failed to load recommended users: " + error);
                Toast.makeText(getContext(), "Error loading recommended users: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }
}
