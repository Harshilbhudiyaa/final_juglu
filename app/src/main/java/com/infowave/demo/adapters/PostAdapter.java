package com.infowave.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.infowave.demo.CommentBottomSheet;
import com.infowave.demo.R;
import com.infowave.demo.models.Post;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
        holder.postImage.setImageResource(post.getImageResId());
        holder.profileImage.setImageResource(post.getProfileImageResId());

        if (post.isLiked()) {
            holder.likeButton.setColorFilter(context.getResources().getColor(R.color.error));
        } else {
            holder.likeButton.setColorFilter(context.getResources().getColor(android.R.color.white));
        }

        holder.likeButton.setOnClickListener(v -> {
            if (!post.isLiked()) {
                int currentLikes = post.getLikes() + 1;
                post.setLikes(currentLikes);
                post.setLiked(true);
                holder.likesCount.setText(String.valueOf(currentLikes));
                holder.likeButton.setColorFilter(context.getResources().getColor(R.color.error));
            } else {
                int currentLikes = post.getLikes() - 1;
                post.setLikes(currentLikes);
                post.setLiked(false);
                holder.likesCount.setText(String.valueOf(currentLikes));
                holder.likeButton.setColorFilter(context.getResources().getColor(android.R.color.white));
            }
        });

        holder.commentButton.setOnClickListener(v -> {
            CommentBottomSheet.newInstance().show(((AppCompatActivity)context).getSupportFragmentManager(), "Comments");
        });

        holder.shareButton.setOnClickListener(v -> {
            String shareLink = "https://myapp.com/post/" + position;

            // Create the share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this post!");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareLink);

            // Start system share chooser
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView authorName, timestamp, content, likesCount, commentsCount;
        ImageButton likeButton, commentButton, shareButton;
        ImageView postImage;
        CircleImageView profileImage;

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
            postImage = itemView.findViewById(R.id.post_image);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }

} 