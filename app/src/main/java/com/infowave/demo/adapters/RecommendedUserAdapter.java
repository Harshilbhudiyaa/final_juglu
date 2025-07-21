package com.infowave.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide; // Add this for dynamic image

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.infowave.demo.R;
import com.infowave.demo.models.RecommendedUser;

import java.util.ArrayList;
import java.util.List;

public class RecommendedUserAdapter extends RecyclerView.Adapter<RecommendedUserAdapter.ViewHolder> {

    public List<RecommendedUser> recommendedList;
    private final OnRecommendedUserClickListener listener;

    public interface OnRecommendedUserClickListener {
        void onClick(RecommendedUser user);
    }

    public RecommendedUserAdapter(List<RecommendedUser> recommendedList, OnRecommendedUserClickListener listener) {
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
        // Optional: follow button logic here
    }


    @Override
    public int getItemCount() {
        return recommendedList != null ? recommendedList.size() : 0;
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

    // Helper for updating data
    public void setRecommendedList(List<RecommendedUser> list) {
        this.recommendedList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }
}
