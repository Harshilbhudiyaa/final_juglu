package com.infowave.demo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.infowave.demo.R;
import com.infowave.demo.models.FollowState;
import com.infowave.demo.models.RecommendedUser;
import com.infowave.demo.supabase.FollowRepository;

import java.util.ArrayList;
import java.util.List;

public class RecommendedUserAdapter extends RecyclerView.Adapter<RecommendedUserAdapter.ViewHolder> {

    public List<RecommendedUser> recommendedList;
    private final OnRecommendedUserClickListener listener;
    private final Context context;

    public interface OnRecommendedUserClickListener {
        void onClick(RecommendedUser user);
    }

    public RecommendedUserAdapter(Context context, List<RecommendedUser> recommendedList, OnRecommendedUserClickListener listener) {
        this.context = context;
        this.recommendedList = recommendedList != null ? recommendedList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommended_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedUser user = recommendedList.get(position);

        holder.name.setText(user.getName());
        holder.interests.setText(user.getInterests());

        String imageUrl = user.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.profileImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(user));

        // Follow button logic
        setButtonState(holder.actionButton, user.getFollowState());

        holder.actionButton.setOnClickListener(v -> handleFollowClick(user, position));
        holder.actionButton.setOnLongClickListener(v -> handleLongClickDecline(user, position));
    }

    @Override
    public int getItemCount() {
        return recommendedList != null ? recommendedList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profileImage;
        android.widget.TextView name, interests;
        MaterialButton actionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            interests = itemView.findViewById(R.id.interests);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }

    // Public helper for updating data
    @SuppressLint("NotifyDataSetChanged")
    public void setRecommendedList(List<RecommendedUser> list) {
        this.recommendedList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ----------------- PRIVATE HELPERS -----------------

    private void setButtonState(MaterialButton btn, FollowState state) {
        switch (state) {
            case NONE:
                btn.setText("Follow");
                btn.setEnabled(true);
                break;
            case REQUEST_SENT:
                btn.setText("Requested");
                btn.setEnabled(true);
                break;
            case REQUEST_RECEIVED:
                btn.setText("Accept");
                btn.setEnabled(true);
                break;
            case FOLLOWING:
                btn.setText("Following");
                btn.setEnabled(true);
                break;
        }
    }

    private void handleFollowClick(RecommendedUser user, int position) {
        switch (user.getFollowState()) {
            case NONE:
                FollowRepository.sendFollowRequest(context, user.getId(), new FollowRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        user.setFollowState(FollowState.REQUEST_SENT);
                        notifyItemChanged(position);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case REQUEST_SENT:
                // Cancel request
                FollowRepository.deleteFriendship(context, user.getFriendshipRowId(), new FollowRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        user.setFollowState(FollowState.NONE);
                        user.setFriendshipRowId(null);
                        notifyItemChanged(position);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case REQUEST_RECEIVED:
                // Accept request
                FollowRepository.acceptRequest(context, user.getFriendshipRowId(), new FollowRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        user.setFollowState(FollowState.FOLLOWING);
                        notifyItemChanged(position);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case FOLLOWING:
                // Unfollow
                FollowRepository.deleteFriendship(context, user.getFriendshipRowId(), new FollowRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        user.setFollowState(FollowState.NONE);
                        user.setFriendshipRowId(null);
                        notifyItemChanged(position);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    // Long press to Decline (only when REQUEST_RECEIVED)
    private boolean handleLongClickDecline(RecommendedUser user, int position) {
        if (user.getFollowState() == FollowState.REQUEST_RECEIVED) {
            FollowRepository.deleteFriendship(context, user.getFriendshipRowId(), new FollowRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    user.setFollowState(FollowState.NONE);
                    user.setFriendshipRowId(null);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return false;
    }
}
