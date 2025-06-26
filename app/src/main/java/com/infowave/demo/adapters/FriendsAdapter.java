package com.infowave.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.infowave.demo.FriendProfileActivity;
import com.infowave.demo.R;
import com.infowave.demo.models.Friends;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    public interface OnUnfollowClick {
        void onUnfollow(Friends friend);
    }

    private List<Friends> list;
    private OnUnfollowClick listener;
    private Context context;

    public FriendsAdapter(Context context, List<Friends> list, OnUnfollowClick listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friends f = list.get(position);
        holder.name.setText(f.getName());
        holder.mutual.setText(f.getMutual());
        holder.image.setImageResource(f.getImageRes());

        // Unfollow button click
        holder.unfollow.setOnClickListener(v -> listener.onUnfollow(f));

        // Entire item click (redirect to profile)
        holder.itemView.setOnClickListener(v -> openFriendProfile(f));
    }

    private void openFriendProfile(Friends friend) {
        Intent profileIntent = new Intent(context, FriendProfileActivity.class);
        profileIntent.putExtra("friend", friend);
        context.startActivity(profileIntent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, mutual;
        Button unfollow;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.friend_image);
            name = itemView.findViewById(R.id.friend_name);
            mutual = itemView.findViewById(R.id.friend_mutual);
            unfollow = itemView.findViewById(R.id.unfollow_btn);
        }
    }
}