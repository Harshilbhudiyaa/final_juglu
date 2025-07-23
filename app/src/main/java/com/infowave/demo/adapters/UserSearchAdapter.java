package com.infowave.demo.adapters;

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
import com.infowave.demo.models.UserSearchResult;
import com.infowave.demo.supabase.FollowRepository;

import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private List<UserSearchResult> results;
    private final OnUserClickListener listener;
    private final Context context;

    public interface OnUserClickListener {
        void onClick(UserSearchResult user);
    }

    public UserSearchAdapter(Context context, List<UserSearchResult> results, OnUserClickListener listener) {
        this.context = context;
        this.results = results != null ? results : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSearchAdapter.ViewHolder holder, int position) {
        UserSearchResult user = results.get(position);

        holder.tvName.setText(user.getFullName());
        holder.tvUsername.setText("@" + user.getUsername());
        holder.tvBio.setText(user.getBio());

        String img = user.getProfileImage();
        if (img != null && !img.isEmpty()) {
            Glide.with(holder.ivProfile.getContext())
                    .load(img)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.default_profile);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(user));

        setButtonState(holder.btnFollow, user.getFollowState());

        holder.btnFollow.setOnClickListener(v -> handleFollowClick(user, position));
        holder.btnFollow.setOnLongClickListener(v -> handleLongClickDecline(user, position));
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProfile;
        android.widget.TextView tvName, tvUsername, tvBio;
        MaterialButton btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile  = itemView.findViewById(R.id.ivProfile);
            tvName     = itemView.findViewById(R.id.tvName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvBio      = itemView.findViewById(R.id.tvBio);
            btnFollow  = itemView.findViewById(R.id.btnFollow);
        }
    }

    // ---- public helpers ----
    public void setResults(List<UserSearchResult> newList) {
        this.results = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ---- private helpers ----
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

    private void handleFollowClick(UserSearchResult user, int position) {
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

    private boolean handleLongClickDecline(UserSearchResult user, int position) {
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
