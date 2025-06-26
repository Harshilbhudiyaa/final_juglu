package com.infowave.demo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infowave.demo.R;
import com.infowave.demo.adapters.ChatAdapter; // <-- use the fixed adapter from earlier
import com.infowave.demo.models.ChatMessage;
import com.infowave.demo.supabase.ChatRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ImageView back_button, profileImage;
    TextView usernameText;
    EditText messageInput;
    ImageView sendButton;
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;
    List<ChatMessage> messageList = new ArrayList<>();

    String currentUserId, otherUserId, username, receiverProfileUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // === Get views ===
        back_button = findViewById(R.id.back_button);
        profileImage = findViewById(R.id.chat_profile_image);
        usernameText = findViewById(R.id.chat_username);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        recyclerView = findViewById(R.id.chat_recycler_view);

        // === Intent data ===
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        otherUserId = intent.getStringExtra("otherUserId");
        receiverProfileUrl = intent.getStringExtra("profileUrl"); // must be putExtra when launching

        // === Set header (username + image) ===
        usernameText.setText(username != null ? username : "Unknown");
        if (receiverProfileUrl != null && !receiverProfileUrl.isEmpty()) {
            Glide.with(this)
                    .load(receiverProfileUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
        }

        back_button.setOnClickListener(v -> finishAfterTransition());

        // === User IDs ===
        currentUserId = getCurrentUserId();

        // === RecyclerView setup ===
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messageList, currentUserId, receiverProfileUrl);
        recyclerView.setAdapter(chatAdapter);

        // === Load chat history ===
        ChatRepository.fetchMessagesBetweenUsers(
                this,
                currentUserId,
                otherUserId,
                new ChatRepository.ChatCallback<List<ChatMessage>>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(List<ChatMessage> result) {
                        messageList.clear();
                        messageList.addAll(result);
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // === Send button logic ===
        sendButton.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (msg.isEmpty()) return;

            sendButton.setEnabled(false);
            ChatRepository.sendMessage(
                    ChatActivity.this,
                    currentUserId,
                    otherUserId,
                    msg,
                    new ChatRepository.ChatCallback<ChatMessage>() {
                        @Override
                        public void onSuccess(ChatMessage sentMessage) {
                            messageInput.setText("");
                            sendButton.setEnabled(true);

                            // Add sent message to list and refresh UI instantly
                            messageList.add(sentMessage);
                            chatAdapter.notifyItemInserted(messageList.size() - 1);
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        }
                        @Override
                        public void onFailure(String error) {
                            sendButton.setEnabled(true);
                            Toast.makeText(ChatActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });
    }

    private String getCurrentUserId() {
        return getSharedPreferences("juglu_prefs", MODE_PRIVATE).getString("user_id", "-1");
    }
}
