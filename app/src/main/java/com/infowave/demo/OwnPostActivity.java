package com.infowave.demo;

import android.content.Context;
import android.content.Intent;
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
        View decoreview = getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets insets) {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                v.setPadding(left,top,right,bottom);
                return insets.consumeSystemWindowInsets();
            }
        });
        back = findViewById(R.id.btn_back);
        back.setOnClickListener(V->{
            finish();
        });
        rv = findViewById(R.id.rv_own_posts);
        rv.setLayoutManager(new GridLayoutManager(this, 3));


        // TODO: replace this with real fetch from your backend/Supabase
        loadDummyPosts();

        PostAdapter adapter = new PostAdapter(this, posts);
        rv.setAdapter(adapter);
    }

    private void loadDummyPosts() {
        // Example; in real life you'd call an API
        posts.add(new Own_posts("1", R.drawable.image1));
        posts.add(new Own_posts("2", R.drawable.image2));
        posts.add(new Own_posts("3", R.drawable.image3));
        posts.add(new Own_posts("4", R.drawable.image4));
        posts.add(new Own_posts("5", R.drawable.image5));
        posts.add(new Own_posts("6", R.drawable.image2));
        // … add as many as you need
    }

    // Adapter inner class
    static class PostAdapter extends RecyclerView.Adapter<PostAdapter.HVH> {
        Context ctx;
        List<Own_posts> list;
        PostAdapter(Context ctx, List<Own_posts> list){ this.ctx=ctx; this.list=list; }

        @NonNull @Override
        public HVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_post_image, parent, false);
            return new HVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HVH h, int i) {
            Own_posts p = list.get(i);
            Glide.with(ctx).load(p.getImageUrl()).into(h.iv);

        }

        @Override public int getItemCount() { return list.size(); }

        static class HVH extends RecyclerView.ViewHolder {
            ImageView iv;
            HVH(View itemView) {
                super(itemView);
                iv = itemView.findViewById(R.id.iv_post_image);
            }
        }
    }
}