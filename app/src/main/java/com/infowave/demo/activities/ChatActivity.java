package com.infowave.demo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.infowave.demo.R;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ImageView back_button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
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
        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAfterTransition();
            }
        });
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        int profileRes = intent.getIntExtra("profileRes", R.drawable.ic_profile_placeholder);

        TextView usernameText = findViewById(R.id.chat_username);
        ImageView profileImage = findViewById(R.id.chat_profile_image);
        usernameText.setText(username);
        profileImage.setImageResource(profileRes);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ChatAdapter(getMessages()));
    }

    private List<Message> getMessages() {
        List<Message> messages = new ArrayList<>();

        // Add static messages
        messages.add(new Message("Hello there!", false, "4:30 PM"));
        messages.add(new Message("Hi! How are you?", true, "4:31 PM"));
        messages.add(new Message("I'm good, thanks! What about you?", false, "4:32 PM"));


        return messages;
    }

    // Message Model Class
    static class Message {
        private final String content;
        private final boolean isSent;
        private final String timestamp;

        public Message(String content, boolean isSent, String timestamp) {
            this.content = content;
            this.isSent = isSent;
            this.timestamp = timestamp;
        }

        public String getContent() { return content; }
        public boolean isSent() { return isSent; }
        public String getTimestamp() { return timestamp; }
    }

    // Chat Adapter
    static class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_SENT = 1;
        private static final int VIEW_TYPE_RECEIVED = 2;

        private final List<Message> messages;

        public ChatAdapter(List<Message> messages) {
            this.messages = messages;
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
            Message message = messages.get(position);

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
            return messages.get(position).isSent() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
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

            public void bind(Message message) {
                messageText.setText(message.getContent());
                timeText.setText(message.getTimestamp());
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

            public void bind(Message message) {
                messageText.setText(message.getContent());
                timeText.setText(message.getTimestamp());
            }
        }
    }
}