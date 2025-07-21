package com.infowave.demo.fragments;

import android.annotation.SuppressLint;
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

import com.infowave.demo.FriendProfileActivity;
import com.infowave.demo.R;
import com.infowave.demo.adapters.PersonNearbyAdapter;
import com.infowave.demo.adapters.RecommendedUserAdapter;
import com.infowave.demo.adapters.UserSearchAdapter;
import com.infowave.demo.models.PersonNearby;
import com.infowave.demo.models.RecommendedUser;
import com.infowave.demo.models.UserSearchResult;
import com.infowave.demo.supabase.SearchRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView rvPeopleNearby, rvRecommended, rvSearchResults;
    private PersonNearbyAdapter nearbyAdapter;
    private RecommendedUserAdapter recommendedAdapter;
    private UserSearchAdapter userSearchAdapter;
    private EditText searchInput;
    private ImageButton clearButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchInput = view.findViewById(R.id.search_input);
        clearButton = view.findViewById(R.id.clear_button);
        rvPeopleNearby = view.findViewById(R.id.rvPeopleNearby);
        rvRecommended = view.findViewById(R.id.rvRecommended);

        // Add this line for the search results RecyclerView (add to your XML if missing)
        rvSearchResults = view.findViewById(R.id.rvSearchResults); // <-- see XML note below

        setupSearch();
        setupAdapters();
        setupRecyclerViews();
        loadDummyData();

        return view;
    }

    private void setupSearch() {
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (clearButton != null) {
                        clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                    }
                    performLiveUserSearch(s.toString().trim());
                }

                @Override
                public void afterTextChanged(Editable s) {}
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
            if (rvPeopleNearby != null) rvPeopleNearby.setVisibility(View.VISIBLE);
            if (rvRecommended != null) rvRecommended.setVisibility(View.VISIBLE);
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
                if (rvPeopleNearby != null) rvPeopleNearby.setVisibility(View.GONE);
                if (rvRecommended != null) rvRecommended.setVisibility(View.GONE);
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
        nearbyAdapter = new PersonNearbyAdapter(new ArrayList<>(), position -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Connect clicked: " + position, Toast.LENGTH_SHORT).show();
            }
        });

        // Recommended adapter
        recommendedAdapter = new RecommendedUserAdapter(new ArrayList<>(), position -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Follow clicked: " + position, Toast.LENGTH_SHORT).show();
            }
        });

        // User search adapter (open profile on click)
        userSearchAdapter = new UserSearchAdapter(new ArrayList<>(), user -> {
            if (getContext() != null && user != null) {
                Intent i = new Intent(getContext(), FriendProfileActivity.class);
//                i.putExtra("userId", user.getId());
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

        // Set up search results RecyclerView (add this block)
        if (rvSearchResults != null) {
            rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSearchResults.setAdapter(userSearchAdapter);
            rvSearchResults.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadDummyData() {
        // Nearby dummy data
        List<PersonNearby> nearbyList = new ArrayList<>();
        nearbyList.add(new PersonNearby("Alex", "1.2 km away", R.drawable.image1));
        nearbyList.add(new PersonNearby("Taylor", "0.8 km away", R.drawable.image2));
        nearbyList.add(new PersonNearby("Jordan", "2.1 km away", R.drawable.image3));
        nearbyList.add(new PersonNearby("Morgan", "1.5 km away", R.drawable.image4));
        if (nearbyAdapter != null) {
            nearbyAdapter.nearbyList = nearbyList;
            nearbyAdapter.notifyDataSetChanged();
        }

        // Recommended dummy data
        List<RecommendedUser> recommendedList = new ArrayList<>();
        recommendedList.add(new RecommendedUser("Alex Johnson", "Travel, Photography", R.drawable.image5));
        recommendedList.add(new RecommendedUser("Sam Wilson", "Music, Hiking", R.drawable.image1));
        recommendedList.add(new RecommendedUser("Casey Lee", "Cooking, Art", R.drawable.image3));
        if (recommendedAdapter != null) {
            recommendedAdapter.recommendedList = recommendedList;
            recommendedAdapter.notifyDataSetChanged();
        }
    }
}
