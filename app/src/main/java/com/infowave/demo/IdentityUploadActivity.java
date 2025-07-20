package com.infowave.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infowave.demo.supabase.MediaUploadRepository;
import com.infowave.demo.supabase.SupabaseClient;
import com.infowave.demo.supabase.UsersRepository;

import java.io.IOException;

public class IdentityUploadActivity extends AppCompatActivity {

    private static final int PICK_PROFILE_IMAGE = 101;
    private ImageView imgProfile;
    private Button btnPickProfile, btnContinue;
    private boolean isProfileUploaded = false;
    private String userId = "";

    // Use the anon key for public/test uploads (production: never use service key on client!)
    private final String SUPABASE_BEARER_TOKEN = SupabaseClient.getAnonKey();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity_document_upload);

        View decoreview = getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener((v, insets) -> {
            int left = insets.getSystemWindowInsetLeft();
            int top = insets.getSystemWindowInsetTop();
            int right = insets.getSystemWindowInsetRight();
            int bottom = insets.getSystemWindowInsetBottom();
            v.setPadding(left, top, right, bottom);
            return insets.consumeSystemWindowInsets();
        });

        imgProfile = findViewById(R.id.imgProfile);
        btnPickProfile = findViewById(R.id.btnPickProfile);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.6f);

        userId = getIntent().getStringExtra("userId");
        Log.d("IDENTITY_FLOW", "userId from Intent: " + userId);

        btnPickProfile.setOnClickListener(v -> pickImage(PICK_PROFILE_IMAGE));

        btnContinue.setOnClickListener(v -> {
            Log.d("IDENTITY_FLOW", "Continue clicked, userId: " + userId);
            getSharedPreferences("YourApp", MODE_PRIVATE).edit().putString("userId", userId).apply();
            startActivity(new Intent(IdentityUploadActivity.this, Main.class));
            finish();
        });
    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestCode);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                if (requestCode == PICK_PROFILE_IMAGE) {
                    imgProfile.setImageBitmap(bitmap);

                    btnContinue.setEnabled(false);
                    btnContinue.setAlpha(0.6f);
                    btnContinue.setText("Uploading...");

                    Log.d("IDENTITY_FLOW", "Starting image upload to storage for userId: " + userId + ", uri: " + selectedImageUri);

                    MediaUploadRepository.uploadProfileImage(
                            this,
                            selectedImageUri,
                            userId,
                            new MediaUploadRepository.ImageUploadCallback() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onSuccess(String publicUrl) {
                                    Log.d("IDENTITY_FLOW", "Image upload success! Public URL: " + publicUrl);

                                    runOnUiThread(() -> btnContinue.setText("Saving..."));

                                    UsersRepository.updateProfileImage(
                                            IdentityUploadActivity.this,
                                            userId,
                                            publicUrl,
                                            new UsersRepository.UserCallback() {
                                                @Override
                                                public void onSuccess(String msg) {
                                                    Log.d("IDENTITY_FLOW", "Profile image URL saved to users table: " + msg);
                                                    runOnUiThread(() -> {
                                                        isProfileUploaded = true;
                                                        btnContinue.setEnabled(true);
                                                        btnContinue.setAlpha(1f);
                                                        btnContinue.setText("Continue");
                                                        Toast.makeText(IdentityUploadActivity.this, "Profile uploaded!", Toast.LENGTH_SHORT).show();
                                                    });
                                                }

                                                @Override
                                                public void onFailure(String error) {
                                                    Log.e("IDENTITY_FLOW", "DB save failed: " + error);
                                                    runOnUiThread(() -> {
                                                        isProfileUploaded = false;
                                                        btnContinue.setEnabled(false);
                                                        btnContinue.setAlpha(0.6f);
                                                        btnContinue.setText("Continue");
                                                        Toast.makeText(IdentityUploadActivity.this, "DB save failed: " + error, Toast.LENGTH_LONG).show();
                                                    });
                                                }
                                            }
                                    );
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e("IDENTITY_FLOW", "Image upload failed: " + error);
                                    runOnUiThread(() -> {
                                        isProfileUploaded = false;
                                        btnContinue.setEnabled(false);
                                        btnContinue.setAlpha(0.6f);
                                        btnContinue.setText("Continue");
                                        Toast.makeText(IdentityUploadActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                    );
                }
            } catch (IOException e) {
                Log.e("IDENTITY_FLOW", "Image decode error: " + e.getMessage(), e);
                Toast.makeText(this, "Image Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (resultCode != RESULT_OK) {
            Log.e("IDENTITY_FLOW", "Image selection canceled or failed: resultCode=" + resultCode);
            Toast.makeText(this, "Image selection canceled.", Toast.LENGTH_SHORT).show();
        }
    }
}
