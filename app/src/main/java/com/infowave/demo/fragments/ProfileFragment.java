package com.infowave.demo.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.infowave.demo.*;
import com.infowave.demo.adapters.FeatureAdapter;
import com.infowave.demo.models.FeatureItem;
import com.infowave.demo.supabase.ProfileRepository;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private RecyclerView featuresRecyclerView;
    private FeatureAdapter featureAdapter;
    MaterialButton editProfileButton;
    private ShapeableImageView profileImage;
    private TextView username, userBio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        userBio = view.findViewById(R.id.user_bio);

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
                requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit().clear().apply();
                requireActivity().getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE).edit().clear().apply();
                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
                logout_button.postDelayed(() -> {
                    requireActivity().finishAffinity();
                    System.exit(0);
                }, 900);
            });
        }

        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> {
                SharedPreferences prefs = requireContext().getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
                String userId = prefs.getString("user_id", null);
                if (userId != null) {
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "User ID not found, please login again.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        friendsCount.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendsActivity.class);
            startActivity(intent);
        });

        postsCount.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OwnPostActivity.class);
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
        loadFeatures();

        // === Fetch user id from SharedPreferences ===
        SharedPreferences prefs = requireContext().getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        Log.d("PROFILE_FRAGMENT", "Loaded userId from prefs: " + userId);

        if (userId == null) {
            Toast.makeText(requireContext(), "User ID not found, please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- DYNAMIC PROFILE DATA + LOGGING ---
        ProfileRepository.getLoggedInUserProfile(requireContext(), userId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(ProfileRepository.Profile user) {
                Log.d("PROFILE_FRAGMENT", "Profile loaded: name=" + user.name + ", bio=" + user.bio + ", imageUrl=" + user.imageUrl);
                username.setText(user.name);
                userBio.setText(user.bio);
                if (!user.imageUrl.isEmpty() && getContext() != null) {
                    Glide.with(getContext()).load(user.imageUrl).placeholder(R.drawable.image1).into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.image1);
                }
            }
            @Override
            public void onError(String error) {
                Log.e("PROFILE_FRAGMENT", "Profile error: " + error);
                Toast.makeText(requireContext(), "Failed to load profile: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadFeatures() {
        List<FeatureItem> features = new ArrayList<>();
        features.add(new FeatureItem(R.drawable.ic_setting, "App Preferences", "Customize your experience"));
        features.add(new FeatureItem(R.drawable.ic_bell, "Notifications", "Manage alerts and sounds"));
        features.add(new FeatureItem(R.drawable.ic_lock, "Privacy Center", "Control your data and visibility"));
        features.add(new FeatureItem(R.drawable.help, "Help Center", "FAQs and support resources"));
        featureAdapter.updateData(features);
    }
}
