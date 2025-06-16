package com.infowave.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.Post;
import com.infowave.demo.models.StatusItem;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_STATUS = 0;
    private static final int TYPE_POST = 1;

    private final Context context;
    private final List<StatusItem> statusList;
    private final List<Post> postList;

    public FeedAdapter(Context context, List<StatusItem> statusList, List<Post> postList) {
        this.context = context;
        this.statusList = statusList;
        this.postList = postList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_STATUS;
        return TYPE_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_STATUS) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_status_horizontal, parent, false);
            return new StatusSectionViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof StatusSectionViewHolder) {
            StatusSectionViewHolder statusHolder = (StatusSectionViewHolder) holder;
            StatusAdapter statusAdapter = new StatusAdapter(context, statusList);
            statusHolder.statusRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            statusHolder.statusRecyclerView.setAdapter(statusAdapter);
        } else if (holder instanceof PostViewHolder) {
            Post post = postList.get(position - 1); // -1 because 0 is status
            PostViewHolder postHolder = (PostViewHolder) holder;
            postHolder.authorName.setText(post.getAuthor());
            postHolder.timestamp.setText(post.getTimestamp());
            postHolder.content.setText(post.getContent());
            postHolder.likesCount.setText(String.valueOf(post.getLikes()));
            postHolder.commentsCount.setText(String.valueOf(post.getComments()));
            postHolder.postImage.setImageResource(post.getImageResId());
            postHolder.profileImage.setImageResource(post.getProfileImageResId());
        }
    }

    @Override
    public int getItemCount() {
        return 1 + postList.size();
    }

    static class StatusSectionViewHolder extends RecyclerView.ViewHolder {
        RecyclerView statusRecyclerView;
        StatusSectionViewHolder(@NonNull View itemView) {
            super(itemView);
            statusRecyclerView = itemView.findViewById(R.id.status_horizontal_recycler);
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        android.widget.ImageView postImage;
        de.hdodenhof.circleimageview.CircleImageView profileImage;
        android.widget.TextView authorName;
        android.widget.TextView timestamp;
        android.widget.TextView content;
        android.widget.TextView likesCount;
        android.widget.TextView commentsCount;
        android.widget.ImageButton likeButton;
        android.widget.ImageButton commentButton;
        android.widget.ImageButton shareButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_image);
            profileImage = itemView.findViewById(R.id.profile_image);
            authorName = itemView.findViewById(R.id.author_name);
            timestamp = itemView.findViewById(R.id.timestamp);
            content = itemView.findViewById(R.id.content);
            likesCount = itemView.findViewById(R.id.likes_count);
            commentsCount = itemView.findViewById(R.id.comments_count);
            likeButton = itemView.findViewById(R.id.like_button);
            commentButton = itemView.findViewById(R.id.comment_button);
            shareButton = itemView.findViewById(R.id.share_button);
        }
    }
} 