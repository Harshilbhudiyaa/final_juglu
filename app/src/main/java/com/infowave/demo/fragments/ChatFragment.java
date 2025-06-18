package com.infowave.demo.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.adapters.ChatAdapter;
import com.infowave.demo.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView userName;
    private CircleImageView profileImage;
    private ImageButton backButton, videoCallButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Header
        userName = view.findViewById(R.id.user_name);
        profileImage = view.findViewById(R.id.profile_image);
        backButton = view.findViewById(R.id.back_button);
        videoCallButton = view.findViewById(R.id.video_call_button);

        userName.setText("Sarah");
        profileImage.setImageResource(R.drawable.profile1);

        // Optionally set click listeners
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
        videoCallButton.setOnClickListener(v -> {
            // Handle video call click
        });

        // Chat list
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList = getSampleChatData();
        chatAdapter = new ChatAdapter(chatList);
        chatRecyclerView.setAdapter(chatAdapter);

        // Input bar
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                chatList.add(new ChatMessage("Me", text, "Now", false, R.drawable.my_profile));
                chatAdapter.notifyItemInserted(chatList.size() - 1);
                chatRecyclerView.scrollToPosition(chatList.size() - 1);
                messageInput.setText("");
            }
        });

        return view;
    }

    private List<ChatMessage> getSampleChatData() {
        List<ChatMessage> list = new ArrayList<>();
        // Sample data similar to the image
        list.add(new ChatMessage("Me", "Are you free this weekend?", "10:23 AM", false, R.drawable.my_profile));
        list.add(new ChatMessage("Sarah", "Yes, I'm free!", "10:24 AM", true, R.drawable.profile1));
        list.add(new ChatMessage("Me", "Do you want to grab a coffee?", "10:25 AM", false, R.drawable.my_profile));
        list.add(new ChatMessage("Sarah", "Sure, let's do it!", "10:26 AM", true, R.drawable.profile1));
        return list;
    }
}
