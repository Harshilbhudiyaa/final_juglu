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
import com.infowave.demo.adapters.StatusAdapter;
import com.infowave.demo.models.Post;
import com.infowave.demo.models.StatusItem;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView postsRecyclerView, statusRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton addPostFab;
    private PostAdapter postAdapter;
    private StatusAdapter statusAdapter;
    private List<Post> posts;
    private List<StatusItem> statusList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postsRecyclerView = view.findViewById(R.id.posts_recycler_view);
        statusRecyclerView = view.findViewById(R.id.status_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        addPostFab = view.findViewById(R.id.add_post_fab);

        setupRecyclerViews();
        setupSwipeRefresh();
        setupClickListeners();
    }

    private void setupRecyclerViews() {
        // Setup status recycler (horizontal)
        statusList = new ArrayList<>();
        statusList.add(new StatusItem(R.drawable.image1, "Your Story", true));
        statusList.add(new StatusItem(R.drawable.image2, "John", false));
        statusList.add(new StatusItem(R.drawable.image3, "Emma", false));
        statusList.add(new StatusItem(R.drawable.image4, "Mike", false));

        statusAdapter = new StatusAdapter(requireContext(), statusList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        statusRecyclerView.setLayoutManager(horizontalLayoutManager);
        statusRecyclerView.setAdapter(statusAdapter);

        // Setup posts recycler (vertical)
        posts = new ArrayList<>();
        posts.add(new Post("John Doe", "2 hours ago", "Beautiful sunset 🌅", 120, 30, R.drawable.image1, R.drawable.image1));
        posts.add(new Post("Emma", "4 hours ago", "Morning workout 💪", 80, 20, R.drawable.image2, R.drawable.image2));
        posts.add(new Post("Mike", "6 hours ago", "New project launched 🚀", 200, 40, R.drawable.image3, R.drawable.image3));

        postAdapter = new PostAdapter(requireContext(), posts);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        postsRecyclerView.setAdapter(postAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Feed Refreshed", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        addPostFab.setOnClickListener(v -> Toast.makeText(getContext(), "Add Post Clicked", Toast.LENGTH_SHORT).show());
    }
}
