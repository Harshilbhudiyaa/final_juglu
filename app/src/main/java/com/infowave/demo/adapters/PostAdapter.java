package com.infowave.demo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.R;
import com.infowave.demo.models.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> posts;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.authorName.setText(post.getAuthor());
        holder.timestamp.setText(post.getTimestamp());
        holder.content.setText(post.getContent());
        holder.likesCount.setText(String.valueOf(post.getLikes()));
        holder.commentsCount.setText(String.valueOf(post.getComments()));

        holder.likeButton.setOnClickListener(v -> {
            // TODO: Implement like functionality
        });

        holder.commentButton.setOnClickListener(v -> {
            // TODO: Implement comment functionality
        });

        holder.shareButton.setOnClickListener(v -> {
            // TODO: Implement share functionality
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView authorName, timestamp, content, likesCount, commentsCount;
        ImageButton likeButton, commentButton, shareButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
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