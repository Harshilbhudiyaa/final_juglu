package com.infowave.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.infowave.demo.supabase.ProfileRepository;
import com.infowave.demo.supabase.MediaUploadRepository;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    CircleImageView editProfileImage;
    EditText editFullName, editUsername, editEmail, editMobile, editBio;
    TextView genderText;
    SwitchMaterial privateSwitch;
    MaterialButton btnSaveProfile;
    ImageView backIcon;

    Uri imageUri = null;
    String currentImageUrl = "";
    String userId = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        View decoreview = getWindow().getDecorView();
        decoreview.setOnApplyWindowInsetsListener((v, insets) -> {
            int left = insets.getSystemWindowInsetLeft();
            int top = insets.getSystemWindowInsetTop();
            int right = insets.getSystemWindowInsetRight();
            int bottom = insets.getSystemWindowInsetBottom();
            v.setPadding(left, top, right, bottom);
            return insets.consumeSystemWindowInsets();
        });

        editProfileImage = findViewById(R.id.edit_profile_image);
        editFullName = findViewById(R.id.edit_full_name);
        editUsername = findViewById(R.id.edit_username);
        editMobile = findViewById(R.id.edit_mobile);
        editEmail = findViewById(R.id.edit_email);
        genderText = findViewById(R.id.gender_text);
        privateSwitch = findViewById(R.id.switch_private);
        editBio = findViewById(R.id.edit_bio);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        backIcon = findViewById(R.id.back_icon);

        editMobile.setEnabled(false);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            userId = getSharedPreferences("juglu_prefs", Context.MODE_PRIVATE).getString("user_id", null);
        }
        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchAndPrefillProfile();

        backIcon.setOnClickListener(v -> finish());
        editProfileImage.setOnClickListener(v -> openGallery());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void fetchAndPrefillProfile() {
        ProfileRepository.getLoggedInUserProfile(this, userId, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(ProfileRepository.Profile user) {
                editFullName.setText(user.fullName != null ? user.fullName : "");
                editUsername.setText(user.username != null ? user.username : "");
                editBio.setText(user.bio != null ? user.bio : "");
                editMobile.setText(user.phone != null ? user.phone : "");
                editEmail.setText(user.email != null ? user.email : "");

                // Gender is readonly as TextView
                genderText.setText(
                        user.gender != null && !user.gender.isEmpty() ?
                                capitalize(user.gender) : "Not specified"
                );

                privateSwitch.setChecked(user.isPrivate != null && user.isPrivate);

                currentImageUrl = user.imageUrl != null ? user.imageUrl : "";
                if (!currentImageUrl.isEmpty()) {
                    Glide.with(EditProfileActivity.this)
                            .load(currentImageUrl)
                            .placeholder(R.drawable.image1)
                            .into(editProfileImage);
                } else {
                    editProfileImage.setImageResource(R.drawable.image1);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load profile: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                editProfileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile() {
        final String fullName = editFullName.getText().toString().trim();
        final String username = editUsername.getText().toString().trim();
        final String bio = editBio.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String gender = genderText.getText().toString().trim().toLowerCase(); // Readonly
        final Boolean isPrivate = privateSwitch.isChecked();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveProfile.setEnabled(false);

        if (imageUri != null) {
            MediaUploadRepository.uploadProfileImage(this, imageUri, userId, new MediaUploadRepository.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    updateProfileInSupabase(fullName, username, email, bio, imageUrl, gender, isPrivate);
                }
                @Override
                public void onFailure(String error) {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            updateProfileInSupabase(fullName, username, email, bio, currentImageUrl, gender, isPrivate);
        }
    }

    private void updateProfileInSupabase(String fullName, String username, String email, String bio, String imageUrl,
                                         String gender, Boolean isPrivate) {
        ProfileRepository.updateUserProfile(this, userId, fullName, username, email, bio, imageUrl, gender, isPrivate,
                new ProfileRepository.UpdateProfileCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EditProfileActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // To refresh ProfileFragment
                        finish();
                    }
                    @Override
                    public void onFailure(String error) {
                        btnSaveProfile.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Helper: capitalize first letter (for gender display)
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
