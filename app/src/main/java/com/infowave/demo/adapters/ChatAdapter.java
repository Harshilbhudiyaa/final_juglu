package com.infowave.demo.adapters;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import com.infowave.demo.activities.FullScreenMediaActivity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infowave.demo.R;
import com.infowave.demo.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_CALL_SENT = 3;
    private static final int VIEW_TYPE_CALL_RECEIVED = 4;

    private static final String TAG = "JugluChatAdapter";

    private final List<ChatMessage> messages;
    private final String currentUserId;
    private final String receiverProfileUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final CallBubbleClickListener callBubbleClickListener;

    public interface CallBubbleClickListener {
        void onCallBubbleClick(String room, boolean isVideo);
    }

    public ChatAdapter(List<ChatMessage> messages, String currentUserId, String receiverProfileUrl, CallBubbleClickListener callBubbleClickListener) {
        this.messages = (messages != null) ? messages : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.receiverProfileUrl = receiverProfileUrl;
        this.callBubbleClickListener = callBubbleClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.isCallInvite()) {
            return msg.getSenderId().equals(currentUserId) ? VIEW_TYPE_CALL_SENT : VIEW_TYPE_CALL_RECEIVED;
        } else {
            return msg.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_CALL_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_call_invite_sent, parent, false);
            return new CallInviteSentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_call_invite_received, parent, false);
            return new CallInviteReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);

            // MEDIA CLICK HANDLERS for sent messages
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            // IMAGE
            sentHolder.messageImage.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                intent.putExtra(FullScreenMediaActivity.EXTRA_URL, message.getMediaUrl());
                intent.putExtra(FullScreenMediaActivity.EXTRA_TYPE, "image");
                v.getContext().startActivity(intent);
            });
            // VIDEO
            sentHolder.videoContainer.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                intent.putExtra(FullScreenMediaActivity.EXTRA_URL, message.getMediaUrl());
                intent.putExtra(FullScreenMediaActivity.EXTRA_TYPE, "video");
                v.getContext().startActivity(intent);
            });
            // AUDIO
            sentHolder.audioPlayButton.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                intent.putExtra(FullScreenMediaActivity.EXTRA_URL, message.getMediaUrl());
                intent.putExtra(FullScreenMediaActivity.EXTRA_TYPE, "audio");
                v.getContext().startActivity(intent);
            });

        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message, receiverProfileUrl);

            // MEDIA CLICK HANDLERS for received messages
            ReceivedMessageViewHolder recvHolder = (ReceivedMessageViewHolder) holder;
            // IMAGE
            recvHolder.messageImage.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                intent.putExtra(FullScreenMediaActivity.EXTRA_URL, message.getMediaUrl());
                intent.putExtra(FullScreenMediaActivity.EXTRA_TYPE, "image");
                v.getContext().startActivity(intent);
            });
            // VIDEO
            recvHolder.videoContainer.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                intent.putExtra(FullScreenMediaActivity.EXTRA_URL, message.getMediaUrl());
                intent.putExtra(FullScreenMediaActivity.EXTRA_TYPE, "video");
                v.getContext().startActivity(intent);
            });
            // AUDIO
            recvHolder.audioPlayButton.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                intent.putExtra(FullScreenMediaActivity.EXTRA_URL, message.getMediaUrl());
                intent.putExtra(FullScreenMediaActivity.EXTRA_TYPE, "audio");
                v.getContext().startActivity(intent);
            });

        } else if (holder instanceof CallInviteSentViewHolder) {
            ((CallInviteSentViewHolder) holder).bind(message);
            holder.itemView.setOnClickListener(v -> {
                String room = getRoomFromContent(message);
                boolean isVideo = isVideoCallType(message);
                if (callBubbleClickListener != null) {
                    callBubbleClickListener.onCallBubbleClick(room, isVideo);
                }
            });
        } else if (holder instanceof CallInviteReceivedViewHolder) {
            ((CallInviteReceivedViewHolder) holder).bind(message, receiverProfileUrl);
            holder.itemView.setOnClickListener(v -> {
                String room = getRoomFromContent(message);
                boolean isVideo = isVideoCallType(message);
                if (callBubbleClickListener != null) {
                    callBubbleClickListener.onCallBubbleClick(room, isVideo);
                }
            });
        }
    }


    private String getRoomFromContent(ChatMessage message) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(message.getContent());
            return obj.optString("room", "juglu_" + message.getSenderId() + "_" + message.getReceiverId());
        } catch (Exception e) {
            return "juglu_" + message.getSenderId() + "_" + message.getReceiverId();
        }
    }

    private boolean isVideoCallType(ChatMessage message) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(message.getContent());
            return "video".equalsIgnoreCase(obj.optString("call_type", "video"));
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @SuppressLint("LogNotTimber")
    public void addMessage(ChatMessage message) {
        mainHandler.post(() -> {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
            Log.d(TAG, "addMessage: message added at " + (messages.size() - 1));
        });
    }

    @SuppressLint("LogNotTimber")
    public void addMessages(List<ChatMessage> newMessages) {
        if (newMessages == null || newMessages.isEmpty()) return;
        mainHandler.post(() -> {
            int start = messages.size();
            messages.addAll(newMessages);
            notifyItemRangeInserted(start, newMessages.size());
            Log.d(TAG, "addMessages: " + newMessages.size() + " messages added.");
        });
    }

    @SuppressLint({"LogNotTimber", "NotifyDataSetChanged"})
    public void replaceMessages(List<ChatMessage> newMessages) {
        mainHandler.post(() -> {
            messages.clear();
            if (newMessages != null) messages.addAll(newMessages);
            notifyDataSetChanged();
            Log.d(TAG, "replaceMessages: All messages replaced.");
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "LogNotTimber"})
    public void clearMessages() {
        mainHandler.post(() -> {
            messages.clear();
            notifyDataSetChanged();
            Log.d(TAG, "clearMessages: All messages cleared.");
        });
    }
    // ================= SENT MESSAGE VIEWHOLDER ====================
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        LinearLayout messageLayout;
        ImageView messageImage;
        FrameLayout videoContainer;
        ImageView videoThumbnail, videoPlayIcon;
        LinearLayout audioContainer;
        ImageView audioPlayButton;
        TextView audioDuration;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sent_message_text);
            timeText = itemView.findViewById(R.id.sent_time_text);
            messageLayout = itemView.findViewById(R.id.sent_message_layout);

            messageImage = itemView.findViewById(R.id.message_image);
            videoContainer = itemView.findViewById(R.id.video_container);
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            videoPlayIcon = itemView.findViewById(R.id.video_play_icon);
            audioContainer = itemView.findViewById(R.id.audio_container);
            audioPlayButton = itemView.findViewById(R.id.audio_play_button);
            audioDuration = itemView.findViewById(R.id.audio_duration);
        }

        void bind(ChatMessage message) {
            // Hide all media by default
            messageText.setVisibility(View.GONE);
            messageImage.setVisibility(View.GONE);
            videoContainer.setVisibility(View.GONE);
            audioContainer.setVisibility(View.GONE);

            if ("image".equalsIgnoreCase(message.getType())) {
                messageImage.setVisibility(View.VISIBLE);
                Glide.with(messageImage.getContext())
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(messageImage);
                // Optional: full screen
                messageImage.setOnClickListener(v -> {
                    // TODO: Start FullScreenImageActivity if you have one
                });
            } else if ("video".equalsIgnoreCase(message.getType())) {
                videoContainer.setVisibility(View.VISIBLE);
                Glide.with(videoThumbnail.getContext())
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(videoThumbnail);
                // Show play icon
                videoPlayIcon.setVisibility(View.VISIBLE);
                videoContainer.setOnClickListener(v -> {
                    // TODO: Start FullScreenVideoActivity if you have one
                });
            } else if ("audio".equalsIgnoreCase(message.getType())) {
                audioContainer.setVisibility(View.VISIBLE);

                // Optionally set a play icon and clear duration (if not available)
                audioPlayButton.setImageResource(R.drawable.ic_playu); // Replace with your actual play icon
                audioDuration.setText(""); // Or set duration if you want

                // Click launches the full screen audio player
                audioPlayButton.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                    Log.d("AudioPlay", "URL: " + message.getMediaUrl());
                    intent.putExtra(FullScreenMediaActivity.EXTRA_URL, message.getMediaUrl());
                    intent.putExtra(FullScreenMediaActivity.EXTRA_TYPE, "audio");
                    v.getContext().startActivity(intent);
                });
            }
            else {
                // Default to text
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(message.getMessage());
            }
            timeText.setText(message.getCreatedAt());
        }
    }

    // =============== RECEIVED MESSAGE VIEWHOLDER ==================
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        CircleImageView profileImage;
        LinearLayout messageLayout;
        ImageView messageImage;
        FrameLayout videoContainer;
        ImageView videoThumbnail, videoPlayIcon;
        LinearLayout audioContainer;
        ImageView audioPlayButton;
        TextView audioDuration;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.received_message_text);
            timeText = itemView.findViewById(R.id.received_time_text);
            profileImage = itemView.findViewById(R.id.message_profile_image);
            messageLayout = itemView.findViewById(R.id.received_message_layout);

            messageImage = itemView.findViewById(R.id.message_image);
            videoContainer = itemView.findViewById(R.id.video_container);
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            videoPlayIcon = itemView.findViewById(R.id.video_play_icon);
            audioContainer = itemView.findViewById(R.id.audio_container);
            audioPlayButton = itemView.findViewById(R.id.audio_play_button);
            audioDuration = itemView.findViewById(R.id.audio_duration);
        }

        void bind(ChatMessage message, String receiverProfileUrl) {
            // Hide all media by default
            messageText.setVisibility(View.GONE);
            messageImage.setVisibility(View.GONE);
            videoContainer.setVisibility(View.GONE);
            audioContainer.setVisibility(View.GONE);

            if ("image".equalsIgnoreCase(message.getType())) {
                messageImage.setVisibility(View.VISIBLE);
                Glide.with(messageImage.getContext())
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(messageImage);
                messageImage.setOnClickListener(v -> {
                    // TODO: Start FullScreenImageActivity if you have one
                });
            } else if ("video".equalsIgnoreCase(message.getType())) {
                videoContainer.setVisibility(View.VISIBLE);
                Glide.with(videoThumbnail.getContext())
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(videoThumbnail);
                videoPlayIcon.setVisibility(View.VISIBLE);
                videoContainer.setOnClickListener(v -> {
                    // TODO: Start FullScreenVideoActivity if you have one
                });
            } else if ("audio".equalsIgnoreCase(message.getType())) {
                audioContainer.setVisibility(View.VISIBLE);

                // Optionally set a play icon and clear duration (if not available)
                audioPlayButton.setImageResource(R.drawable.ic_playu); // Replace with your actual play icon
                audioDuration.setText(""); // Or set duration if you want

                // Click launches the full screen audio player
                audioPlayButton.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                    Log.d("AudioPlay", "URL: " + message.getMediaUrl());

                    intent.putExtra(FullScreenMediaActivity.EXTRA_URL, message.getMediaUrl());
                    intent.putExtra(FullScreenMediaActivity.EXTRA_TYPE, "audio");
                    v.getContext().startActivity(intent);
                });
            }
            else {
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(message.getMessage());
            }
            timeText.setText(message.getCreatedAt());

            // Profile image
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


    static class CallInviteSentViewHolder extends RecyclerView.ViewHolder {
        TextView callTypeText, callTimeText;
        ImageView callTypeIcon;

        CallInviteSentViewHolder(View itemView) {
            super(itemView);
            callTypeText = itemView.findViewById(R.id.call_invite_type);
            callTimeText = itemView.findViewById(R.id.call_invite_time);
            callTypeIcon = itemView.findViewById(R.id.call_invite_icon);
        }

        @SuppressLint("SetTextI18n")
        void bind(ChatMessage message) {
            String callType = message.getCallType();
            if ("video".equalsIgnoreCase(callType)) {
                callTypeText.setText("Outgoing Video Call");
                callTypeIcon.setImageResource(R.drawable.ic_videocam);
            } else {
                callTypeText.setText("Outgoing Audio Call");
                callTypeIcon.setImageResource(R.drawable.ic_call);
            }
            callTimeText.setText(message.getCreatedAt());
        }
    }

    static class CallInviteReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView callTypeText, callTimeText;
        ImageView callTypeIcon;
        CircleImageView profileImage;

        CallInviteReceivedViewHolder(View itemView) {
            super(itemView);
            callTypeText = itemView.findViewById(R.id.call_invite_type);
            callTimeText = itemView.findViewById(R.id.call_invite_time);
            callTypeIcon = itemView.findViewById(R.id.call_invite_icon);
            profileImage = itemView.findViewById(R.id.call_invite_profile_image);
        }

        @SuppressLint("SetTextI18n")
        void bind(ChatMessage message, String receiverProfileUrl) {
            String callType = message.getCallType();
            if ("video".equalsIgnoreCase(callType)) {
                callTypeText.setText("Incoming Video Call");
                callTypeIcon.setImageResource(R.drawable.ic_videocam);
            } else {
                callTypeText.setText("Incoming Audio Call");
                callTypeIcon.setImageResource(R.drawable.ic_call);
            }
            callTimeText.setText(message.getCreatedAt());
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
