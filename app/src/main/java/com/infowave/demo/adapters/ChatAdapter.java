package com.infowave.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private Context context;
    private List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (message.isReceived()) {
            holder.sentMessageLayout.setVisibility(View.GONE);
            holder.receivedMessageLayout.setVisibility(View.VISIBLE);
            holder.receivedProfileImage.setImageResource(message.getProfileImage());
            holder.receivedMessage.setText(message.getMessage());
            holder.receivedTimestamp.setText(message.getTimestamp());
        } else {
            holder.receivedMessageLayout.setVisibility(View.GONE);
            holder.sentMessageLayout.setVisibility(View.VISIBLE);
            holder.sentProfileImage.setImageResource(message.getProfileImage());
            holder.sentMessage.setText(message.getMessage());
            holder.sentTimestamp.setText(message.getTimestamp());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        View receivedMessageLayout, sentMessageLayout;
        ImageView receivedProfileImage, sentProfileImage;
        TextView receivedMessage, sentMessage;
        TextView receivedTimestamp, sentTimestamp;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedMessageLayout = itemView.findViewById(R.id.received_message_layout);
            sentMessageLayout = itemView.findViewById(R.id.sent_message_layout);
            receivedProfileImage = itemView.findViewById(R.id.received_profile_image);
            sentProfileImage = itemView.findViewById(R.id.sent_profile_image);
            receivedMessage = itemView.findViewById(R.id.received_message);
            sentMessage = itemView.findViewById(R.id.sent_message);
            receivedTimestamp = itemView.findViewById(R.id.received_timestamp);
            sentTimestamp = itemView.findViewById(R.id.sent_timestamp);
        }
    }
} 