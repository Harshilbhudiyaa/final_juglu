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

        Handler h = new Handler();
        h.postDelayed(() -> {
            // Check JWT token in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
            String jwt = prefs.getString("jwt_token", null);

            Intent in;
            if (jwt != null && !jwt.isEmpty()) {
                in = new Intent(Splash.this, Main.class); // Redirect to home
            } else {
                in = new Intent(Splash.this, Register.class); // Redirect to register
            }
            startActivity(in);
            finish();
        }, 3000);
    }
}
