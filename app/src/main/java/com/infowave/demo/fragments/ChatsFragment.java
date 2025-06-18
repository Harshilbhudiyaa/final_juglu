package com.infowave.demo.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.infowave.demo.R;
import com.infowave.demo.adapters.PersonNearbyAdapter;
import com.infowave.demo.adapters.RecommendedUserAdapter;
import com.infowave.demo.models.PersonNearby;
import com.infowave.demo.models.RecommendedUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.content.Intent;
import com.infowave.demo.activities.ChatActivity;

// ChatsFragment.java
public class ChatsFragment extends Fragment {
    private RecyclerView chatListRecycler;
    private ChatListAdapter chatListAdapter;
    private List<ChatListItem> chatList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        chatListRecycler = view.findViewById(R.id.chat_list_recycler);
        chatListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList = getSampleChatList();
        chatListAdapter = new ChatListAdapter(chatList);
        chatListRecycler.setAdapter(chatListAdapter);
        return view;
    }

    private List<ChatListItem> getSampleChatList() {
        List<ChatListItem> list = new ArrayList<>();
        list.add(new ChatListItem("VIMARSH", "3 new messages", "25m", R.drawable.image1, true));
        list.add(new ChatListItem("Keval Kumbhani", "Reacted ❤️ to your message", "11h", R.drawable.image3, false));
        list.add(new ChatListItem("VIMARSH hetanshi", "Ohh", "14h", R.drawable.image2, false));
        list.add(new ChatListItem("KUNJ PATEL", "Sent a reel by saurabh.singhh18", "16h", R.drawable.image5, true));
        list.add(new ChatListItem("unseen_amity", "Sent a reel by unseen_amity", "16h", R.drawable.image1, true));
        list.add(new ChatListItem("Jenil Donga", "Reacted 😮 to your message", "21h", R.drawable.image4, false));
        list.add(new ChatListItem("Abhi Tadhani", "Reacted 😂 to your message", "24h", R.drawable.image2, false));
        list.add(new ChatListItem("Krunal Mandanka", "Sent a reel by the_gujumemess", "4d", R.drawable.image1, true));
        list.add(new ChatListItem("Dr. VIMARSH Raiyani", "Sent a post by go__nature", "Reply?", R.drawable.image5, false));
        list.add(new ChatListItem("VIMARSH", "Reacted 😂 to your message", "1d", R.drawable.image1, false));

        return list;
    }

    // ChatListItem model
    public static class ChatListItem {
        private String userName;
        private String lastMessage;
        private String time;
        private int profileImageRes;
        private boolean unread;

        public ChatListItem(String userName, String lastMessage, String time, int profileImageRes, boolean unread) {
            this.userName = userName;
            this.lastMessage = lastMessage;
            this.time = time;
            this.profileImageRes = profileImageRes;
            this.unread = unread;
        }
        public String getUserName() { return userName; }
        public String getLastMessage() { return lastMessage; }
        public String getTime() { return time; }
        public int getProfileImageRes() { return profileImageRes; }
        public boolean isUnread() { return unread; }
    }

    // ChatListAdapter for RecyclerView
    public static class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
        private List<ChatListItem> chatList;
        public ChatListAdapter(List<ChatListItem> chatList) { this.chatList = chatList; }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatListItem item = chatList.get(position);
            holder.userName.setText(item.getUserName());
            holder.lastMessage.setText(item.getLastMessage());
            holder.time.setText(item.getTime());
            holder.profileImage.setImageResource(item.getProfileImageRes());
            holder.unreadDot.setVisibility(item.isUnread() ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("username", item.getUserName());
                    intent.putExtra("profileRes", item.getProfileImageRes());
                    context.startActivity(intent);
                }
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