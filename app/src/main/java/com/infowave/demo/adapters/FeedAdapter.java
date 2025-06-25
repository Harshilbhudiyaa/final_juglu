package com.infowave.demo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.media3.common.Player;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infowave.demo.CommentBottomSheet;
import com.infowave.demo.R;
import com.infowave.demo.models.Post;
import com.infowave.demo.models.StatusItem;
import com.infowave.demo.supabase.PostsRepository;

import java.util.List;

// Import media3 ExoPlayer classes
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_STATUS = 0;
    private static final int TYPE_POST = 1;

    private final Context context;
    private final List<StatusItem> statusList;
    private final List<Post> postList;
    private final String currentUserId;

    public FeedAdapter(Context context, List<StatusItem> statusList, List<Post> postList) {
        this.context = context;
        this.statusList = statusList;
        this.postList = postList;
        SharedPreferences prefs = context.getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", "");
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TYPE_STATUS : TYPE_POST;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof StatusSectionViewHolder) {
            StatusSectionViewHolder statusHolder = (StatusSectionViewHolder) holder;
            StatusAdapter statusAdapter = new StatusAdapter(context, statusList);
            statusHolder.statusRecyclerView.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            );
            statusHolder.statusRecyclerView.setAdapter(statusAdapter);
        } else if (holder instanceof PostViewHolder) {
            Post post = postList.get(position - 1);
            PostViewHolder postHolder = (PostViewHolder) holder;

            postHolder.authorName.setText(post.getAuthor());
            postHolder.timestamp.setText(post.getTimestamp());
            postHolder.content.setText(post.getContent());

            // Profile image logic
            if (post.getProfileUrl() != null && !post.getProfileUrl().isEmpty()) {
                Glide.with(context)
                        .load(post.getProfileUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(postHolder.profileImage);
            } else if (post.getProfileImageResId() != 0) {
                postHolder.profileImage.setImageResource(post.getProfileImageResId());
            } else {
                postHolder.profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // --- MEDIA LOGIC ---
            String mediaUrl = post.getImageUrl();
            boolean isVideo = mediaUrl != null && mediaUrl.toLowerCase().endsWith(".mp4");

            // Always clean up old player instance
            if (postHolder.exoPlayer != null) {
                postHolder.exoPlayer.release();
                postHolder.exoPlayer = null;
            }
            postHolder.postImage.setVisibility(View.GONE);
            postHolder.playIcon.setVisibility(View.GONE);
            postHolder.postPlayerView.setVisibility(View.GONE);

            if (isVideo) {
                postHolder.postImage.setVisibility(View.VISIBLE);
                postHolder.playIcon.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(mediaUrl)
                        .frame(1000000)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(postHolder.postImage);

                View.OnClickListener playListener = v -> {
                    postHolder.postImage.setVisibility(View.GONE);
                    postHolder.playIcon.setVisibility(View.GONE);
                    postHolder.postPlayerView.setVisibility(View.VISIBLE);

                    postHolder.exoPlayer = new ExoPlayer.Builder(context).build();
                    postHolder.postPlayerView.setPlayer(postHolder.exoPlayer);

                    MediaItem mediaItem = MediaItem.fromUri(mediaUrl);
                    postHolder.exoPlayer.setMediaItem(mediaItem);
                    postHolder.exoPlayer.prepare();
                    postHolder.exoPlayer.setPlayWhenReady(true);

                    postHolder.exoPlayer.addListener(new Player.Listener() {
                        @Override
                        public void onPlaybackStateChanged(int state) {
                            if (state == ExoPlayer.STATE_ENDED) {
                                postHolder.exoPlayer.release();
                                postHolder.exoPlayer = null;
                                postHolder.postPlayerView.setVisibility(View.GONE);
                                postHolder.postImage.setVisibility(View.VISIBLE);
                                postHolder.playIcon.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                };
                postHolder.postImage.setOnClickListener(playListener);
                postHolder.playIcon.setOnClickListener(playListener);

                postHolder.postPlayerView.setOnClickListener(v -> {
                    if (postHolder.exoPlayer != null) {
                        if (postHolder.exoPlayer.isPlaying()) {
                            postHolder.exoPlayer.pause();
                        } else {
                            postHolder.exoPlayer.play();
                        }
                    }
                });

            } else {
                postHolder.postImage.setVisibility(View.VISIBLE);
                postHolder.playIcon.setVisibility(View.GONE);

                if (mediaUrl != null && !mediaUrl.isEmpty()) {
                    Glide.with(context)
                            .load(mediaUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(postHolder.postImage);
                } else if (post.getImageResId() != 0) {
                    postHolder.postImage.setImageResource(post.getImageResId());
                } else {
                    postHolder.postImage.setImageResource(R.drawable.ic_profile_placeholder);
                }
                postHolder.postImage.setOnClickListener(null);
            }

            // --------- DYNAMIC LIKES/COMMENTS ---------
            postHolder.likesCount.setText(String.valueOf(post.getLikes()));
            postHolder.commentsCount.setText(String.valueOf(post.getComments()));
            postHolder.likeButton.setImageResource(
                    post.isLiked() ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline
            );


            // Query dynamic likes/comments and isLiked
            PostsRepository.getPostEngagements(context, post.getId(), currentUserId, (likeCount, commentCount, likedByMe) -> {
                postHolder.likesCount.setText(String.valueOf(likeCount));
                postHolder.commentsCount.setText(String.valueOf(commentCount));
                postHolder.likeButton.setImageResource(likedByMe ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline);
                post.setLikes(likeCount);
                post.setComments(commentCount);
                post.setLiked(likedByMe);
            });

            // Like/Unlike functionality
            postHolder.likeButton.setOnClickListener(v -> {
                boolean willLike = !post.isLiked();
                int newCount = post.getLikes() + (willLike ? 1 : -1);
                post.setLiked(willLike);
                post.setLikes(newCount);
                postHolder.likesCount.setText(String.valueOf(newCount));
                postHolder.likeButton.setImageResource(willLike ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline);

                if (willLike) {
                    PostsRepository.likePost(context, post.getId(), currentUserId, () -> {}, () -> {});
                } else {
                    PostsRepository.unlikePost(context, post.getId(), currentUserId, () -> {}, () -> {});
                }
            });

            // Double-tap Heart Animation
            GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (!post.isLiked()) {
                        post.setLiked(true);
                        post.setLikes(post.getLikes() + 1);
                        postHolder.likeButton.setImageResource(R.drawable.ic_heart_red);
                        postHolder.likesCount.setText(String.valueOf(post.getLikes()));
                        PostsRepository.likePost(context, post.getId(), currentUserId, () -> {}, () -> {});
                    }
                    postHolder.doubleTapHeart.setScaleX(0f);
                    postHolder.doubleTapHeart.setScaleY(0f);
                    postHolder.doubleTapHeart.setAlpha(0.8f);
                    postHolder.doubleTapHeart.setVisibility(View.VISIBLE);

                    postHolder.doubleTapHeart.animate()
                            .scaleX(3.5f)
                            .scaleY(3.5f)
                            .alpha(1f)
                            .setDuration(300)
                            .withEndAction(() -> postHolder.doubleTapHeart.animate()
                                    .alpha(0f)
                                    .setDuration(500)
                                    .withEndAction(() -> postHolder.doubleTapHeart.setVisibility(View.GONE))
                                    .start())
                            .start();

                    return true;
                }
            });
            postHolder.postImage.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true;
            });

            // Comments
            postHolder.commentsCount.setOnClickListener(v -> showCommentBottomSheet());
            postHolder.commentButton.setOnClickListener(v -> showCommentBottomSheet());

            postHolder.shareButton.setOnClickListener(v -> {
                String shareLink = "https://myapp.com/post/" + position;
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this post!");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "Look at this post by " + post.getAuthor() + ":\n\n"
                                + post.getContent() + "\n\n"
                                + shareLink);
                context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            });
        }
    }

    private void showCommentBottomSheet() {
        if (context instanceof AppCompatActivity) {
            CommentBottomSheet bottomSheet = CommentBottomSheet.newInstance();
            bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "Comments");
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

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof PostViewHolder) {
            PostViewHolder postHolder = (PostViewHolder) holder;
            if (postHolder.exoPlayer != null) {
                postHolder.exoPlayer.release();
                postHolder.exoPlayer = null;
            }
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        ImageView playIcon;
        PlayerView postPlayerView;
        ExoPlayer exoPlayer;
        ImageView doubleTapHeart;
        de.hdodenhof.circleimageview.CircleImageView profileImage;
        TextView authorName;
        TextView timestamp;
        TextView content;
        TextView likesCount;
        TextView commentsCount;
        ImageButton likeButton;
        ImageButton commentButton;
        ImageButton shareButton;
        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_image);
            playIcon = itemView.findViewById(R.id.play_icon);
            postPlayerView = itemView.findViewById(R.id.post_player_view);
            doubleTapHeart = itemView.findViewById(R.id.double_tap_heart);
            profileImage = itemView.findViewById(R.id.profile_image);
            authorName = itemView.findViewById(R.id.author_name);
            timestamp = itemView.findViewById(R.id.timestamp);
            content = itemView.findViewById(R.id.content);
            likesCount = itemView.findViewById(R.id.likes_count);
            commentsCount = itemView.findViewById(R.id.comments_count);
            likeButton = itemView.findViewById(R.id.like_button);
            commentButton = itemView.findViewById(R.id.comment_button);
            shareButton = itemView.findViewById(R.id.share_button);
            exoPlayer = null;
        }
    }
}
