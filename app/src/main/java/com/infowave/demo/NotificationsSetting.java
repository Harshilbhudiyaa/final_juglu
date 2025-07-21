package com.infowave.demo;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class NotificationsSetting extends AppCompatActivity {

    private SwitchCompat switchLikes, switchMatches, switchMessages, switchVibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_settings);
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
        switchLikes = findViewById(R.id.switch_likes);
        switchMatches = findViewById(R.id.switch_matches);
        switchMessages = findViewById(R.id.switch_messages);

        switchVibration = findViewById(R.id.switch_vibration);

        // Set default states or load from preferences
        switchLikes.setChecked(true);
        switchMatches.setChecked(true);
        switchMessages.setChecked(true);

        switchVibration.setChecked(true);

        // You can add logic to save these in SharedPreferences or call an API
    }
}
