package com.infowave.demo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
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
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    private static final int CALL_PERMISSION_REQUEST_CODE = 888;
    private static final int REQUEST_PICK_PHOTO = 102;
    private Handler waveformHandler = new Handler();
    private Runnable waveformRunnable;
    private boolean isFakeWaveformAnimating = false;
    private View[] waveformBars;

    private static final int REQUEST_PICK_VIDEO = 103;
    private boolean isFirstLoad = true;


    private boolean isVideoCall = true;

    private static final String[] CALL_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @SuppressLint({"IntentReset", "ClickableViewAccessibility"})
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
        ImageView audioRecordButton = findViewById(R.id.audio_record_button);
        LinearLayout fakeWaveformContainer = findViewById(R.id.fake_waveform_container);
        View bar1 = findViewById(R.id.bar1);
        View bar2 = findViewById(R.id.bar2);
        View bar3 = findViewById(R.id.bar3);
        View bar4 = findViewById(R.id.bar4);
        View bar5 = findViewById(R.id.bar5);
        View bar6 = findViewById(R.id.bar6);
        View bar7 = findViewById(R.id.bar7);
        View bar8 = findViewById(R.id.bar8);
        waveformBars = new View[]{bar1, bar2, bar3, bar4, bar5, bar6, bar7, bar8};




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


        audioRecordButton.setOnTouchListener(new View.OnTouchListener() {
            float startX, startY;
            boolean isCancelled = false;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        isCancelled = false;
                        startAudioRecording();
                        // Optional: show recording UI
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(event.getX() - startX);
                        float dy = Math.abs(event.getY() - startY);
                        if ((dx > 120 || dy > 120) && !isCancelled) {
                            cancelAudioRecording();
                            // Optional: hide recording UI
                            isCancelled = true;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        // Optional: hide recording UI
                        if (!isCancelled) {
                            stopAudioRecordingAndSend();
                        }
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        // Optional: hide recording UI
                        if (!isCancelled) {
                            cancelAudioRecording();
                        }
                        return true;
                }
                return false;
            }
        });

    }
    private void startFakeWaveformAnimation() {
        isFakeWaveformAnimating = true;
        LinearLayout fakeWaveform = findViewById(R.id.fake_waveform_container);
        fakeWaveform.setVisibility(View.VISIBLE);
        waveformRunnable = new Runnable() {
            @Override
            public void run() {
                int[] minDpArr = {12, 18, 10, 24, 14, 8, 20, 10}; // for more natural look
                int[] maxDpArr = {36, 32, 28, 34, 28, 24, 36, 20};
                for (int i = 0; i < waveformBars.length; i++) {
                    int minDp = minDpArr[i];
                    int maxDp = maxDpArr[i];
                    int heightDp = minDp + (int) (Math.random() * (maxDp - minDp));
                    int heightPx = (int) (heightDp * getResources().getDisplayMetrics().density);
                    waveformBars[i].getLayoutParams().height = heightPx;
                    waveformBars[i].requestLayout();
                }
                if (isFakeWaveformAnimating) {
                    waveformHandler.postDelayed(this, 60); // smoother
                }
            }
        };
        waveformHandler.post(waveformRunnable);
    }

    private void stopFakeWaveformAnimation() {
        isFakeWaveformAnimating = false;
        waveformHandler.removeCallbacksAndMessages(null);
        findViewById(R.id.fake_waveform_container).setVisibility(View.GONE);
    }

    private void startAudioRecording() {
        try {
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio_" + System.currentTimeMillis() + ".m4a";
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;

            // Show full-width waveform, hide input
            messageInput.setVisibility(View.GONE);
            findViewById(R.id.fake_waveform_container).setVisibility(View.VISIBLE);
            startFakeWaveformAnimation();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudioRecordingAndSend() {
        try {
            if (mediaRecorder != null && isRecording) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                // Hide waveform, restore input
                stopFakeWaveformAnimation();
                findViewById(R.id.fake_waveform_container).setVisibility(View.GONE);
                messageInput.setVisibility(View.VISIBLE);

                sendAudioMessage(audioFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Stop recording failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAudioRecording() {
        try {
            if (mediaRecorder != null && isRecording) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                // Hide waveform, restore input
                stopFakeWaveformAnimation();
                findViewById(R.id.fake_waveform_container).setVisibility(View.GONE);
                messageInput.setVisibility(View.VISIBLE);

                if (audioFilePath != null) {
                    new java.io.File(audioFilePath).delete();
                }
                Toast.makeText(this, "Recording cancelled", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                            // Only scroll on first load
                            if (isFirstLoad) {
                                recyclerView.scrollToPosition(result.size() - 1);
                                isFirstLoad = false;
                            }
                            // Call invite handling...
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
                    // --------- SIZE CHECK START ---------
                    long maxSize = 30L * 1024 * 1024; // 30 MB in bytes
                    long fileSize = 0;
                    try {
                        android.database.Cursor cursor = getContentResolver().query(videoUri, null, null, null, null);
                        if (cursor != null) {
                            int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                            if (sizeIndex != -1) {
                                cursor.moveToFirst();
                                fileSize = cursor.getLong(sizeIndex);
                            }
                            cursor.close();
                        }
                    } catch (Exception e) {
                        fileSize = 0;
                    }
                    if (fileSize > maxSize) {
                        Toast.makeText(this, "Video size must be less than 30 MB", Toast.LENGTH_LONG).show();
                        return;
                    }
                    // --------- SIZE CHECK END ---------

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

    private void sendAudioMessage(String audioPath) {
        MediaUploadRepository.uploadChatAudio(this, audioPath, currentUserId, new MediaUploadRepository.ImageUploadCallback() {
            @Override
            public void onSuccess(String publicUrl) {
                // Send audio message in chat
                ChatRepository.sendMediaMessage(
                        ChatActivity.this,
                        currentUserId,
                        otherUserId,
                        publicUrl,
                        "audio", // type
                        new ChatRepository.ChatCallback<ChatMessage>() {
                            @Override
                            public void onSuccess(ChatMessage sentMessage) {
                                chatAdapter.addMessage(sentMessage);
                                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                            }
                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(ChatActivity.this, "Failed to send audio.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(ChatActivity.this, "Audio upload failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
