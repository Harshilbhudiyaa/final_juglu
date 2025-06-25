package com.infowave.demo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class Register extends AppCompatActivity {

    private EditText etUsername, etEmail, etMobile, etPassword, etDOB, etBio;
    private Button btnNext;
    private BottomSheetDialog otpBottomSheet;
    TextView login_txt;
    private View dimView;
    private WindowManager windowManager;
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_basic);
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

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        etDOB = findViewById(R.id.etDOB);
        etBio = findViewById(R.id.etBio);
        btnNext = findViewById(R.id.btnNext);
        login_txt = findViewById(R.id.login);

        // Date of Birth picker (no keyboard popup)
        etDOB.setFocusable(false);
        etDOB.setClickable(true);
        etDOB.setOnClickListener(v -> showDatePickerDialog());

        btnNext.setOnClickListener(v -> {
            Log.d("BUTTON", "Next button clicked");
            if (validateForm()) {
                Log.d("NAVIGATION", "Validation passed, showing OTP bottom sheet");
                showOtpBottomSheet();
            } else {
                Log.d("VALIDATION", "Validation failed");
                Toast.makeText(Register.this, "Please fix the errors", Toast.LENGTH_SHORT).show();
            }
        });

        login_txt.setOnClickListener(V->{
            Intent i = new Intent(Register.this, LoginActivity.class);
            startActivity(i);
        });
    }

    @SuppressLint("SetTextI18n")
    private void showOtpBottomSheet() {
        otpBottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_otp, null);
        otpBottomSheet.setContentView(bottomSheetView);

        // Set up blur effect
        dimView = new View(this);
        dimView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black));
        dimView.setAlpha(0.6f);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;

        try {
            windowManager.addView(dimView, params);
        } catch (WindowManager.BadTokenException e) {
            Log.e("Register", "Error adding dim view", e);
            // If we can't add the dim view, continue without it
        }

        // Initialize OTP views
        TextInputEditText[] otpInputs = new TextInputEditText[]{
                bottomSheetView.findViewById(R.id.otp1),
                bottomSheetView.findViewById(R.id.otp2),
                bottomSheetView.findViewById(R.id.otp3),
                bottomSheetView.findViewById(R.id.otp4),
                bottomSheetView.findViewById(R.id.otp5),
                bottomSheetView.findViewById(R.id.otp6)
        };

        TextView tvPhoneNumber = bottomSheetView.findViewById(R.id.tvPhoneNumber);
        Button btnVerify = bottomSheetView.findViewById(R.id.btnVerify);
        Button btnResendCode = bottomSheetView.findViewById(R.id.btnResendCode);
        TextView tvTimer = bottomSheetView.findViewById(R.id.tvTimer);

        // Set phone number
        tvPhoneNumber.setText("+91 " + etMobile.getText().toString());

        // Set up OTP input handling
        setupOtpInputs(otpInputs);

        // Set up button click listeners
        btnVerify.setOnClickListener(v -> verifyOtp(otpInputs));
        btnResendCode.setOnClickListener(v -> resendOtp(tvTimer, btnResendCode));

        // Start timer
        startTimer(tvTimer, btnResendCode);

        // Show bottom sheet
        otpBottomSheet.show();

        // Handle dismiss
        otpBottomSheet.setOnDismissListener(dialog -> {
            if (dimView != null && dimView.getParent() != null) {
                windowManager.removeView(dimView);
            }
        });
    }

    private void setupOtpInputs(TextInputEditText[] otpInputs) {
        for (int i = 0; i < otpInputs.length; i++) {
            final int currentIndex = i;
            otpInputs[i].addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpInputs.length - 1) {
                        otpInputs[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void startTimer(TextView tvTimer, Button btnResendCode) {
        btnResendCode.setEnabled(false);
        new android.os.CountDownTimer(30000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Resend code in: " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                btnResendCode.setEnabled(true);
                tvTimer.setText("");
            }
        }.start();
    }

    private void verifyOtp(TextInputEditText[] otpInputs) {
        StringBuilder otp = new StringBuilder();
        for (TextInputEditText input : otpInputs) {
            otp.append(input.getText().toString());
        }

        if (otp.length() != 6) {
            Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement actual OTP verification logic here
        // For now, we'll just proceed to the next screen
        if (otpBottomSheet != null && otpBottomSheet.isShowing()) {
            otpBottomSheet.dismiss();
        }
        Intent intent = new Intent(Register.this, GenderSelectionActivity.class);
        intent.putExtra("username", etUsername.getText().toString());
        intent.putExtra("email", etEmail.getText().toString());
        intent.putExtra("mobile", etMobile.getText().toString());
        intent.putExtra("password", etPassword.getText().toString());
        intent.putExtra("dob", etDOB.getText().toString());
        intent.putExtra("bio", etBio.getText().toString());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void resendOtp(TextView tvTimer, Button btnResendCode) {
        // TODO: Implement actual OTP resend logic here
        Toast.makeText(this, "OTP resent to " + etMobile.getText().toString(), Toast.LENGTH_SHORT).show();
        startTimer(tvTimer, btnResendCode);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpBottomSheet != null && otpBottomSheet.isShowing()) {
            otpBottomSheet.dismiss();
        }
        if (dimView != null && dimView.getParent() != null) {
            windowManager.removeView(dimView);
        }
    }
}
