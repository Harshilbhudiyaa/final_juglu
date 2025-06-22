package com.infowave.demo;

import android.content.DialogInterface;
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

public class PrivacyCenterActivity extends AppCompatActivity {

    SwitchCompat switchSearch, switchReceipts, switchActivity;
    TextView profileVisibility, deleteAccount;
    LinearLayout visibilityRow, blockedRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_center);
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
        switchSearch = findViewById(R.id.switch_search_visibility);
        switchReceipts = findViewById(R.id.switch_read_receipts);
        switchActivity = findViewById(R.id.switch_activity_status);

        profileVisibility = findViewById(R.id.tv_profile_visibility);
        visibilityRow = findViewById(R.id.privacy_visibility_row);
        blockedRow = findViewById(R.id.blocked_users_row);
        deleteAccount = findViewById(R.id.delete_account);

        // Visibility selection
        visibilityRow.setOnClickListener(v -> {
            String[] options = {"Everyone", "Only Matches", "Nobody"};
            new AlertDialog.Builder(this)
                    .setTitle("Who can see your profile?")
                    .setItems(options, (dialog, which) -> {
                        profileVisibility.setText(options[which]);
                    })
                    .show();
        });

        // Blocked users - placeholder
        blockedRow.setOnClickListener(v -> {
            // TODO: Start BlockedUsersActivity
        });

        // Delete account
        deleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // TODO: Handle account deletion
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
