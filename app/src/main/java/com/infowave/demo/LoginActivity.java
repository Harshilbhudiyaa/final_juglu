package com.infowave.demo;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private EditText etMobile;
    private Button btnSubmit;
    private BottomSheetDialog otpBottomSheet;
    private View dimView;
    private WindowManager windowManager;
    TextView register_page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        etMobile = findViewById(R.id.etLoginMobile);
        btnSubmit = findViewById(R.id.btnLoginSubmit);
        register_page = findViewById(R.id.register_page);

        register_page.setOnClickListener(V->{
            Intent i = new Intent(LoginActivity.this, Register.class);
            startActivity(i);
        });

        btnSubmit.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            if (mobile.isEmpty() || mobile.length() != 10) {
                etMobile.setError("Enter valid 10-digit mobile number");
            } else {
                showOtpBottomSheet(mobile);
            }
        });
    }

    private void showOtpBottomSheet(String mobile) {
        otpBottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_otp, null);
        otpBottomSheet.setContentView(bottomSheetView);

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
            Log.e("Login", "Error adding dim view", e);
        }

        TextView tvPhoneNumber = bottomSheetView.findViewById(R.id.tvPhoneNumber);
        Button btnVerify = bottomSheetView.findViewById(R.id.btnVerify);

        TextInputEditText[] otpInputs = new TextInputEditText[]{
                bottomSheetView.findViewById(R.id.otp1),
                bottomSheetView.findViewById(R.id.otp2),
                bottomSheetView.findViewById(R.id.otp3),
                bottomSheetView.findViewById(R.id.otp4),
                bottomSheetView.findViewById(R.id.otp5),
                bottomSheetView.findViewById(R.id.otp6)
        };

        tvPhoneNumber.setText("+91 " + mobile);
        setupOtpInputs(otpInputs);

        btnVerify.setOnClickListener(v -> {
            StringBuilder otp = new StringBuilder();
            for (TextInputEditText input : otpInputs) {
                otp.append(input.getText().toString());
            }

            if (otp.length() != 6) {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            otpBottomSheet.dismiss();
            Intent intent = new Intent(LoginActivity.this, Main.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        otpBottomSheet.setOnDismissListener(dialog -> {
            if (dimView != null && dimView.getParent() != null) {
                windowManager.removeView(dimView);
            }
        });

        otpBottomSheet.show();
    }

    private void setupOtpInputs(TextInputEditText[] otpInputs) {
        for (int i = 0; i < otpInputs.length; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].requestFocus();
                    }
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
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