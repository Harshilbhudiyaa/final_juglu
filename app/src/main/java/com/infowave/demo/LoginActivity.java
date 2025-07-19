package com.infowave.demo;

import android.annotation.SuppressLint;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.infowave.demo.supabase.SupabaseClient;
import com.infowave.demo.supabase.UsersRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText etMobile;
    private Button btnSubmit;
    private BottomSheetDialog otpBottomSheet;
    private View dimView;
    private WindowManager windowManager;
    TextView register_page;
    private String lastMobileForOtp = "";

    @SuppressLint("SetTextI18n")
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
                v.setPadding(left, top, right, bottom);
                return insets.consumeSystemWindowInsets();
            }
        });
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        etMobile = findViewById(R.id.etLoginMobile);
        btnSubmit = findViewById(R.id.btnLoginSubmit);
        register_page = findViewById(R.id.register_page);

        register_page.setOnClickListener(V -> {
            Intent i = new Intent(LoginActivity.this, Register.class);
            startActivity(i);
        });

        btnSubmit.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            if (mobile.isEmpty() || mobile.length() != 10) {
                etMobile.setError("Enter valid 10-digit mobile number");
            } else {
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Sending OTP...");
                sendOtpAndSaveToSupabase(mobile,
                        () -> {
                            runOnUiThread(() -> {
                                btnSubmit.setEnabled(true);
                                btnSubmit.setText("Send OTP");
                                lastMobileForOtp = mobile;
                                showOtpBottomSheet(mobile);
                            });
                        },
                        () -> {
                            runOnUiThread(() -> {
                                btnSubmit.setEnabled(true);
                                btnSubmit.setText("Send OTP");
                            });
                        }
                );
            }
        });
    }

    // ======= OTP via NINZASMS and Save to Supabase =======
    private void sendOtpAndSaveToSupabase(String mobile, Runnable onSuccess, Runnable onFailure) {
        String otp = String.valueOf((int) (Math.random() * 900000 + 100000));
        Log.d("LOGIN_OTP", "Generated OTP for " + mobile + ": " + otp);

        String smsUrl = "https://ninzasms.in.net/auth/send_sms";
        JSONObject smsBody = new JSONObject();
        try {
            smsBody.put("sender_id", "15155");
            smsBody.put("variables_values", otp);
            smsBody.put("numbers", mobile);
        } catch (JSONException e) {
            Log.e("LOGIN_OTP", "JSON creation error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (onFailure != null) onFailure.run();
            return;
        }

        Log.d("LOGIN_OTP", "Sending OTP via NinzaSMS: " + smsBody.toString());

        JsonObjectRequest smsReq = new JsonObjectRequest(Request.Method.POST, smsUrl, smsBody,
                response -> {
                    Log.d("LOGIN_OTP", "NinzaSMS Response: " + response.toString());
                    saveOtpToSupabase(mobile, otp, onSuccess, onFailure);
                },
                error -> {
                    String err = error.networkResponse != null
                            ? new String(error.networkResponse.data)
                            : error.toString();
                    Log.e("LOGIN_OTP", "NinzaSMS ERROR: " + err);
                    Toast.makeText(this, "SMS sending failed: " + err, Toast.LENGTH_LONG).show();
                    if (onFailure != null) onFailure.run();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> h = new java.util.HashMap<>();
                h.put("Content-Type", "application/json");
                h.put("Authorization", "NINZASMSf6e2000ba91482e5cc0116b1b2bf1bc20818fd77297c02ae50e9a70b");
                return h;
            }
        };
        smsReq.setRetryPolicy(new DefaultRetryPolicy(5000, 0, 1f));
        SupabaseClient.addToRequestQueue(this, smsReq);
    }

    private void saveOtpToSupabase(String mobile, String otp, Runnable onSuccess, Runnable onFailure) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/phone_otps?on_conflict=phone";
        JSONObject body = new JSONObject();
        try {
            body.put("phone", mobile);
            body.put("otp", otp);
        } catch (JSONException e) {
            Log.e("LOGIN_OTP", "JSON error (supabase): " + e.getMessage());
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (onFailure != null) onFailure.run();
            return;
        }
        Log.d("LOGIN_OTP", "Saving OTP to Supabase for " + mobile + ": " + otp);

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("LOGIN_OTP", "Supabase OTP save response: " + response);
                    if (onSuccess != null) onSuccess.run();
                },
                error -> {
                    String err = error.networkResponse != null
                            ? new String(error.networkResponse.data)
                            : error.toString();
                    Log.e("LOGIN_OTP", "Supabase OTP SAVE ERROR: " + err);
                    Toast.makeText(this, "DB Save failed: " + err, Toast.LENGTH_SHORT).show();
                    if (onFailure != null) onFailure.run();
                }
        ) {
            @Override
            public byte[] getBody() { return body.toString().getBytes(); }
            @Override
            public String getBodyContentType() { return "application/json"; }
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> h = SupabaseClient.getHeaders(LoginActivity.this);
                h.put("Prefer", "resolution=merge-duplicates");
                return h;
            }
        };
        SupabaseClient.addToRequestQueue(this, req);
    }

    // ====== OTP BottomSheet and Login Logic ======
    @SuppressLint("SetTextI18n")
    private void showOtpBottomSheet(String mobile) {
        otpBottomSheet = new BottomSheetDialog(this);
        @SuppressLint("InflateParams") View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_otp, null);
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
        Button btnResend = bottomSheetView.findViewById(R.id.btnResendCode);
        TextView tvTimer = bottomSheetView.findViewById(R.id.tvTimer);

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
                otp.append(Objects.requireNonNull(input.getText()).toString());
            }
            if (otp.length() != 6) {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("LOGIN_OTP", "Verifying OTP: " + otp + " for " + mobile);

            btnVerify.setEnabled(false);
            btnVerify.setText("Verifying...");
            UsersRepository.loginWithOtp(this, mobile, otp.toString(), new UsersRepository.UserCallback() {
                @Override
                public void onSuccess(String userId) {
                    Log.d("LOGIN_OTP", "Login successful for " + mobile + " userId: " + userId);
                    btnVerify.setEnabled(true);
                    btnVerify.setText("Verify");
                    if (otpBottomSheet != null && otpBottomSheet.isShowing()) otpBottomSheet.dismiss();
                    if (dimView != null && dimView.getParent() != null) windowManager.removeView(dimView);
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, Main.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onFailure(String error) {
                    Log.e("LOGIN_OTP", "Login failed for " + mobile + " error: " + error);
                    btnVerify.setEnabled(true);
                    btnVerify.setText("Verify");
                    Toast.makeText(LoginActivity.this, "Login Failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        btnResend.setOnClickListener(v -> {
            btnResend.setEnabled(false);
            btnResend.setText("Resending...");
            Log.d("LOGIN_OTP", "Resending OTP for " + mobile);
            sendOtpAndSaveToSupabase(mobile,
                    () -> {
                        runOnUiThread(() -> {
                            Log.d("LOGIN_OTP", "OTP resent for " + mobile);
                            Toast.makeText(LoginActivity.this, "OTP resent", Toast.LENGTH_SHORT).show();
                            btnResend.setEnabled(true);
                            btnResend.setText("Resend");
                            startTimer(tvTimer, btnResend);
                        });
                    },
                    () -> {
                        runOnUiThread(() -> {
                            Log.e("LOGIN_OTP", "OTP resend failed for " + mobile);
                            Toast.makeText(LoginActivity.this, "Resend failed", Toast.LENGTH_SHORT).show();
                            btnResend.setEnabled(true);
                            btnResend.setText("Resend");
                        });
                    }
            );
        });

        otpBottomSheet.setOnDismissListener(dialog -> {
            if (dimView != null && dimView.getParent() != null) {
                windowManager.removeView(dimView);
            }
        });

        otpBottomSheet.show();
        startTimer(tvTimer, btnResend);
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

    private void startTimer(TextView tv, Button btn) {
        btn.setEnabled(false);
        new android.os.CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long ms) { tv.setText("Resend in: " + (ms / 1000) + "s"); }
            @Override
            public void onFinish() { btn.setEnabled(true); tv.setText(""); }
        }.start();
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
