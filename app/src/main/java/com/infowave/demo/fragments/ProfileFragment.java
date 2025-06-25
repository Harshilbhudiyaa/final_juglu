package com.infowave.demo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.infowave.demo.AppPreferencesActivity;
import com.infowave.demo.HelpCenterActivity;
import com.infowave.demo.NotificationsActivity;
import com.infowave.demo.OwnPostActivity;
import com.infowave.demo.PrivacyCenterActivity;
import com.infowave.demo.R;
import com.infowave.demo.adapters.FeatureAdapter;
import com.infowave.demo.models.FeatureItem;
import com.infowave.demo.BlockedUsersActivity;
import com.infowave.demo.Register;
import com.infowave.demo.EditProfileActivity;
import com.infowave.demo.FriendsActivity;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private RecyclerView featuresRecyclerView;
    private FeatureAdapter featureAdapter;
    MaterialButton editProfileButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        LinearLayout blockedUsersButton = view.findViewById(R.id.blocked_users_button);
        LinearLayout logout_button = view.findViewById(R.id.logout_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        TextView friendsCount = view.findViewById(R.id.friends_count);
        TextView postsCount = view.findViewById(R.id.posts_count);
        if (blockedUsersButton != null) {
            blockedUsersButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), BlockedUsersActivity.class);
                startActivity(intent);
            });
        }

        if (logout_button != null) {
            logout_button.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), Register.class);
                startActivity(intent);
            });
        }


            if (editProfileButton != null) {
                editProfileButton.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    startActivity(intent);
                });
            }

        friendsCount.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendsActivity.class);
            startActivity(intent);
        });

        postsCount.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OwnPostActivity.class);
            // if you need to pass the user ID, you can do:
            // intent.putExtra("USER_ID", currentUserId);
            startActivity(intent);
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        featuresRecyclerView = view.findViewById(R.id.profile_recycler_view);
        featuresRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        featuresRecyclerView.setNestedScrollingEnabled(false);

        // Initialize adapter
        featureAdapter = new FeatureAdapter(new ArrayList<>(), position -> {
            FeatureItem item = featureAdapter.getItem(position);
            String title = item.getTitle();

            if (title.equals("App Preferences")) {
                startActivity(new Intent(requireContext(), AppPreferencesActivity.class));
            } else if (title.equals("Notifications")) {
                startActivity(new Intent(requireContext(), NotificationsActivity.class));
            } else if (title.equals("Privacy Center")) {
                startActivity(new Intent(requireContext(), PrivacyCenterActivity.class));
            } else if (title.equals("Help Center")) {
                startActivity(new Intent(requireContext(), HelpCenterActivity.class));
            }
        });

        featuresRecyclerView.setAdapter(featureAdapter);

        // Load the feature list
        loadFeatures();
    }

    private void loadFeatures() {
        List<FeatureItem> features = new ArrayList<>();

        features.add(new FeatureItem(
                R.drawable.ic_setting, // replace with actual drawable
                "App Preferences",
                "Customize your experience"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_bell, // replace with actual drawable
                "Notifications",
                "Manage alerts and sounds"
        ));

        features.add(new FeatureItem(
                R.drawable.ic_lock, // replace with actual drawable
                "Privacy Center",
                "Control your data and visibility"
        ));

        features.add(new FeatureItem(
                R.drawable.help, // replace with actual drawable
                "Help Center",
                "FAQs and support resources"
        ));

        featureAdapter.updateData(features);
    }
}
