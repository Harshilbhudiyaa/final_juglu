package com.infowave.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infowave.demo.R;
import com.infowave.demo.models.ChatMessage;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ChatMessage> messages;
    private final String currentUserId;
    private final String receiverProfileUrl; // For 1-to-1 chats only

    // Constructor when you know the receiver's profile URL (for all received messages)
    public ChatAdapter(List<ChatMessage> messages, String currentUserId, String receiverProfileUrl) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.receiverProfileUrl = receiverProfileUrl;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        return msg.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
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

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message, receiverProfileUrl);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for Sent Messages (right side)
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        LinearLayout messageLayout;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sent_message_text);
            timeText = itemView.findViewById(R.id.sent_time_text);
            messageLayout = itemView.findViewById(R.id.sent_message_layout);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
            timeText.setText(message.getCreatedAt());
        }
    }

    // ViewHolder for Received Messages (left side)
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        CircleImageView profileImage;
        LinearLayout messageLayout;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.received_message_text);
            timeText = itemView.findViewById(R.id.received_time_text);
            profileImage = itemView.findViewById(R.id.message_profile_image);
            messageLayout = itemView.findViewById(R.id.received_message_layout);
        }

        void bind(ChatMessage message, String receiverProfileUrl) {
            messageText.setText(message.getMessage());
            timeText.setText(message.getCreatedAt());

            // Use the profile URL passed for the receiver
            if (profileImage != null) {
                if (receiverProfileUrl != null && !receiverProfileUrl.isEmpty()) {
                    Glide.with(profileImage.getContext())
                            .load(receiverProfileUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }
        }
    }
}
