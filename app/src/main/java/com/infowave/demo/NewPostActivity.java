package com.infowave.demo;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NewPostActivity extends AppCompatActivity {

    ImageView selectedImage;
    EditText captionInput;
    Button shareBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

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
        selectedImage = findViewById(R.id.image_preview);
        captionInput = findViewById(R.id.edit_caption);
        shareBtn = findViewById(R.id.btn_share);

        shareBtn.setOnClickListener(v -> {
            String caption = captionInput.getText().toString().trim();
            if (caption.isEmpty()) {
                Toast.makeText(this, "Add a caption", Toast.LENGTH_SHORT).show();
            } else {
                // You can now upload the post with media + caption
                Toast.makeText(this, "Post shared!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
