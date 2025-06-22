package com.infowave.demo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowInsets;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    CircleImageView editProfileImage;
    EditText editFullName, editUsername, editDob, editLocation, editInstagram, editBio;
    MaterialButton btnSaveProfile;
    ImageView backIcon;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
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

        // Initialize views
        editProfileImage = findViewById(R.id.edit_profile_image);

        editUsername = findViewById(R.id.edit_username);
        editDob = findViewById(R.id.edit_dob);
        editLocation = findViewById(R.id.edit_location);
        editInstagram = findViewById(R.id.edit_instagram);
        editBio = findViewById(R.id.edit_bio);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        backIcon = findViewById(R.id.back_icon);

        // Back icon action
        backIcon.setOnClickListener(v -> finish());

        // Image selection
        editProfileImage.setOnClickListener(v -> openGallery());

        // Date picker for DOB
        editDob.setOnClickListener(v -> showDatePickerDialog());

        // Save button
        btnSaveProfile.setOnClickListener(v -> saveProfile());
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

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String dob = String.format("%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
            editDob.setText(dob);
        }, year, month, day).show();
    }

    private void saveProfile() {

        String username = editUsername.getText().toString().trim();
        String dob = editDob.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String insta = editInstagram.getText().toString().trim();
        String bio = editBio.getText().toString().trim();

        // Validate fields (optional)
        if (username.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Username are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Save to backend or local DB
        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
