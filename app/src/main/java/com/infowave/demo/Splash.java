package com.infowave.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        Handler h = new Handler();
        h.postDelayed(() -> {
            Intent in = new Intent(Splash.this, Register.class);
            startActivity(in);
        },3000);
    }
}