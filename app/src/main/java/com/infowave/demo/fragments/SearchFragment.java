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
import com.infowave.demo.supabase.FollowRepository;
import com.infowave.demo.supabase.SearchRepository;
import com.infowave.demo.supabase.SupabaseClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchInput        = view.findViewById(R.id.search_input);
        clearButton        = view.findViewById(R.id.clear_button);
        rvPeopleNearby     = view.findViewById(R.id.rvPeopleNearby);
        rvRecommended      = view.findViewById(R.id.rvRecommended);
        rvSearchResults    = view.findViewById(R.id.rvSearchResults);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);

        setupSearch();
        setupAdapters();
        setupRecyclerViews();

        FollowRepository.setOnNeedsRefreshListener(() -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(this::refreshAllData);
        });

        refreshAllData();
        swipeRefreshLayout.setOnRefreshListener(this::refreshAllData);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FollowRepository.setOnNeedsRefreshListener(null);
    }

    private void refreshAllData() {
        performLiveUserSearch(searchInput.getText().toString().trim());
        loadNearbyPeople();
        loadRecommendedUsers();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearButton.setVisibility(s.length()>0? View.VISIBLE: View.GONE);
                performLiveUserSearch(s.toString().trim());
            }
        });
        clearButton.setOnClickListener(v -> searchInput.setText(""));
    }

    private void performLiveUserSearch(String query) {
        if (query.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            rvPeopleNearby.setVisibility(View.VISIBLE);
            rvRecommended.setVisibility(View.VISIBLE);
            return;
        }
        SearchRepository.searchUsers(requireContext(), query, new SearchRepository.SearchCallback() {
            @Override
            public void onResults(List<UserSearchResult> results) {
                userSearchAdapter.setResults(results);
                rvSearchResults.setVisibility(View.VISIBLE);
                rvPeopleNearby.setVisibility(View.GONE);
                rvRecommended.setVisibility(View.GONE);
            }
            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Search error: "+error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdapters() {
        // **यहाँ Context पहले पास करें**
        nearbyAdapter = new PersonNearbyAdapter(
                requireContext(),
                new ArrayList<>(),
                person -> Toast.makeText(requireContext(), "Clicked: "+person.getName(), Toast.LENGTH_SHORT).show()
        );

        recommendedAdapter = new RecommendedUserAdapter(
                requireContext(),
                new ArrayList<>(),
                user -> FollowRepository.sendFollowRequest(requireContext(), user.getId(), new FollowRepository.SimpleCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onError(String err) {
                        Toast.makeText(requireContext(), "Follow error: "+err, Toast.LENGTH_SHORT).show();
                    }
                })
        );

        userSearchAdapter = new UserSearchAdapter(
                requireContext(),
                new ArrayList<>(),
                user -> {
                    Intent i = new Intent(requireContext(), FriendProfileActivity.class);
                    i.putExtra("userId", user.getId());
                    startActivity(i);
                }
        );
    }

    private void setupRecyclerViews() {
        rvPeopleNearby.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvPeopleNearby.setAdapter(nearbyAdapter);

        rvRecommended.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecommended.setAdapter(recommendedAdapter);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(userSearchAdapter);
        rvSearchResults.setVisibility(View.GONE);
    }

    private void loadNearbyPeople() {
        Context ctx = requireContext();
        String userId = ctx.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE)
                .getString("user_id", null);
        if (userId==null) {
            Toast.makeText(ctx, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = SupabaseClient.getBaseUrl()
                + "/rest/v1/users?id=eq." + userId + "&select=latitude,longitude";
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length()==0) {
                        Toast.makeText(ctx, "Please set location.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject o = response.getJSONObject(0);
                        double lat = o.optDouble("latitude",0),
                                lng = o.optDouble("longitude",0);
                        if (lat==0 && lng==0) {
                            Toast.makeText(ctx, "Enable location.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SearchRepository.getPeopleNearby(ctx, lat, lng, 500.0,
                                new SearchRepository.PeopleNearbyCallback() {
                                    @Override
                                    public void onResults(List<PersonNearby> list) {
                                        nearbyAdapter.setNearbyList(list);
                                    }
                                    @Override
                                    public void onError(String err) {
                                        Toast.makeText(ctx, "Nearby failed: "+err, Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    } catch (Exception e) {
                        Toast.makeText(ctx, "Location parse error.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ctx, "Fetch coords error.", Toast.LENGTH_SHORT).show()
        ) {
            @Override public Map<String,String> getHeaders() {
                return SupabaseClient.getHeaders(ctx);
            }
        };
        SupabaseClient.addToRequestQueue(ctx, req);
    }

    private void loadRecommendedUsers() {
        SearchRepository.getRecommendedUsers(requireContext(),
                new SearchRepository.RecommendedUsersCallback() {
                    @Override
                    public void onResults(List<RecommendedUser> users) {
                        recommendedAdapter.setRecommendedList(users);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), "Recomm. failed: "+error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
