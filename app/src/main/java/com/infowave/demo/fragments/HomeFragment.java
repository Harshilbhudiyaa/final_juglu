package com.infowave.demo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.infowave.demo.R;
import com.infowave.demo.adapters.PostAdapter;
import com.infowave.demo.models.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton addPostFab;
    private PostAdapter postAdapter;
    private List<Post> posts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupClickListeners();
        loadPosts();
    }

    private void initializeViews(View view) {
        postsRecyclerView = view.findViewById(R.id.posts_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        addPostFab = view.findViewById(R.id.add_post_fab);
    }

    private void setupRecyclerView() {
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(requireContext(), posts);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        postsRecyclerView.setAdapter(postAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);
    }

    private void setupClickListeners() {
        addPostFab.setOnClickListener(v -> {
            // TODO: Implement add post functionality
            Toast.makeText(getContext(), "Add Post clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadPosts() {
        // TODO: Load actual posts from your data source
        posts.clear();
        posts.add(new Post("John Doe", "2 hours ago", "Beautiful sunset at the beach! 🌅", 128, 32));
        posts.add(new Post("Jane Smith", "4 hours ago", "Just finished my morning workout 💪", 64, 16));
        posts.add(new Post("Mike Johnson", "6 hours ago", "New project launch today! 🚀", 256, 48));
        postAdapter.notifyDataSetChanged();
    }

    private void refreshPosts() {
        // Simulate network delay
        swipeRefreshLayout.postDelayed(() -> {
            loadPosts();
            swipeRefreshLayout.setRefreshing(false);
        }, 1500);
    }
} 