package com.infowave.demo.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.infowave.demo.R;
import com.infowave.demo.adapters.PersonNearbyAdapter;
import com.infowave.demo.adapters.RecommendedUserAdapter;
import com.infowave.demo.models.PersonNearby;
import com.infowave.demo.models.RecommendedUser;

import java.util.ArrayList;
import java.util.List;

// ChatsFragment.java
public class ChatsFragment extends Fragment {

    private RecyclerView rvPeopleNearby, rvRecommended;
    private PersonNearbyAdapter nearbyAdapter;
    private RecommendedUserAdapter recommendedAdapter;
    private TextInputEditText searchInput;
    private ImageButton clearButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Initialize views
        searchInput = view.findViewById(R.id.search_input);
        clearButton = view.findViewById(R.id.clear_button);
        rvPeopleNearby = view.findViewById(R.id.rvPeopleNearby);
        rvRecommended = view.findViewById(R.id.rvRecommended);

        setupSearch();
        setupAdapters();
        setupRecyclerViews();
        loadDummyData();

        return view;
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearButton.setOnClickListener(v -> searchInput.setText(""));
    }

    private void setupAdapters() {
        // Nearby adapter
        nearbyAdapter = new PersonNearbyAdapter(new ArrayList<>(), position -> {
            // Handle connect button click
            Toast.makeText(getContext(), "Connect clicked: " + position, Toast.LENGTH_SHORT).show();
        });

        // Recommended adapter
        recommendedAdapter = new RecommendedUserAdapter(new ArrayList<>(), position -> {
            // Handle follow button click
            Toast.makeText(getContext(), "Follow clicked: " + position, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerViews() {
        // Nearby people (horizontal)
        rvPeopleNearby.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        rvPeopleNearby.setAdapter(nearbyAdapter);

        // Recommended users (vertical)
        rvRecommended.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecommended.setAdapter(recommendedAdapter);
    }

    private void loadDummyData() {
        // Nearby dummy data
        List<PersonNearby> nearbyList = new ArrayList<>();
        nearbyList.add(new PersonNearby("Alex", "1.2 km away", R.drawable.image1));
        nearbyList.add(new PersonNearby("Taylor", "0.8 km away", R.drawable.image2));
        nearbyList.add(new PersonNearby("Jordan", "2.1 km away", R.drawable.image3));
        nearbyList.add(new PersonNearby("Morgan", "1.5 km away", R.drawable.image4));
        nearbyAdapter.nearbyList = nearbyList;
        nearbyAdapter.notifyDataSetChanged();

        // Recommended dummy data
        List<RecommendedUser> recommendedList = new ArrayList<>();
        recommendedList.add(new RecommendedUser("Alex Johnson", "Travel, Photography", R.drawable.image5));
        recommendedList.add(new RecommendedUser("Sam Wilson", "Music, Hiking", R.drawable.image1));
        recommendedList.add(new RecommendedUser("Casey Lee", "Cooking, Art", R.drawable.image3));
        recommendedAdapter.recommendedList = recommendedList;
        recommendedAdapter.notifyDataSetChanged();
    }
}