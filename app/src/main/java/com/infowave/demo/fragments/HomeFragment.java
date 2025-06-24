package com.infowave.demo.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.infowave.demo.supabase.StoriesRepository;
import com.infowave.demo.supabase.PostsRepository;
import com.infowave.demo.supabase.SupabaseClient;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private RecyclerView feedRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton addPostFab;
    private FeedAdapter feedAdapter;
    private List<Post> posts = new ArrayList<>();
    private List<StatusItem> statusList = new ArrayList<>();

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

        feedAdapter = new FeedAdapter(requireContext(), statusList, posts);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        feedRecyclerView.setAdapter(feedAdapter);

        setupFeedRecyclerView();
        setupSwipeRefresh();
        setupClickListeners();
    }

    private void setupFeedRecyclerView() {
        statusList.clear();
        posts.clear();

        SharedPreferences prefs = requireContext().getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", null);

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "No user selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Get all user IDs (me + friends)
        Set<String> userIdSet = new HashSet<>();
        userIdSet.add(currentUserId);

        String url = SupabaseClient.getBaseUrl() + "/rest/v1/friendships"
                + "?or=(user_one.eq." + currentUserId + ",user_two.eq." + currentUserId + ")"
                + "&status=eq.accepted&select=user_one,user_two";

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET, url, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj != null) {
                        String userOne = obj.optString("user_one");
                        String userTwo = obj.optString("user_two");
                        if (userOne.equals(currentUserId) && !userTwo.equals(currentUserId)) {
                            userIdSet.add(userTwo);
                        } else if (userTwo.equals(currentUserId) && !userOne.equals(currentUserId)) {
                            userIdSet.add(userOne);
                        }
                    }
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Friend parse error", Toast.LENGTH_SHORT).show();
            }

            // 2. Fetch stories for all these users
            StoriesRepository.getAllStoriesCustom(requireContext(),
                    SupabaseClient.getBaseUrl()
                            + "/rest/v1/stories?user_id=in.(" + String.join(",", userIdSet) + ")&select=*",
                    new StoriesRepository.AllStoriesCallback() {
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
                            feedAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onError(String message) {
                            Toast.makeText(getContext(), "Error loading stories: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });

            // 3. Fetch posts for all these users
            List<String> userIdList = new ArrayList<>(userIdSet);
            PostsRepository.getPostsForUsers(requireContext(), userIdList, new PostsRepository.AllPostsCallback() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostsLoaded(JSONArray postsArr) {
                    posts.clear();
                    for (int i = 0; i < postsArr.length(); i++) {
                        JSONObject obj = postsArr.optJSONObject(i);
                        if (obj != null) {
                            // Join result: user data inside "users"
                            JSONObject user = obj.optJSONObject("users");
                            String author = user != null ? user.optString("full_name", "User") : "User";
                            String profileImage = user != null ? user.optString("profile_image", "") : "";

                            String caption = obj.optString("caption", "");
                            String createdAt = obj.optString("created_at", "");
                            String mediaUrl = obj.optString("media_url", "");

                            posts.add(new Post(author, createdAt, caption, 120, 30, mediaUrl, profileImage));
                        }
                    }
                    feedAdapter.notifyDataSetChanged();
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Error loading posts: " + message, Toast.LENGTH_SHORT).show();
                }
            });

        }, error -> Toast.makeText(requireContext(), "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders();
            }
        };

        SupabaseClient.getInstance(requireContext()).getRequestQueue().add(request);
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
