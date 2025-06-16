package com.infowave.demo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.adapters.FeatureAdapter;
import com.infowave.demo.models.FeatureItem;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private RecyclerView featuresRecyclerView;
    private FeatureAdapter featureAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        featuresRecyclerView = view.findViewById(R.id.profile_recycler_view);
        featuresRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        featuresRecyclerView.setNestedScrollingEnabled(false);

        // Initialize with empty list
        featureAdapter = new FeatureAdapter(new ArrayList<>(), position -> {
            // Handle feature click
            long item = featureAdapter.getItemId(position);
            // Add your click logic here
        });

        featuresRecyclerView.setAdapter(featureAdapter);
        loadFeatures();
    }

    private void loadFeatures() {
        List<FeatureItem> features = new ArrayList<>();

        // Add features with minimalist icons
        features.add(new FeatureItem(
                R.drawable.ic_setting,
                "App Preferences",
                "Customize your experience"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_platte,
                "Theme Options",
                "Light, dark, or system default"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_bell,
                "Notifications",
                "Manage alerts and sounds"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_lock,
                "Privacy Center",
                "Control your data and visibility"
        ));

        features.add(new FeatureItem(
                R.drawable.help,
                "Help Center",
                "FAQs and support resources"
        ));

        featureAdapter.updateData(features);
    }

    // Add to FeatureAdapter class:

}