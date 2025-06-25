package com.infowave.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.media3.common.MediaItem;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.Executor;

public class StoryUploadActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private PreviewView previewView;
    private ImageView captureButton, imagePreview, btnGallery, btnBack;
    private TextView btnUpload, btnRetake;
    private VideoView videoPreview;

    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording activeRecording;
    private boolean isImageVisible = false;

    private File lastCapturedVideoFile = null;

    private Executor cameraExecutor;
    private ProcessCameraProvider cameraProvider;

    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        showSelectedMedia(selectedImage);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_upload);
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
        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.captureButton);
        imagePreview = findViewById(R.id.imagePreview);
        playerView = findViewById(R.id.playerView);
        btnGallery = findViewById(R.id.btnGallery);
        btnBack = findViewById(R.id.btnBack);
        btnUpload = findViewById(R.id.btnUpload);
        btnRetake = findViewById(R.id.btnRetake);

        cameraExecutor = ContextCompat.getMainExecutor(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }

        captureButton.setOnClickListener(v -> {
            if (isImageVisible) {
                resetToCamera();
            } else {
                takePhoto();
            }
        });

        captureButton.setOnLongClickListener(v -> {
            startVideoRecording();
            return true;
        });

        captureButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                stopVideoRecording();
            }
            return false;
        });

        btnGallery.setOnClickListener(v -> openGallery());
        btnBack.setOnClickListener(v -> finish());

        btnUpload.setOnClickListener(v -> {
            if (isImageVisible) {
                Toast.makeText(this, "Uploading media...", Toast.LENGTH_SHORT).show();
                // TODO: Upload logic here
            } else {
                Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show();
            }
        });

        btnRetake.setOnClickListener(v -> {
            resetToCamera();
            if (lastCapturedVideoFile != null && lastCapturedVideoFile.exists()) {
                lastCapturedVideoFile.delete();
                lastCapturedVideoFile = null;
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void showSelectedMedia(Uri uri) {
        String path = uri.toString();

        if (path.endsWith(".mp4")) {
            if (exoPlayer != null) {
                exoPlayer.release();
            }

            playerView.setVisibility(View.VISIBLE);
            imagePreview.setVisibility(View.GONE);
            previewView.setVisibility(View.GONE);
            btnRetake.setVisibility(View.VISIBLE);

            exoPlayer = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(exoPlayer);

            MediaItem mediaItem = MediaItem.fromUri(uri);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
            exoPlayer.prepare();
            exoPlayer.play();

            lastCapturedVideoFile = new File(uri.getPath());
        } else {
            imagePreview.setImageURI(uri);
            imagePreview.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            previewView.setVisibility(View.GONE);
            btnRetake.setVisibility(View.VISIBLE);
        }

        isImageVisible = true;
    }


    private void resetToCamera() {
        imagePreview.setVisibility(View.GONE);
        playerView.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.GONE);
        isImageVisible = false;

        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCamera();
            } catch (Exception e) {
                Toast.makeText(this, "Camera failed", Toast.LENGTH_SHORT).show();
            }
        }, cameraExecutor);
    }

    private void bindCamera() {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build();

        videoCapture = VideoCapture.withOutput(recorder);

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
    }

    private void takePhoto() {
        File photoFile = new File(getExternalFilesDir(null),
                "story_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Uri uri = Uri.fromFile(photoFile);
                        runOnUiThread(() -> showSelectedMedia(uri));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        runOnUiThread(() -> Toast.makeText(StoryUploadActivity.this, "Capture failed", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void startVideoRecording() {
        if (videoCapture == null || activeRecording != null) return;

        File file = new File(getExternalFilesDir(null),
                "video_" + System.currentTimeMillis() + ".mp4");

        FileOutputOptions outputOptions = new FileOutputOptions.Builder(file).build();

        PendingRecording pendingRecording = videoCapture.getOutput()
                .prepareRecording(this, outputOptions);

        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(this),
                videoEvent -> {
                    if (videoEvent instanceof VideoRecordEvent.Finalize) {
                        VideoRecordEvent.Finalize finalize = (VideoRecordEvent.Finalize) videoEvent;
                        Uri uri = Uri.fromFile(file);
                        runOnUiThread(() -> showSelectedMedia(uri));
                    }
                });
    }

    private void stopVideoRecording() {
        if (activeRecording != null) {
            activeRecording.stop();
            activeRecording = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) cameraProvider.unbindAll();
        if (exoPlayer != null) exoPlayer.release();
    }

}
