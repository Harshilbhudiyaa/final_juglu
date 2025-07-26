// FullScreenMediaActivity.java
package com.infowave.demo.activities;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.infowave.demo.R;

public class FullScreenMediaActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "media_url";
    public static final String EXTRA_TYPE = "media_type"; // "image", "video", "audio"

    private VideoView videoView;
    private ImageView imageView, audioIcon, audioPlayButton;
    private LinearLayout audioLayout;
    private MediaPlayer mediaPlayer;
    private boolean isAudioPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_media);

        // Find all views
        imageView = findViewById(R.id.fullscreen_image);
        videoView = findViewById(R.id.fullscreen_video_view);
        audioLayout = findViewById(R.id.audio_layout);
        audioIcon = findViewById(R.id.fullscreen_audio_icon);
        audioPlayButton = findViewById(R.id.fullscreen_audio_play);

        // Get intent data
        String url = getIntent().getStringExtra(EXTRA_URL);
        String type = getIntent().getStringExtra(EXTRA_TYPE);

        if (type == null || url == null) {
            Toast.makeText(this, "Invalid media", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hide all by default
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        audioLayout.setVisibility(View.GONE);

        // Set up by media type
        switch (type) {
            case "image":
                imageView.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(imageView);
                break;

            case "video":
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(Uri.parse(url));
                videoView.setMediaController(new MediaController(this));
                videoView.requestFocus();
                videoView.setOnPreparedListener(mp -> videoView.start());
                videoView.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(this, "Video can't be played.", Toast.LENGTH_SHORT).show();
                    return true;
                });
                break;

            case "audio":
                audioLayout.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(R.drawable.ic_audio_waveform)
                        .into(audioIcon);
                audioPlayButton.setImageResource(R.drawable.ic_playu);
                audioPlayButton.setOnClickListener(v -> {
                    if (!isAudioPlaying) {
                        playAudio(url);
                    } else {
                        stopAudio();
                    }
                });
                break;
        }
    }

    private void playAudio(String url) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isAudioPlaying = true;
                audioPlayButton.setImageResource(R.drawable.ic_person); // Use your pause icon
            });
            mediaPlayer.setOnCompletionListener(mp -> stopAudio());
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(this, "Audio play error", Toast.LENGTH_SHORT).show();
            isAudioPlaying = false;
            audioPlayButton.setImageResource(R.drawable.ic_playu);
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isAudioPlaying = false;
        audioPlayButton.setImageResource(R.drawable.ic_playu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
    }

    @Override
    public void onBackPressed() {
        stopAudio();
        super.onBackPressed();
    }
}
