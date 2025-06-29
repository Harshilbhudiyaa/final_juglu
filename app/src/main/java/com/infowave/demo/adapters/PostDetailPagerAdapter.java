package com.infowave.demo.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.infowave.demo.CommentBottomSheet;
import com.infowave.demo.FriendProfileActivity;
import com.infowave.demo.R;
import com.infowave.demo.models.Comment;

import java.util.Arrays;
import java.util.List;

public class PostDetailPagerAdapter extends RecyclerView.Adapter<PostDetailPagerAdapter.ViewHolder> {
    private final Context context;
    private final List<Integer> postImages;


    public PostDetailPagerAdapter(Context context, List<Integer> postImages) {
        this.context = context;
        this.postImages = postImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_detail, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int imgRes = postImages.get(position);
        holder.imageView.setImageResource(imgRes);
        // Optionally set other UI fields here
    }

    @Override
    public int getItemCount() {
        return postImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, heartAnim, profileImage, likeButton, commentButton, shareButton, moreButton;
        TextView username, likesCount, caption;
        boolean isLiked = false;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            imageView     = itemView.findViewById(R.id.post_image);
            heartAnim     = itemView.findViewById(R.id.heart_anim);
            profileImage  = itemView.findViewById(R.id.profile_image);
            username      = itemView.findViewById(R.id.username);
            likesCount    = itemView.findViewById(R.id.likes_count);
            caption       = itemView.findViewById(R.id.caption);
            likeButton    = itemView.findViewById(R.id.like_button);
            commentButton = itemView.findViewById(R.id.comment_button);
            shareButton   = itemView.findViewById(R.id.share_button);
            moreButton = itemView.findViewById(R.id.more_button);

            imageView.setClickable(true);

            GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (!isLiked) {
                        isLiked = true;
                        likeButton.setImageResource(R.drawable.ic_heart_filled);
                    }
                    showHeartAnimation();
                    return true;
                }
            });

            imageView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true;
            });

            likeButton.setOnClickListener(v -> {
                isLiked = !isLiked;
                if (isLiked) {
                    likeButton.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    likeButton.setImageResource(R.drawable.ic_heart_outline);
                }
            });
            commentButton.setOnClickListener(v -> {
                BottomSheetDialog commentsDialog = new BottomSheetDialog(itemView.getContext());
                View commentsView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.bottom_sheet_comments, null, false);

                RecyclerView rvComments = commentsView.findViewById(R.id.comments_recycler);

                // Example comment list with images (use your own drawable resources)
                List<Comment> comments = Arrays.asList(
                        new Comment("alex", "Great photo! 🔥", "2h", R.drawable.image1),
                        new Comment("emma", "Amazing view 😍", "1h", R.drawable.image2),
                        new Comment("rohit", "Wow, where is this?", "45m", R.drawable.image3)

                );

                CommentAdapter adapter = new CommentAdapter(comments);
                rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(itemView.getContext()));
                rvComments.setAdapter(adapter);

                // (Optional) Handle send button for adding comments live
                // EditText commentInput = commentsView.findViewById(R.id.comment_input);
                // ImageButton sendButton = commentsView.findViewById(R.id.send_button);
                // (You can implement comment adding logic as needed)

                commentsDialog.setContentView(commentsView);
                commentsDialog.show();
            });

            shareButton.setOnClickListener(v -> {
                // Optional: Get context and adapter position
                Context ctx = itemView.getContext();
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                // Example share text and optional image (update as per your data structure)
                String shareText = "Check out this photo on MyApp! https://yourapp.com/posts/" + position;

                // If you want to share ONLY a link (recommended for social apps):
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

                // If you want to share the image as well (uncomment below if needed)
                // Uri imageUri = ...; // Provide a valid image Uri (must be FileProvider if file)
                // shareIntent.setType("image/*");
                // shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                // shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Always show chooser for a professional experience
                ctx.startActivity(Intent.createChooser(shareIntent, "Share via"));
            });


            moreButton.setOnClickListener(v -> {
                BottomSheetDialog sheetDialog = new BottomSheetDialog(itemView.getContext());
                View sheetView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.bottom_sheet_post_options, null, false);

                // Find options
                TextView optionEdit = sheetView.findViewById(R.id.option_edit);
                TextView optionDelete = sheetView.findViewById(R.id.option_delete);
                TextView optionHideComment = sheetView.findViewById(R.id.option_hide_comment);
                TextView optionHideLike = sheetView.findViewById(R.id.option_hide_like);
                TextView optionHideShare = sheetView.findViewById(R.id.option_hide_share);

                optionEdit.setOnClickListener(view -> {
                    sheetDialog.dismiss();
                    // Handle Edit
                });

                optionDelete.setOnClickListener(view -> {
                    sheetDialog.dismiss();
                    // Handle Delete
                });

                optionHideComment.setOnClickListener(view -> {
                    sheetDialog.dismiss();
                    // Handle Hide Comment
                });

                optionHideLike.setOnClickListener(view -> {
                    sheetDialog.dismiss();
                    // Handle Hide Like
                });

                optionHideShare.setOnClickListener(view -> {
                    sheetDialog.dismiss();
                    // Handle Hide Share
                });

                sheetDialog.setContentView(sheetView);
                sheetDialog.show();
            });

            View.OnClickListener profileClickListener = v -> {
                Context ctx = itemView.getContext();
                Intent intent = new Intent(ctx, FriendProfileActivity.class);
                ctx.startActivity(intent);
            };

            username.setOnClickListener(profileClickListener);
            profileImage.setOnClickListener(profileClickListener);

        }

        private void showHeartAnimation() {
            heartAnim.setVisibility(View.VISIBLE);
            heartAnim.setAlpha(1f);
            heartAnim.setScaleX(0.3f);
            heartAnim.setScaleY(0.3f);

            heartAnim.animate()
                    .scaleX(1.6f)
                    .scaleY(1.6f)
                    .alpha(0f)
                    .setDuration(700)
                    .withEndAction(() -> {
                        heartAnim.setVisibility(View.GONE);
                        heartAnim.setScaleX(1f);
                        heartAnim.setScaleY(1f);
                        heartAnim.setAlpha(0f);
                    })
                    .start();
        }
    }
}
