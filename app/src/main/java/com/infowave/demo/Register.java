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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.android.volley.toolbox.StringRequest;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.infowave.demo.models.User;
import com.infowave.demo.supabase.SupabaseClient;
import com.infowave.demo.supabase.UsersRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText etUsername, etEmail, etMobile, etPassword, etDOB, etBio;
    private Button btnNext;
    private String originalBtnText;
    private BottomSheetDialog otpBottomSheet;
    TextView login_txt;
    private View dimView;
    private WindowManager windowManager;

    @SuppressLint("SetTextI18n")
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
                v.setPadding(left, top, right, bottom);
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

        etDOB.setFocusable(false);
        etDOB.setClickable(true);
        etDOB.setOnClickListener(v -> showDatePickerDialog());

        btnNext.setOnClickListener(v -> {
            if (validateForm()) {
                originalBtnText = btnNext.getText().toString();
                btnNext.setText("Sending OTP...");
                btnNext.setEnabled(false);
                sendOtp();
            } else {
                Toast.makeText(this, "Please fix the errors", Toast.LENGTH_SHORT).show();
            }
        });

        login_txt.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    // ==== 1. Send OTP ====
    private void sendOtp() {
        String mobile = etMobile.getText().toString();
        String otp = String.valueOf((int) (Math.random() * 900000 + 100000));
        Log.d("SEND_OTP", "Generated OTP: " + otp);

//        String url = "https://ninzasms.in.net/auth/send_sms";
//        JSONObject body = new JSONObject();
//        try {
//            body.put("sender_id", "15155");
//            body.put("variables_values", otp);
//            body.put("numbers", mobile);
//        } catch (JSONException e) {
//            Log.e("SEND_OTP", "JSON Error: " + e.getMessage());
//            return;
//        }
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
//                response -> {
//                    Log.d("OTP_SUCCESS", "OTP API Response: " + response.toString());
//                    saveOtpToSupabase(mobile, otp);
//                },
//                error -> {
//                    String err = error.networkResponse != null
//                            ? new String(error.networkResponse.data)
//                            : error.toString();
//                    Log.e("OTP_ERROR", err);
//                    Toast.makeText(this, "OTP failed: " + err, Toast.LENGTH_LONG).show();
//                    btnNext.setText(originalBtnText);
//                    btnNext.setEnabled(true);
//                }
//        ) {
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> h = new HashMap<>();
//                h.put("Content-Type", "application/json");
//                h.put("Authorization", "NINZASMSf6e2000ba91482e5cc0116b1b2bf1bc20818fd77297c02ae50e9a70b");
//                return h;
//            }
//        };
//        request.setRetryPolicy(new DefaultRetryPolicy(5000, 0, 1f));
//        SupabaseClient.addToRequestQueue(this, request);

        // For testing (remove in production)
        saveOtpToSupabase(mobile, otp);
    }

    // ==== 2. Save OTP to Supabase ====
    private void saveOtpToSupabase(String phone, String otp) {
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/phone_otps?on_conflict=phone";
        Log.d("SUPABASE_OTP", "Saving OTP for phone: " + phone);

        JSONObject body = new JSONObject();
        try {
            body.put("phone", phone);
            body.put("otp", otp);
        } catch (JSONException e) {
            Log.e("SUPABASE_OTP", "JSON Error: " + e.getMessage());
            return;
        }

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("SUPABASE_OTP", "Upsert raw response: '" + response + "'");
                    showOtpBottomSheet();
                    btnNext.setText(originalBtnText);
                    btnNext.setEnabled(true);
                },
                error -> {
                    String err = error.networkResponse != null
                            ? new String(error.networkResponse.data)
                            : error.toString();
                    Log.e("SUPABASE_OTP_ERROR", err);
                    Toast.makeText(this, "OTP Save Failed: " + err, Toast.LENGTH_SHORT).show();
                    btnNext.setText(originalBtnText);
                    btnNext.setEnabled(true);
                }
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = SupabaseClient.getHeaders(Register.this);
                h.put("Prefer", "resolution=merge-duplicates");
                return h;
            }
        };
        SupabaseClient.addToRequestQueue(this, req);
    }

    // ==== 3. Show OTP BottomSheet ====
    @SuppressLint("SetTextI18n")
    private void showOtpBottomSheet() {
        otpBottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_otp, null);
        otpBottomSheet.setContentView(view);

        dimView = new View(this);
        dimView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black));
        dimView.setAlpha(0.6f);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        windowManager.addView(dimView, params);

        TextInputEditText[] otpInputs = new TextInputEditText[]{
                view.findViewById(R.id.otp1), view.findViewById(R.id.otp2),
                view.findViewById(R.id.otp3), view.findViewById(R.id.otp4),
                view.findViewById(R.id.otp5), view.findViewById(R.id.otp6)
        };

        TextView tvPhone = view.findViewById(R.id.tvPhoneNumber);
        Button btnVerify = view.findViewById(R.id.btnVerify);
        Button btnResend = view.findViewById(R.id.btnResendCode);
        TextView tvTimer = view.findViewById(R.id.tvTimer);

        tvPhone.setText("+91 " + etMobile.getText().toString());

        setupOtpInputs(otpInputs);
        btnVerify.setOnClickListener(v -> verifyOtp(otpInputs));
        btnResend.setOnClickListener(v -> sendOtp());
        startTimer(tvTimer, btnResend);

        otpBottomSheet.setOnDismissListener(dialog -> {
            if (dimView != null && dimView.getParent() != null)
                windowManager.removeView(dimView);
        });
        otpBottomSheet.show();
    }

    // ==== 4. Verify OTP ====
    private void verifyOtp(TextInputEditText[] otpInputs) {
        StringBuilder sb = new StringBuilder();
        for (TextInputEditText in : otpInputs) sb.append(in.getText().toString());

        String phone = etMobile.getText().toString();
        String url = SupabaseClient.getBaseUrl() + "/rest/v1/phone_otps?phone=eq." + phone + "&select=otp";

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                resp -> {
                    try {
                        if (resp.length() > 0 && resp.getJSONObject(0).getString("otp").equals(sb.toString())) {
                            Log.d("VERIFY_OTP", "OTP matched");
                            otpBottomSheet.dismiss();
                            registerUserWithAuthAndInsertProfile();
                        } else {
                            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("VERIFY_OTP", "Parse error: " + e.getMessage());
                        Toast.makeText(this, "Error verifying OTP", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "OTP check error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return SupabaseClient.getHeaders(Register.this);
            }
        };
        SupabaseClient.addToRequestQueue(this, req);
    }

    // ==== 5. Register with Supabase Auth and Insert user profile ====
    private void registerUserWithAuthAndInsertProfile() {
        User user = new User(
                etUsername.getText().toString(),
                etUsername.getText().toString(),
                etMobile.getText().toString(),
                etPassword.getText().toString(),
                etBio.getText().toString()
        );

        UsersRepository.registerUserWithAuth(this, user, etMobile.getText().toString(), etPassword.getText().toString(), new UsersRepository.UserCallback() {
            @Override
            public void onSuccess(String resp) {
                Log.d("REGISTER", "User registered and profile saved: " + resp);
                // Move to next activity with fetched userId (optional fetchUserIdByPhone)
                UsersRepository.fetchUserIdByPhone(Register.this, etMobile.getText().toString(), new UsersRepository.UserCallback() {
                    @Override
                    public void onSuccess(String userId) {
                        Log.d("REGISTER", "Fetched userId: " + userId);
                        Intent i = new Intent(Register.this, GenderSelectionActivity.class);
                        i.putExtra("userId", userId);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(Register.this, "UserId fetch failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e("REGISTER", "Registration failed: " + error);
                Toast.makeText(Register.this, "Registration failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupOtpInputs(TextInputEditText[] inputs) {
        for (int i = 0; i < inputs.length; i++) {
            final int idx = i;
            inputs[i].addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    if (s.length() == 1 && idx < inputs.length - 1)
                        inputs[idx + 1].requestFocus();
                }
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void startTimer(TextView tv, Button btn) {
        btn.setEnabled(false);
        new android.os.CountDownTimer(30000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long ms) { tv.setText("Resend in: " + (ms / 1000) + "s"); }
            public void onFinish() { btn.setEnabled(true); tv.setText(""); }
        }.start();
    }

    @SuppressLint("SetTextI18n")
    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> etDOB.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private boolean validateForm() {
        boolean valid = true;
        if (etUsername.getText().toString().trim().isEmpty()) {
            etUsername.setError("Username required"); valid = false;
        }
        if (etEmail.getText().toString().trim().isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError("Valid email required"); valid = false;
        }
        if (etMobile.getText().toString().length() != 10) {
            etMobile.setError("10-digit number required"); valid = false;
        }
        if (etPassword.getText().toString().length() < 6) {
            etPassword.setError("Min 6 chars"); valid = false;
        }
        if (etDOB.getText().toString().trim().isEmpty()) {
            etDOB.setError("DOB required"); valid = false;
        }
        return valid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpBottomSheet != null && otpBottomSheet.isShowing()) otpBottomSheet.dismiss();
        if (dimView != null && dimView.getParent() != null) windowManager.removeView(dimView);
    }
}
