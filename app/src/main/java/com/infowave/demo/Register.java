package com.infowave.demo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import com.infowave.demo.R;

import java.util.Calendar;

public class Register extends AppCompatActivity {

    EditText etFullName, etUsername, etEmail, etMobile, etPassword, etDOB, etBio;
    RadioGroup rgGender, rgLookingFor;
    Button btnUploadProfile, btnUploadID, btnRegister;
    ImageView imgProfilePreview, imgIDPreview;

    Uri profileImageUri = null;
    Uri idProofUri = null;

    ActivityResultLauncher<String> profilePickerLauncher;
    ActivityResultLauncher<String> idProofPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupImagePickers();
        setupDOBPicker();

        btnRegister.setOnClickListener(view -> {
            if (validateForm()) {
                // You can proceed with your backend API submission here.
                Toast.makeText(this, "Validation Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etDOB = findViewById(R.id.etDOB);
        etBio = findViewById(R.id.etBio);
        rgGender = findViewById(R.id.rgGender);
        rgLookingFor = findViewById(R.id.rgLookingFor);
        btnUploadProfile = findViewById(R.id.btnUploadProfile);
        btnUploadID = findViewById(R.id.btnUploadID);
        btnRegister = findViewById(R.id.btnRegister);
        imgProfilePreview = findViewById(R.id.imgProfilePreview);
        imgIDPreview = findViewById(R.id.imgIDPreview);

        profilePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                profileImageUri = uri;
                btnUploadProfile.setText("Profile Photo Selected");
                imgProfilePreview.setImageURI(uri);  // show preview
            }
        });

        idProofPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                idProofUri = uri;
                btnUploadID.setText("ID Proof Selected");
                imgIDPreview.setImageURI(uri);  // show preview
            }
        });

    }

    private void setupImagePickers() {
        profilePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                profileImageUri = uri;
                btnUploadProfile.setText("Profile Photo Selected");
                imgProfilePreview.setImageURI(uri);
            }
        });


        idProofPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                idProofUri = uri;
                btnUploadID.setText("ID Proof Selected");
            }
        });

        btnUploadProfile.setOnClickListener(view -> profilePickerLauncher.launch("image/*"));
        btnUploadID.setOnClickListener(view -> idProofPickerLauncher.launch("image/*"));
    }

    private void setupDOBPicker() {
        etDOB.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(Register.this,
                    (view, year1, month1, dayOfMonth) -> etDOB.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                    year, month, day);
            dp.show();
        });
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(etFullName.getText())) {
            etFullName.setError("Enter full name");
            return false;
        }
        if (TextUtils.isEmpty(etUsername.getText())) {
            etUsername.setError("Enter username");
            return false;
        }
        if (TextUtils.isEmpty(etEmail.getText())) {
            etEmail.setError("Enter email");
            return false;
        }
        if (TextUtils.isEmpty(etMobile.getText())) {
            etMobile.setError("Enter mobile number");
            return false;
        }
        if (TextUtils.isEmpty(etPassword.getText())) {
            etPassword.setError("Enter password");
            return false;
        }
        if (TextUtils.isEmpty(etDOB.getText())) {
            etDOB.setError("Select date of birth");
            return false;
        }
        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Select gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (rgLookingFor.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Select Looking For option", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etBio.getText())) {
            etBio.setError("Enter your bio");
            return false;
        }
        if (profileImageUri == null) {
            Toast.makeText(this, "Select profile photo", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (idProofUri == null) {
            Toast.makeText(this, "Select ID proof", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
