package com.infowave.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowInsets;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.infowave.demo.supabase.UsersRepository;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
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

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
            String jwt = prefs.getString("jwt_token", null);
            String refreshToken = prefs.getString("refresh_token", null);

            if (refreshToken != null && !refreshToken.isEmpty()) {
                // Attempt to refresh JWT
                UsersRepository.refreshAccessToken(Splash.this, new UsersRepository.UserCallback() {
                    @Override
                    public void onSuccess(String newJwt) {
                        // Proceed to Main if refresh works
                        Intent in = new Intent(Splash.this, Main.class);
                        startActivity(in);
                        finish();
                    }
                    @Override
                    public void onFailure(String error) {
                        // On failure, clear tokens and redirect to Register
                        UsersRepository.clearTokens(Splash.this);
                        Intent in = new Intent(Splash.this, Register.class);
                        startActivity(in);
                        finish();
                    }
                });
            } else if (jwt != null && !jwt.isEmpty()) {
                // JWT exists (legacy, fallback)
                Intent in = new Intent(Splash.this, Main.class);
                startActivity(in);
                finish();
            } else {
                // No tokens, go to Register
                Intent in = new Intent(Splash.this, Register.class);
                startActivity(in);
                finish();
            }
        }, 1200); // Faster splash, reduce to 1200ms for better UX
    }
}
