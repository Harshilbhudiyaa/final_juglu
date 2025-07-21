package com.infowave.demo.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // <-- ADD THIS IMPORT
import com.infowave.demo.R;
import com.infowave.demo.models.UserSearchResult;

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private List<UserSearchResult> results;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onClick(UserSearchResult user);
    }

    public UserSearchAdapter(List<UserSearchResult> results, OnUserClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    public void setResults(List<UserSearchResult> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search_result, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserSearchAdapter.ViewHolder holder, int position) {
        UserSearchResult user = results.get(position);
        holder.name.setText(user.getFullName());
        holder.username.setText("@" + user.getUsername());

        // Use Glide to load the profile image from URL
        String imageUrl = user.getProfileImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.profileImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.default_profile) // Set your placeholder image
                    .error(R.drawable.default_profile)      // Set your error image
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.default_profile);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(user));
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView name, username;

        ViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
        }
    }
}
