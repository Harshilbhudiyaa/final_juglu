package com.infowave.demo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.ChatMessage;
import com.infowave.demo.supabase.ChatRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ImageView back_button;
    ChatAdapter chatAdapter;
    RecyclerView recyclerView;
    List<ChatMessage> messageList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Handle insets (optional)
        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            int left = insets.getSystemWindowInsetLeft();
            int top = insets.getSystemWindowInsetTop();
            int right = insets.getSystemWindowInsetRight();
            int bottom = insets.getSystemWindowInsetBottom();
            v.setPadding(left, top, right, bottom);
            return insets.consumeSystemWindowInsets();
        });

        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> finishAfterTransition());

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        int profileRes = intent.getIntExtra("profileRes", R.drawable.ic_profile_placeholder);

        TextView usernameText = findViewById(R.id.chat_username);
        ImageView profileImage = findViewById(R.id.chat_profile_image);
        usernameText.setText(username);
        profileImage.setImageResource(profileRes);

        // Setup RecyclerView and Adapter
        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messageList, getCurrentUserId());
        recyclerView.setAdapter(chatAdapter);

        // Fetch user IDs (from intent or session)
        String currentUserId = getCurrentUserId();
        String otherUserId = intent.getStringExtra("otherUserId"); // Pass via Intent

        // Fetch Messages from Supabase
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
    }

    // Use your real session logic here!
    private String getCurrentUserId() {
        // TODO: Replace with real session/user logic!
        // Example using shared preferences:
        return getSharedPreferences("juglu_prefs", MODE_PRIVATE).getString("user_id", "-1");
        //return "PUT_CURRENT_USER_ID_HERE";
    }

    // Adapter expects List<ChatMessage>
    static class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_SENT = 1;
        private static final int VIEW_TYPE_RECEIVED = 2;

        private final List<ChatMessage> messages;
        private final String currentUserId;

        public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
            this.messages = messages;
            this.currentUserId = currentUserId;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_SENT) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            if (holder.getItemViewType() == VIEW_TYPE_SENT) {
                ((SentMessageViewHolder) holder).bind(message);
            } else {
                ((ReceivedMessageViewHolder) holder).bind(message);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public int getItemViewType(int position) {
            ChatMessage message = messages.get(position);
            // Sent if senderId == currentUserId
            return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        }

        // ViewHolder for sent messages
        static class SentMessageViewHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            LinearLayout messageLayout;

            public SentMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.sent_message_text);
                timeText = itemView.findViewById(R.id.sent_time_text);
                messageLayout = itemView.findViewById(R.id.sent_message_layout);
            }

            public void bind(ChatMessage message) {
                messageText.setText(message.getMessage());
                timeText.setText(message.getCreatedAt());
            }
        }

        // ViewHolder for received messages
        static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            LinearLayout messageLayout;

            public ReceivedMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.received_message_text);
                timeText = itemView.findViewById(R.id.received_time_text);
                messageLayout = itemView.findViewById(R.id.received_message_layout);
            }

            public void bind(ChatMessage message) {
                messageText.setText(message.getMessage());
                timeText.setText(message.getCreatedAt());
            }
        }
    }
}
