package com.infowave.demo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infowave.demo.R;
import com.infowave.demo.adapters.ChatAdapter;
import com.infowave.demo.models.ChatMessage;
import com.infowave.demo.supabase.ChatRepository;
import com.infowave.demo.supabase.MediaUploadRepository;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ImageView back_button, profileImage;
    TextView usernameText;
    EditText messageInput;
    ImageView sendButton;
    ImageView audioCallButton, videoCallButton,attachment_button;

    RecyclerView recyclerView;
    ChatAdapter chatAdapter;
    private String lastShownCallInviteId = null;

    String currentUserId, otherUserId, username, receiverProfileUrl;

    private Handler pollHandler = new Handler(Looper.getMainLooper());
    private Runnable pollRunnable;
    private static final int POLL_INTERVAL_MS = 2000;

    private static final int CALL_PERMISSION_REQUEST_CODE = 888;
    private static final int REQUEST_PICK_PHOTO = 102;
    private static final int REQUEST_PICK_VIDEO = 103;

    private boolean isVideoCall = true;

    private static final String[] CALL_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        back_button = findViewById(R.id.back_button);
        profileImage = findViewById(R.id.chat_profile_image);
        usernameText = findViewById(R.id.chat_username);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        recyclerView = findViewById(R.id.chat_recycler_view);
        audioCallButton = findViewById(R.id.voice_call_button);
        videoCallButton = findViewById(R.id.video_call_button);
        attachment_button = findViewById(R.id.attachment_button);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        otherUserId = intent.getStringExtra("otherUserId");
        receiverProfileUrl = intent.getStringExtra("profileUrl");

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
        currentUserId = getCurrentUserId();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(
                new ArrayList<>(),
                currentUserId,
                receiverProfileUrl,
                new ChatAdapter.CallBubbleClickListener() {
                    @Override
                    public void onCallBubbleClick(String room, boolean isVideo) {
                        launchJitsiCall(room, isVideo);
                    }
                }
        );
        recyclerView.setAdapter(chatAdapter);


        loadChatHistory();

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
                            chatAdapter.addMessage(sentMessage);
                            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                        }
                        @Override
                        public void onFailure(String error) {
                            sendButton.setEnabled(true);
                            Toast.makeText(ChatActivity.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        audioCallButton.setOnClickListener(v -> {
            if (hasCallPermissions()) {
                sendCallInviteAndLaunchCall("audio");
            } else {
                requestCallPermissions(false);
            }
        });

        videoCallButton.setOnClickListener(v -> {
            if (hasCallPermissions()) {
                sendCallInviteAndLaunchCall("video");
            } else {
                requestCallPermissions(true);
            }
        });
        attachment_button.setOnClickListener(v -> {
            String[] options = {"Photo", "Video"};
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ChatActivity.this);
            builder.setTitle("Send Media")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Photo selected: open image picker
                            Intent attachment_intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            attachment_intent.setType("image/*");
                            startActivityForResult(attachment_intent, REQUEST_PICK_PHOTO);
                        } else if (which == 1) {
                            // Video selected: open video picker
                            Intent attachment_intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                            attachment_intent.setType("video/*");
                            startActivityForResult(attachment_intent, REQUEST_PICK_VIDEO);
                        }
                    });
            builder.show();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startPollingMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPollingMessages();
    }

    private void startPollingMessages() {
        pollRunnable = () -> {
            loadChatHistory();
            pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
        };
        pollHandler.post(pollRunnable);
    }

    private void stopPollingMessages() {
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }

    private void loadChatHistory() {
        ChatRepository.fetchMessagesBetweenUsers(
                this,
                currentUserId,
                otherUserId,
                new ChatRepository.ChatCallback<List<ChatMessage>>() {
                    @Override
                    public void onSuccess(List<ChatMessage> result) {
                        chatAdapter.replaceMessages(result);
                        if (result != null && !result.isEmpty()) {
                            recyclerView.scrollToPosition(result.size() - 1);
                            ChatMessage lastMsg = result.get(result.size() - 1);
                            if (isCallInviteMessage(lastMsg)
                                    && lastMsg.getReceiverId().equals(currentUserId)
                                    && (lastShownCallInviteId == null || !lastShownCallInviteId.equals(lastMsg.getId()))) {
                                lastShownCallInviteId = lastMsg.getId();
                                showIncomingCallDialog(lastMsg);
                            }
                        }
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Detects call invite type for adapter and incoming dialog
    private boolean isCallInviteMessage(ChatMessage msg) {
        try {
            String content = msg.getContent();
            if (content == null) return false;
            JSONObject obj = new JSONObject(content);
            return obj.has("room") && obj.has("call_type");
        } catch (Exception e) {
            return false;
        }
    }

    private void showIncomingCallDialog(ChatMessage inviteMsg) {
        String room;
        String callType;
        try {
            JSONObject contentJson = new JSONObject(inviteMsg.getContent());
            room = contentJson.optString("room", "");
            callType = contentJson.optString("call_type", "video");
        } catch (Exception e) {
            room = "juglu_" + currentUserId + "_" + otherUserId;
            callType = "video";
        }

        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_incoming_call, null, false);

        ImageView callerImg = dialogView.findViewById(R.id.incoming_caller_image);
        TextView callerName = dialogView.findViewById(R.id.incoming_caller_name);
        TextView callTypeTxt = dialogView.findViewById(R.id.incoming_call_type);

        callerName.setText(username != null ? username : "Incoming Call");
        callTypeTxt.setText("audio".equalsIgnoreCase(callType) ? "Audio Call" : "Video Call");
        if (receiverProfileUrl != null && !receiverProfileUrl.isEmpty()) {
            Glide.with(this)
                    .load(receiverProfileUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(callerImg);
        } else {
            callerImg.setImageResource(R.drawable.ic_profile_placeholder);
        }

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        final String finalRoom = room;
        final boolean finalIsVideo = "video".equalsIgnoreCase(callType);

        dialogView.findViewById(R.id.btn_accept_call).setOnClickListener(v -> {
            dialog.dismiss();
            launchJitsiIncomingCall(finalRoom, finalIsVideo);
        });

        dialogView.findViewById(R.id.btn_reject_call).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void launchJitsiIncomingCall(String roomName, boolean isVideo) {
        launchJitsiCall(roomName, isVideo);
    }

    private void launchJitsiCall(String roomName, boolean isVideo) {
        try {
            URL serverURL = new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions.Builder optionsBuilder = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setRoom(roomName)
                    // Disable lobby and prejoin, force instant join
                    .setFeatureFlag("lobby.enabled", false)
                    .setFeatureFlag("prejoinpage.enabled", false)
                    .setFeatureFlag("invite.enabled", false)
                    .setFeatureFlag("add-people.enabled", false)
                    .setFeatureFlag("meeting-password.enabled", false);

            if (!isVideo) optionsBuilder.setVideoMuted(true);

            JitsiMeetActivity.launch(this, optionsBuilder.build());
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Error starting call: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private String getCurrentUserId() {
        return getSharedPreferences("juglu_prefs", MODE_PRIVATE).getString("user_id", "-1");
    }

    private boolean hasCallPermissions() {
        for (String perm : CALL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestCallPermissions(boolean video) {
        isVideoCall = video;
        ActivityCompat.requestPermissions(this, CALL_PERMISSIONS, CALL_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (hasCallPermissions()) {
                sendCallInviteAndLaunchCall(isVideoCall ? "video" : "audio");
            } else {
                Toast.makeText(this, "Camera and Microphone permissions are required for calling.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendCallInviteAndLaunchCall(String callType) {
        String roomName = "juglu_" + java.util.UUID.randomUUID().toString();

        ChatRepository.sendCallInvite(
                this,
                currentUserId,
                otherUserId,
                roomName,
                callType,
                new ChatRepository.ChatCallback<ChatMessage>() {
                    @Override
                    public void onSuccess(ChatMessage result) {
                        launchJitsiCall(roomName, callType.equals("video"));
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this, "Could not send call invite: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_PICK_PHOTO) {
                android.net.Uri photoUri = data.getData();
                if (photoUri != null) {
                    MediaUploadRepository.uploadChatImage(this, photoUri, currentUserId, new MediaUploadRepository.ImageUploadCallback() {
                        @Override
                        public void onSuccess(String publicUrl) {
                            sendImageMessage(publicUrl);
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(ChatActivity.this, "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show();
                }
            }
            else if (requestCode == REQUEST_PICK_VIDEO) {
                android.net.Uri videoUri = data.getData();
                if (videoUri != null) {
                    MediaUploadRepository.uploadChatVideo(this, videoUri, currentUserId, new MediaUploadRepository.ImageUploadCallback() {
                        @Override
                        public void onSuccess(String publicUrl) {
                            sendVideoMessage(publicUrl);
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(ChatActivity.this, "Video upload failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private String getRealPathFromURI(android.net.Uri contentUri) {
        String[] proj = {android.provider.MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
    private void sendImageMessage(String imageUrl) {
        ChatRepository.sendMediaMessage(
                this,
                currentUserId,
                otherUserId,
                imageUrl,
                "image", // type
                new ChatRepository.ChatCallback<ChatMessage>() {
                    @Override
                    public void onSuccess(ChatMessage sentMessage) {
                        chatAdapter.addMessage(sentMessage);
                        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this, "Failed to send image.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void sendVideoMessage(String videoUrl) {
        ChatRepository.sendMediaMessage(
                this,
                currentUserId,
                otherUserId,
                videoUrl,
                "video", // type
                new ChatRepository.ChatCallback<ChatMessage>() {
                    @Override
                    public void onSuccess(ChatMessage sentMessage) {
                        chatAdapter.addMessage(sentMessage);
                        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ChatActivity.this, "Failed to send video.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }



}
