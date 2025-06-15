package com.infowave.demo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.infowave.demo.R;
import com.infowave.demo.adapters.ProfileAdapter;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private TextView username, userBio, friendsCount, postsCount, likesCount;
    private MaterialButton editProfileButton, privacySettingsButton, blockedUsersButton, logoutButton;
    private RecyclerView profileRecyclerView;
    private ProfileAdapter profileAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        initializeViews(view);
        
        // Set up click listeners
        setupClickListeners();
        
        // Load profile data
        loadProfileData();
        
        // Set up RecyclerView
        setupRecyclerView();
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        userBio = view.findViewById(R.id.user_bio);
        friendsCount = view.findViewById(R.id.friends_count);
        postsCount = view.findViewById(R.id.posts_count);
        likesCount = view.findViewById(R.id.likes_count);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        privacySettingsButton = view.findViewById(R.id.privacy_settings_button);
        blockedUsersButton = view.findViewById(R.id.blocked_users_button);
        logoutButton = view.findViewById(R.id.logout_button);
        profileRecyclerView = view.findViewById(R.id.profile_recycler_view);
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            // TODO: Implement edit profile functionality
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
        });

        privacySettingsButton.setOnClickListener(v -> {
            // TODO: Implement privacy settings functionality
            Toast.makeText(getContext(), "Privacy Settings clicked", Toast.LENGTH_SHORT).show();
        });

        blockedUsersButton.setOnClickListener(v -> {
            // TODO: Implement blocked users functionality
            Toast.makeText(getContext(), "Blocked Users clicked", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            // TODO: Implement logout functionality
            Toast.makeText(getContext(), "Logout clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProfileData() {
        // TODO: Load actual user data from your data source
        username.setText("John Doe");
        userBio.setText("Software Developer | Coffee Lover");
        friendsCount.setText("128");
        postsCount.setText("56");
        likesCount.setText("1.2K");
    }

    private void setupRecyclerView() {
        List<String> profileItems = new ArrayList<>();
        profileItems.add("Account Settings");
        profileItems.add("Notification Preferences");
        profileItems.add("Language Settings");
        profileItems.add("Help & Support");
        profileItems.add("About");

        profileAdapter = new ProfileAdapter(requireContext(), profileItems);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        profileRecyclerView.setAdapter(profileAdapter);
    }
} 