package com.infowave.demo;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.infowave.demo.adapters.PostsAdapter;
import com.infowave.demo.models.Friends;
import java.util.Arrays;
import java.util.List;

public class FriendProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        View decoreview = getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                v.setPadding(left,top,right,bottom);
                return insets.consumeSystemWindowInsets();

            }
        });
        // Get friend data
        Friends friend = getIntent().getParcelableExtra("friend");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Set profile data
        ImageView profileImage = findViewById(R.id.profile_image);
        TextView username = findViewById(R.id.username);
        TextView bio = findViewById(R.id.bio);
        TextView friendsCount = findViewById(R.id.friends_count);
        TextView postsCount = findViewById(R.id.posts_count);
        TextView likesCount = findViewById(R.id.likes_count);
        Button followButton = findViewById(R.id.follow_button);
        Button messageButton = findViewById(R.id.message_button);

        profileImage.setImageResource(friend.getImageRes());
        username.setText(friend.getName());
        bio.setText(friend.getBio());
        friendsCount.setText(String.valueOf(friend.getFriendsCount()));
        postsCount.setText(String.valueOf(friend.getPostsCount()));
        likesCount.setText(String.valueOf(friend.getLikesCount()));

        // Action buttons
        followButton.setOnClickListener(v -> {
            String currentText = followButton.getText().toString();
            followButton.setText(currentText.equals("Follow") ? "Following" : "Follow");
        });

        // Setup posts grid
        RecyclerView postsGrid = findViewById(R.id.posts_grid);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        postsGrid.setLayoutManager(layoutManager);

        // Sample post images
        List<Integer> postImages = Arrays.asList(
                R.drawable.image1, R.drawable.image2, R.drawable.image3,
                R.drawable.image4, R.drawable.image5, R.drawable.image1
        );

        PostsAdapter adapter = new PostsAdapter(this, postImages);
        postsGrid.setAdapter(adapter);
    }
}