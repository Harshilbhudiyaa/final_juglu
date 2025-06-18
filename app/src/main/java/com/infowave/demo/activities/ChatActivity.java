package com.infowave.demo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.infowave.demo.R;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        int profileRes = intent.getIntExtra("profileRes", R.drawable.ic_profile_placeholder);

        TextView usernameText = findViewById(R.id.chat_username);
        ImageView profileImage = findViewById(R.id.chat_profile_image);
        usernameText.setText(username);
        profileImage.setImageResource(profileRes);

        // Setup RecyclerView, Adapter, etc. here
    }
} 