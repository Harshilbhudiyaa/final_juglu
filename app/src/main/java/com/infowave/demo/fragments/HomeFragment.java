package com.infowave.demo.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.infowave.demo.supabase.StoriesRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.infowave.demo.R;
import com.infowave.demo.adapters.FeedAdapter;
import com.infowave.demo.models.Post;
import com.infowave.demo.models.StatusItem;
import com.infowave.demo.supabase.SupabaseClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private RecyclerView feedRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton addPostFab;
    private FeedAdapter feedAdapter;
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

        feedRecyclerView = view.findViewById(R.id.feed_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        addPostFab = view.findViewById(R.id.add_post_fab);

        setupFeedRecyclerView();
        setupSwipeRefresh();
        setupClickListeners();
    }

    private void setupFeedRecyclerView() {
        statusList = new ArrayList<>();
        SharedPreferences prefs = requireContext().getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", null);

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "No user selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ONLY THIS: delegate logic to repository!
        StoriesRepository.getStoriesForMeAndFriends(requireContext(), currentUserId, new StoriesRepository.AllStoriesCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onStoriesLoaded(JSONArray storiesArr) {
                statusList.clear();
                for (int i = 0; i < storiesArr.length(); i++) {
                    JSONObject obj = storiesArr.optJSONObject(i);
                    if (obj != null) {
                        String imageUrl = obj.optString("media_url", "");
                        String username = obj.optString("caption", "Story");
                        String storyId = obj.optString("id", "");
                        statusList.add(new StatusItem(imageUrl, username, false, storyId));
                    }
                }
                if (feedAdapter != null) feedAdapter.notifyDataSetChanged();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error loading stories: " + message, Toast.LENGTH_SHORT).show();
            }
        });
        // Static posts/feed logic (unchanged)
        posts = new ArrayList<>();
        posts.add(new Post("John Doe", "2 hours ago", "Beautiful sunset 🌅", 120, 30, R.drawable.image1, R.drawable.image1));
        posts.add(new Post("Emma", "4 hours ago", "Morning workout 💪", 80, 20, R.drawable.image2, R.drawable.image2));
        posts.add(new Post("Mike", "6 hours ago", "New project launched 🚀", 200, 40, R.drawable.image4, R.drawable.image4));

        feedAdapter = new FeedAdapter(requireContext(), statusList, posts);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        feedRecyclerView.setAdapter(feedAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            setupFeedRecyclerView();
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Feed Refreshed", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        addPostFab.setOnClickListener(v -> Toast.makeText(getContext(), "Add Post Clicked", Toast.LENGTH_SHORT).show());
    }
}
