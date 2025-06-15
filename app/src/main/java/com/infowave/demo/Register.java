package com.infowave.demo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class Register extends AppCompatActivity {

    private EditText etUsername, etEmail, etMobile, etPassword, etDOB, etBio;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_basic);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etDOB = findViewById(R.id.etDOB);
        etBio = findViewById(R.id.etBio);
        btnNext = findViewById(R.id.btnNext);

        // Date of Birth picker (no keyboard popup)
        etDOB.setFocusable(false);
        etDOB.setClickable(true);
        etDOB.setOnClickListener(v -> showDatePickerDialog());

        btnNext.setOnClickListener(v -> {
            Log.d("BUTTON", "Next button clicked");
            if (validateForm()) {
                Log.d("NAVIGATION", "Validation passed, starting GenderSelectionActivity");
                try {
                    Intent intent = new Intent(Register.this, GenderSelectionActivity.class);
                    intent.putExtra("username", etUsername.getText().toString());
                    intent.putExtra("email", etEmail.getText().toString());
                    intent.putExtra("mobile", etMobile.getText().toString());
                    intent.putExtra("password", etPassword.getText().toString());
                    intent.putExtra("dob", etDOB.getText().toString());
                    intent.putExtra("bio", etBio.getText().toString());

                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (Exception e) {
                    Log.e("NAVIGATION", "Error starting activity", e);
                    Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d("VALIDATION", "Validation failed");
                Toast.makeText(Register.this, "Please fix the errors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDOB.setText(date);
                },
                year, month, day
        );

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean validateForm() {
        boolean valid = true;
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Reset errors
        etUsername.setError(null);
        etEmail.setError(null);
        etMobile.setError(null);
        etPassword.setError(null);
        etDOB.setError(null);

        // Username validation
        if (etUsername.getText().toString().trim().isEmpty()) {
            etUsername.setError("Username is required");
            valid = false;
        }

        // Email validation
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            valid = false;
        }

        // Mobile validation
        if (mobile.isEmpty()) {
            etMobile.setError("Mobile number is required");
            valid = false;
        } else if (mobile.length() != 10) {
            etMobile.setError("Valid 10-digit number required");
            valid = false;
        }

        // Password validation
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            valid = false;
        }

        // DOB validation
        if (etDOB.getText().toString().trim().isEmpty()) {
            etDOB.setError("Date of birth is required");
            valid = false;
        }

        return valid;
    }
}
