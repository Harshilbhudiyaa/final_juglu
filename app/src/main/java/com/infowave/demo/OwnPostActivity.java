package com.infowave.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infowave.demo.models.Own_posts;

import java.util.ArrayList;
import java.util.List;

public class OwnPostActivity extends AppCompatActivity {
    RecyclerView rv;
    ImageView back;
    List<Own_posts> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_post);

        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                v.setPadding(left, top, right, bottom);
                return insets.consumeSystemWindowInsets();
            }
        });

        back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> finish());

        rv = findViewById(R.id.rv_own_posts);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        // Load demo data (replace with real fetch in prod)
        loadDummyPosts();

        PostAdapter adapter = new PostAdapter(this, posts, postId -> showCommentBottomSheet(postId));
        rv.setAdapter(adapter);
    }

    // Show comment bottom sheet dialog
    private void showCommentBottomSheet(String postId) {
        // Replace with your real implementation.
        // Example using a BottomSheetDialogFragment:
        CommentBottomSheet bottomSheet = CommentBottomSheet.newInstance(postId);
        bottomSheet.show(getSupportFragmentManager(), "CommentBottomSheet");
    }

    private void loadDummyPosts() {
        // Example images, use your actual resource or URL
        posts.add(new Own_posts("1", R.drawable.image1));
        posts.add(new Own_posts("2", R.drawable.image2));
        posts.add(new Own_posts("3", R.drawable.image3));
        posts.add(new Own_posts("4", R.drawable.image4));
        posts.add(new Own_posts("5", R.drawable.image5));
        posts.add(new Own_posts("6", R.drawable.image2));
        // Add more as needed
    }

    // ======= Adapter & ViewHolder =======
    static class PostAdapter extends RecyclerView.Adapter<PostAdapter.HVH> {
        Context ctx;
        List<Own_posts> list;
        OnCommentClickListener commentClickListener;

        interface OnCommentClickListener {
            void onCommentClick(String postId);
        }

        PostAdapter(Context ctx, List<Own_posts> list, OnCommentClickListener listener) {
            this.ctx = ctx;
            this.list = list;
            this.commentClickListener = listener;
        }

        @NonNull
        @Override
        public HVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_post_image, parent, false);
            return new HVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HVH h, int i) {
            Own_posts p = list.get(i);
            Glide.with(ctx).load(p.getImageUrl()).into(h.iv);

            // Comment icon click listener
            h.ivComment.setOnClickListener(v -> {
                if (commentClickListener != null) {
                    commentClickListener.onCommentClick(p.getId());
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class HVH extends RecyclerView.ViewHolder {
            ImageView iv, ivComment;

            HVH(View itemView) {
                super(itemView);
                iv = itemView.findViewById(R.id.iv_post_image);
                ivComment = itemView.findViewById(R.id.iv_comment); // Make sure this exists in item_post_image.xml
            }
        }
    }
}
