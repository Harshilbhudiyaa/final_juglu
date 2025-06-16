package com.infowave.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.infowave.demo.R;
import com.infowave.demo.models.RecommendedUser;

import java.util.List;

public class RecommendedUserAdapter extends RecyclerView.Adapter<RecommendedUserAdapter.ViewHolder> {

    public List<RecommendedUser> recommendedList;
    private final OnFollowClickListener followClickListener;

    public interface OnFollowClickListener {
        void onFollowClick(int position);
    }

    public RecommendedUserAdapter(List<RecommendedUser> recommendedList, OnFollowClickListener listener) {
        this.recommendedList = recommendedList;
        this.followClickListener = listener;
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
        holder.profileImage.setImageResource(user.getProfileImageRes());

        holder.actionButton.setOnClickListener(v -> {
            if (followClickListener != null) {
                followClickListener.onFollowClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recommendedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profileImage;
        TextView name, interests;
        MaterialButton actionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            interests = itemView.findViewById(R.id.interests);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}