package com.infowave.demo.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.infowave.demo.R;
import com.infowave.demo.activities.ChatActivity;
import com.infowave.demo.supabase.ChatRepository;
import com.infowave.demo.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private RecyclerView chatListRecycler;
    private ChatListAdapter chatListAdapter;
    private List<ChatRepository.ChatPersonPreview> chatList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        chatListRecycler = view.findViewById(R.id.chat_list_recycler);
        chatListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        chatListAdapter = new ChatListAdapter(chatList, getContext(), getCurrentUserId());
        chatListRecycler.setAdapter(chatListAdapter);

        loadChatList();
        return view;
    }

    private String getCurrentUserId() {
        // Get from shared prefs or your session logic
        return getContext().getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE).getString("user_id", "");
    }

    private void loadChatList() {
        ChatRepository.fetchChatPeople(getContext(), getCurrentUserId(), new ChatRepository.ChatCallback<List<ChatRepository.ChatPersonPreview>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<ChatRepository.ChatPersonPreview> result) {
                chatList.clear();
                chatList.addAll(result);
                chatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to load chats: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter for RecyclerView
    public static class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
        private final List<ChatRepository.ChatPersonPreview> chatList;
        private final Context context;
        private final String currentUserId;

        public ChatListAdapter(List<ChatRepository.ChatPersonPreview> chatList, Context context, String currentUserId) {
            this.chatList = chatList;
            this.context = context;
            this.currentUserId = currentUserId;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatRepository.ChatPersonPreview preview = chatList.get(position);
            ChatRepository.FriendProfile person = preview.friendProfile;
            ChatMessage lastMsg = preview.lastMessage;

            holder.userName.setText(person.fullName != null && !person.fullName.isEmpty() ? person.fullName : person.username);
            holder.lastMessage.setText(lastMsg != null && lastMsg.getContent() != null ? lastMsg.getContent() : "");
            holder.time.setText(lastMsg != null && lastMsg.getCreatedAt() != null ? lastMsg.getCreatedAt() : "");

            // Load actual profile image
            if (person.profileImage != null && !person.profileImage.isEmpty()) {
                Glide.with(context)
                        .load(person.profileImage)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Optional: logic for unread dot (example, always false)
            holder.unreadDot.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("username", person.fullName != null ? person.fullName : person.username);
                intent.putExtra("otherUserId", person.id);
                intent.putExtra("profileRes", R.drawable.ic_profile_placeholder);
                // Pass image URL too if needed!
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return chatList.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView userName, lastMessage, time;
            View unreadDot;
            CircleImageView profileImage;
            ViewHolder(View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.user_name);
                lastMessage = itemView.findViewById(R.id.last_message);
                time = itemView.findViewById(R.id.time);
                unreadDot = itemView.findViewById(R.id.unread_dot);
                profileImage = itemView.findViewById(R.id.profile_image);
            }
        }
    }
}
