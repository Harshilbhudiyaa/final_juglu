package com.infowave.demo;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.infowave.demo.adapters.PostDetailPagerAdapter;
import java.util.ArrayList;

public class PostDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_post_detail);
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
            ViewPager2 viewPager = findViewById(R.id.viewPagerPosts);
            ArrayList<Integer> postImages = getIntent().getIntegerArrayListExtra("post_images");
            int position = getIntent().getIntExtra("position", 0);

            if (postImages == null || postImages.isEmpty()) {
                throw new IllegalStateException("postImages is null or empty");
            }

            PostDetailPagerAdapter adapter = new PostDetailPagerAdapter(this, postImages);
            viewPager.setAdapter(adapter);

            if (position < 0 || position >= postImages.size()) {
                throw new IllegalArgumentException("Invalid position: " + position);
            }
            viewPager.setCurrentItem(position, false);

        } catch (Exception e) {
            e.printStackTrace();
            // Optionally show a Toast with the error
            android.widget.Toast.makeText(this, "Crash: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }

}
