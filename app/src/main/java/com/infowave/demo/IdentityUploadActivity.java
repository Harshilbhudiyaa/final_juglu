package com.infowave.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class IdentityUploadActivity extends AppCompatActivity {

    private static final int PICK_PROFILE_IMAGE = 101;
    private static final int PICK_ID_PROOF = 102;

    private ImageView imgProfile, imgIdProof;
    private Button btnPickProfile, btnPickIdProof, btnContinue;

    private boolean isProfileUploaded = false;
    private boolean isIdUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity_document_upload);

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


        imgProfile = findViewById(R.id.imgProfile);
        imgIdProof = findViewById(R.id.imgIdProof);
        btnPickProfile = findViewById(R.id.btnPickProfile);
        btnPickIdProof = findViewById(R.id.btnPickIdProof);
        btnContinue = findViewById(R.id.btnContinue);

        // Start with Continue button disabled
        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.6f);

        btnPickProfile.setOnClickListener(v -> pickImage(PICK_PROFILE_IMAGE));
        btnPickIdProof.setOnClickListener(v -> pickImage(PICK_ID_PROOF));

        btnContinue.setOnClickListener(v -> {
            // On continue, move to MainActivity
            startActivity(new Intent(IdentityUploadActivity.this, Main.class));
            finish();
        });
    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                if (requestCode == PICK_PROFILE_IMAGE) {
                    imgProfile.setImageBitmap(bitmap);
                    isProfileUploaded = true;
                } else if (requestCode == PICK_ID_PROOF) {
                    imgIdProof.setImageBitmap(bitmap);
                    isIdUploaded = true;
                }
                // Enable the continue button if both images are uploaded
                checkBothImagesUploaded();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkBothImagesUploaded() {
        boolean enabled = isProfileUploaded && isIdUploaded;
        btnContinue.setEnabled(enabled);
        btnContinue.setAlpha(enabled ? 1f : 0.6f);
    }
}
