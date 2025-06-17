package com.infowave.demo;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryViewerActivity extends AppCompatActivity {

    private ImageView storyImage, likeIcon, shareIcon;
    private CircleImageView profileImage;
    private TextView username;
    private EditText commentEditText;
    private ProgressBar progressBar;

    private boolean isLiked = false;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_viewer);
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
        storyImage = findViewById(R.id.story_image);
        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        likeIcon = findViewById(R.id.like_icon);
        shareIcon = findViewById(R.id.share_icon);
        commentEditText = findViewById(R.id.comment_edittext);
        progressBar = findViewById(R.id.progress_bar);

        // Receive data from intent
        int imageRes = getIntent().getIntExtra("image", R.drawable.image1);
        String userName = getIntent().getStringExtra("name");
        int profileRes = getIntent().getIntExtra("profile", R.drawable.image3);

        username.setText(userName);
        profileImage.setImageResource(profileRes);
        storyImage.setImageResource(imageRes);


        likeIcon.setOnClickListener(v -> {
            isLiked = !isLiked;
            likeIcon.setImageResource(isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart_filled);
            likeIcon.setColorFilter(isLiked ? getResources().getColor(android.R.color.holo_red_light)
                    : getResources().getColor(android.R.color.white));
        });

        shareIcon.setOnClickListener(v -> showShareDialog());

        commentEditText.setOnEditorActionListener((v, actionId, event) -> {
            String comment = commentEditText.getText().toString().trim();
            if (!comment.isEmpty()) {
                Toast.makeText(this, "Comment: " + comment, Toast.LENGTH_SHORT).show();
                commentEditText.setText("");
            }
            return true;
        });

        startStoryTimer();
    }

    private void startStoryTimer() {
        timer = new CountDownTimer(5000, 50) {
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((5000 - millisUntilFinished) * 100 / 5000);
                progressBar.setProgress(progress);
            }

            public void onFinish() {
                finish();  // Close activity after 5 sec
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }

    private void showShareDialog() {
        String[] options = {"Copy Link", "Share"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share Story");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Story Link", "https://yourlink.com/story");
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Link copied!", Toast.LENGTH_SHORT).show();
            } else if (which == 1) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://yourlink.com/story");
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });

        builder.show();
    }

}
