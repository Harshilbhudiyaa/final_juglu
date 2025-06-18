package com.infowave.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.ChatMessage;
import com.infowave.demo.activities.ChatActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// Updated: Added click listener to open ChatActivity from chat item
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<ChatMessage> chatList;

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chatMessage = chatList.get(position);
        if (chatMessage.isReceived()) {
            holder.receivedLayout.setVisibility(View.VISIBLE);
            holder.sentLayout.setVisibility(View.GONE);
            holder.receivedMessage.setText(chatMessage.getMessage());
            holder.receivedTimestamp.setText(chatMessage.getTimestamp());
            holder.receivedProfileImage.setImageResource(chatMessage.getProfileImage());
        } else {
            holder.receivedLayout.setVisibility(View.GONE);
            holder.sentLayout.setVisibility(View.VISIBLE);
            holder.sentMessage.setText(chatMessage.getMessage());
            holder.sentTimestamp.setText(chatMessage.getTimestamp());
            holder.sentProfileImage.setImageResource(chatMessage.getProfileImage());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("username", chatMessage.getSender());
                intent.putExtra("profileRes", chatMessage.getProfileImage());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View receivedLayout, sentLayout;
        TextView receivedMessage, receivedTimestamp, sentMessage, sentTimestamp;
        CircleImageView receivedProfileImage, sentProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedLayout = itemView.findViewById(R.id.received_message_layout);
            sentLayout = itemView.findViewById(R.id.sent_message_layout);
            receivedMessage = itemView.findViewById(R.id.received_message);
            receivedTimestamp = itemView.findViewById(R.id.received_timestamp);
            sentMessage = itemView.findViewById(R.id.sent_message);
            sentTimestamp = itemView.findViewById(R.id.sent_timestamp);
            receivedProfileImage = itemView.findViewById(R.id.received_profile_image);
            sentProfileImage = itemView.findViewById(R.id.sent_profile_image);
        }
    }
}
