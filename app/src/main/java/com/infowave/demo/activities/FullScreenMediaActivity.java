package com.infowave.demo.activities;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.SeekBar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.infowave.demo.R;
import com.github.chrisbanes.photoview.PhotoView;

public class FullScreenMediaActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "media_url";
    public static final String EXTRA_TYPE = "media_type"; // "image", "video", "audio"

    private PhotoView imageView;
    private VideoView videoView;
    private LinearLayout audioLayout;
    private ImageView audioIcon, audioPlayButton;
    private TextView audioDurationText;
    private SeekBar audioSeekBar;

    private MediaPlayer mediaPlayer;
    private boolean isAudioPlaying = false;
    private boolean isUserSeeking = false;

    private final Handler seekBarHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_media);

        Log.d("AudioPlay", "FullScreenMediaActivity created");

        // Find all views
        imageView = findViewById(R.id.fullscreen_image);
        videoView = findViewById(R.id.fullscreen_video_view);
        audioLayout = findViewById(R.id.audio_layout);
        audioIcon = findViewById(R.id.fullscreen_audio_icon);
        audioPlayButton = findViewById(R.id.fullscreen_audio_play);
        audioDurationText = findViewById(R.id.fullscreen_audio_duration);
        audioSeekBar = findViewById(R.id.fullscreen_audio_seek);

        // Get intent data
        String url = getIntent().getStringExtra(EXTRA_URL);
        String type = getIntent().getStringExtra(EXTRA_TYPE);

        Log.d("AudioPlay", "Received intent. URL: " + url + " | TYPE: " + type);

        if (type == null || url == null) {
            Log.e("AudioPlay", "Missing type or url in intent extras");
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
                Log.d("AudioPlay", "Setting up image mode");
                imageView.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(imageView);
                break;

            case "video":
                Log.d("AudioPlay", "Setting up video mode");
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(Uri.parse(url));
                videoView.setMediaController(new MediaController(this));
                videoView.requestFocus();
                videoView.setOnPreparedListener(mp -> {
                    Log.d("AudioPlay", "VideoView prepared, starting video.");
                    videoView.start();
                });
                videoView.setOnErrorListener((mp, what, extra) -> {
                    Log.e("AudioPlay", "VideoView error: what=" + what + " extra=" + extra);
                    Toast.makeText(this, "Video can't be played.", Toast.LENGTH_SHORT).show();
                    return true;
                });
                break;

            case "audio":
                Log.d("AudioPlay", "Setting up audio mode");
                audioLayout.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(R.drawable.ic_audio_waveform)
                        .into(audioIcon);
                audioPlayButton.setImageResource(R.drawable.ic_playu);

                // Disable seekBar while preparing/idle
                audioSeekBar.setEnabled(false);
                audioSeekBar.setProgress(0);
                audioSeekBar.setMax(100);

                // Play/Pause click
                audioPlayButton.setOnClickListener(v -> {
                    Log.d("AudioPlay", "audioPlayButton clicked. isAudioPlaying=" + isAudioPlaying);
                    if (!isAudioPlaying) {
                        playAudio(url);
                    } else {
                        stopAudio();
                    }
                });

                // SeekBar listeners
                audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // Show current time while user is dragging
                        if (fromUser && mediaPlayer != null) {
                            int duration = mediaPlayer.getDuration();
                            int pos = (int) ((progress / 100f) * duration);
                            audioDurationText.setText(formatDuration(pos) + " / " + formatDuration(duration));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        isUserSeeking = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int duration = mediaPlayer.getDuration();
                            int newPos = (int) ((seekBar.getProgress() / 100f) * duration);
                            mediaPlayer.seekTo(newPos);
                        }
                        isUserSeeking = false;
                    }
                });
                break;

            default:
                Log.e("AudioPlay", "Unknown media type: " + type);
                Toast.makeText(this, "Unknown media type!", Toast.LENGTH_SHORT).show();
        }
    }

    private void playAudio(String url) {
        Log.d("AudioPlay", "playAudio called with URL: " + url);
        try {
            stopAudio(); // Always stop/release any previous MediaPlayer

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);

            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d("AudioPlay", "MediaPlayer prepared, starting playback...");
                mp.start();
                isAudioPlaying = true;
                audioPlayButton.setImageResource(R.drawable.ic_pause);
                audioSeekBar.setEnabled(true);
                startSeekBarUpdate();
                updateAudioDuration();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d("AudioPlay", "Audio playback completed.");
                stopAudio();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("AudioPlay", "MediaPlayer error: what=" + what + ", extra=" + extra);
                Toast.makeText(this, "Audio play error", Toast.LENGTH_SHORT).show();
                stopAudio();
                return true;
            });

            Log.d("AudioPlay", "Preparing MediaPlayer asynchronously...");
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e("AudioPlay", "Exception during playback: " + e.getMessage(), e);
            Toast.makeText(this, "Audio play error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isAudioPlaying = false;
            audioPlayButton.setImageResource(R.drawable.ic_playu);
        }
    }

    private void stopAudio() {
        Log.d("AudioPlay", "stopAudio called");
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignore) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isAudioPlaying = false;
        audioPlayButton.setImageResource(R.drawable.ic_playu);
        if (audioDurationText != null) audioDurationText.setText("");
        if (audioSeekBar != null) {
            audioSeekBar.setProgress(0);
            audioSeekBar.setEnabled(false);
        }
        seekBarHandler.removeCallbacksAndMessages(null);
    }

    private void updateAudioDuration() {
        if (mediaPlayer != null && audioDurationText != null) {
            int durationMs = mediaPlayer.getDuration();
            String durationStr = formatDuration(durationMs);
            Log.d("AudioPlay", "Audio duration: " + durationStr);
            audioDurationText.setText("00:00 / " + durationStr);
        }
    }

    private void startSeekBarUpdate() {
        seekBarHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isAudioPlaying && !isUserSeeking) {
                    int pos = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    if (duration > 0) {
                        int progress = (int) (100f * pos / duration);
                        audioSeekBar.setProgress(progress);
                        if (audioDurationText != null)
                            audioDurationText.setText(formatDuration(pos) + " / " + formatDuration(duration));
                    }
                    seekBarHandler.postDelayed(this, 400); // update every 400ms
                }
            }
        }, 0);
    }

    private String formatDuration(int ms) {
        int seconds = ms / 1000;
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    @Override
    protected void onDestroy() {
        Log.d("AudioPlay", "onDestroy: releasing MediaPlayer.");
        super.onDestroy();
        stopAudio();
    }

    @Override
    public void onBackPressed() {
        Log.d("AudioPlay", "onBackPressed: stopping audio and exiting.");
        stopAudio();
        super.onBackPressed();
    }
}
