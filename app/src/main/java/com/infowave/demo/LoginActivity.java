package com.infowave.demo;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.infowave.demo.supabase.UsersRepository;

import java.util.Locale;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText etMobile;
    private Button btnSubmit;
    private BottomSheetDialog otpBottomSheet;
    private View dimView;
    private WindowManager windowManager;
    private TextView registerPage;

    // store generated OTP here
    private String generatedOtp;
    // store userId from Supabase
    private String currentUserId;

    // your NinzaSMS key
    private static final String SMS_API_KEY =
            "NINZASMS907929413f89905b48b267a8bf20646ee8dd803ba924f770854f9d49";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        View decor = getWindow().getDecorView();
        decor.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom()
            );
            return insets.consumeSystemWindowInsets();
        });

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        etMobile     = findViewById(R.id.etLoginMobile);
        btnSubmit    = findViewById(R.id.btnLoginSubmit);
        registerPage = findViewById(R.id.register_page);

        registerPage.setOnClickListener(v ->
                startActivity(new Intent(this, Register.class))
        );

        btnSubmit.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            if (mobile.length() != 10) {
                etMobile.setError("Enter valid 10-digit mobile number");
                return;
            }

            // first check if user exists
            UsersRepository.checkUserExists(this, mobile, new UsersRepository.UserCallback() {
                @Override
                public void onSuccess(String userId) {
                    currentUserId = userId;
                    // then send OTP SMS
                    sendOtpSms(mobile);
                }
                @Override
                public void onFailure(String error) {
                    if ("NOT_FOUND".equals(error)) {
                        // unregistered → go to register
                        startActivity(new Intent(LoginActivity.this, Register.class)
                                .putExtra("phone", mobile)
                        );
                    } else {
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    // step 1: generate & send OTP via NinzaSMS
    private void sendOtpSms(String mobile) {
        generatedOtp = String.format(Locale.US, "%06d", new Random().nextInt(1_000_000));
        String message = "Your Infowave OTP is: " + generatedOtp;

        String url = String.format(
                "https://ninzasms.in.net/auth/send_sms",
                SMS_API_KEY,
                mobile,
                Uri.encode(message)              // ← use this instead
        );


        StringRequest req = new StringRequest(
                Request.Method.GET, url,
                resp -> {
                    // on success, show OTP sheet
                    showOtpBottomSheet(mobile);
                },
                err -> Toast.makeText(this, "SMS failed: " + err.getMessage(), Toast.LENGTH_LONG).show()
        );
        req.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        Volley.newRequestQueue(this).add(req);
    }

    // step 2: show OTP entry bottom sheet
    private void showOtpBottomSheet(String mobile) {
        otpBottomSheet = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_otp, null);
        otpBottomSheet.setContentView(sheet);

        // dim background
        dimView = new View(this);
        dimView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black));
        dimView.setAlpha(0.6f);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        try { windowManager.addView(dimView, lp); }
        catch (WindowManager.BadTokenException e) { Log.e("Login", "dimView failed", e); }

        ((TextView) sheet.findViewById(R.id.tvPhoneNumber)).setText("+91 " + mobile);
        TextInputEditText[] inputs = new TextInputEditText[]{
                sheet.findViewById(R.id.otp1),
                sheet.findViewById(R.id.otp2),
                sheet.findViewById(R.id.otp3),
                sheet.findViewById(R.id.otp4),
                sheet.findViewById(R.id.otp5),
                sheet.findViewById(R.id.otp6)
        };
        setupOtpInputs(inputs);

        sheet.findViewById(R.id.btnVerify).setOnClickListener(v -> {
            StringBuilder entered = new StringBuilder();
            for (TextInputEditText ti : inputs) entered.append(ti.getText().toString());

            if (!entered.toString().equals(generatedOtp)) {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            // OTP matches → go to Main
            otpBottomSheet.dismiss();
            startActivity(new Intent(this, Main.class)
                    .putExtra("userId", currentUserId)
            );
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        otpBottomSheet.setOnDismissListener(d -> {
            if (dimView.getParent() != null) windowManager.removeView(dimView);
        });
        otpBottomSheet.show();
    }

    // auto-move focus between inputs
    private void setupOtpInputs(TextInputEditText[] otpInputs) {
        for (int i = 0; i < otpInputs.length; i++) {
            final int idx = i;
            otpInputs[i].addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int ss, int c, int a){}
                public void afterTextChanged(android.text.Editable s){}
                public void onTextChanged(CharSequence s, int ss, int b, int c) {
                    if (s.length()==1 && idx < otpInputs.length-1)
                        otpInputs[idx+1].requestFocus();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpBottomSheet != null && otpBottomSheet.isShowing())
            otpBottomSheet.dismiss();
        if (dimView != null && dimView.getParent() != null)
            windowManager.removeView(dimView);
    }
}
