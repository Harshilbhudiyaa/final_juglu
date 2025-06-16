package com.infowave.demo.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.adapters.SearchAdapter;
import com.infowave.demo.models.SearchItem;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SearchAdapter.OnItemClickListener {

    private EditText searchInput;
    private RecyclerView searchResultsRecyclerView;
    private SearchAdapter searchAdapter;
    private List<SearchItem> searchItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupSearchInput();
        loadInitialData();
    }

    private void initializeViews(View view) {
        searchInput = view.findViewById(R.id.search_input);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);
    }

    private void setupRecyclerView() {
        searchItems = new ArrayList<>();
        searchAdapter = new SearchAdapter(requireContext(), searchItems, this);
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        searchResultsRecyclerView.setAdapter(searchAdapter);
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearchResults(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadInitialData() {
        // TODO: Load actual data from your data source
        searchItems.clear();
        searchItems.add(new SearchItem(R.drawable.image2, "John Doe", "johndoe"));
        searchItems.add(new SearchItem(R.drawable.image1, "Jane Smith", "janesmith"));
        searchItems.add(new SearchItem(R.drawable.image3, "Mike Johnson", "mikejohnson"));
        searchItems.add(new SearchItem(R.drawable.image5, "Sarah Wilson", "sarahwilson"));
        searchItems.add(new SearchItem(R.drawable.image4, "David Brown", "davidbrown"));
        searchItems.add(new SearchItem(R.drawable.image2, "Emily Davis", "emilydavis"));
        searchAdapter.notifyDataSetChanged();
    }

    private void filterSearchResults(String query) {
        List<SearchItem> filteredList = new ArrayList<>();
        for (SearchItem item : searchItems) {
            if (item.getName().toLowerCase().contains(query.toLowerCase()) ||
                item.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        searchAdapter.updateData(filteredList);
    }

    @Override
    public void onItemClick(SearchItem item) {
        // Handle item click
        Toast.makeText(requireContext(), "Clicked: " + item.getName(), Toast.LENGTH_SHORT).show();
    }
} 