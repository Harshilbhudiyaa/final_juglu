package com.infowave.demo;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class AppPreferencesActivity extends AppCompatActivity {

    private TextView selectedLanguageTextView;
    private final String[] languages = {
            "English", "Hindi", "Gujarati", "Spanish", "French", "German", "Chinese"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_preferences);
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
        selectedLanguageTextView = findViewById(R.id.tv_selected_language);
        LinearLayout languageSelector = findViewById(R.id.language_selector);

        SwitchCompat switchAutoplay = findViewById(R.id.switch_autoplay);
        SwitchCompat switchWifi = findViewById(R.id.switch_wifi_download);
        SwitchCompat switchPersonalization = findViewById(R.id.switch_personalization);

        // Language selection
        languageSelector.setOnClickListener(v -> showLanguageDialog());
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");

        builder.setItems(languages, (dialog, which) -> {
            selectedLanguageTextView.setText(languages[which]);
            // Save to SharedPreferences if needed
        });

        builder.show();
    }
}
