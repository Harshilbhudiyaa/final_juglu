package com.infowave.demo;


import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class PostDetailActivity extends AppCompatActivity {
    ImageView ivImage, btnLike;
    TextView tvLikes, tvComments;
    RecyclerView rvComments;
    String postId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
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
        ivImage    = findViewById(R.id.iv_detail_image);
        btnLike    = findViewById(R.id.btn_like);
        tvLikes    = findViewById(R.id.tv_like_count);
        tvComments = findViewById(R.id.tv_comment_count);
//        rvComments = findViewById(R.id.rv_comments);

        postId = getIntent().getStringExtra("POST_ID");
        loadPostDetail(postId);
        setupLikeButton();
        setupCommentsRecycler();
    }

    private void loadPostDetail(String id) {
        // TODO: fetch imageUrl, likesCount, commentsCount from your API/Supabase by postId
        int imageUrl = R.drawable.image1; // demo
        int likes = 123, comments = 45;

        Glide.with(this).load(imageUrl).into(ivImage);
        tvLikes.setText(likes + " likes");
        tvComments.setText(comments + " comments");
    }

    private void setupLikeButton() {
        btnLike.setOnClickListener(v -> {
            // TODO: toggle like state via API, update UI
        });
    }

    private void setupCommentsRecycler() {
        // TODO: fetch comments list and bind to a RecyclerView adapter
    }
}