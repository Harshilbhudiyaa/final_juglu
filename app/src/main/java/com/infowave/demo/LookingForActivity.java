package com.infowave.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class LookingForActivity extends AppCompatActivity {

    private CardView cardBoyfriend, cardGirlfriend;
    private Button btnSkip, btnConfirm;
    private ImageView boyfriendCheck, girlfriendCheck;
    private String selectedOption = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looking_for);

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

        // Make status bar translucent
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        cardBoyfriend = findViewById(R.id.cardBoyfriend);
        cardGirlfriend = findViewById(R.id.cardGirlfriend);
        btnSkip = findViewById(R.id.btnSkip);
        btnConfirm = findViewById(R.id.btnConfirm);
        boyfriendCheck = findViewById(R.id.boyfriendCheck);
        girlfriendCheck = findViewById(R.id.girlfriendCheck);

        cardBoyfriend.setOnClickListener(v -> selectOption("Boyfriend", cardBoyfriend, boyfriendCheck));
        cardGirlfriend.setOnClickListener(v -> selectOption("Girlfriend", cardGirlfriend, girlfriendCheck));

        btnSkip.setOnClickListener(v -> navigateToHome(""));
        btnConfirm.setOnClickListener(v -> navigateToHome(selectedOption));
    }

    private void selectOption(String option, CardView card, ImageView check) {
        selectedOption = option;

        // Reset all cards
        resetCard(cardBoyfriend, boyfriendCheck);
        resetCard(cardGirlfriend, girlfriendCheck);

        // Highlight selected card
        card.setCardElevation(12);
        check.setVisibility(View.VISIBLE);

        // Animation
        card.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> card.animate().scaleX(1f).scaleY(1f).setDuration(150).start());

        // Enable confirm button
        btnConfirm.setEnabled(true);
        btnConfirm.animate().alpha(1f).setDuration(300).start();
    }

    private void resetCard(CardView card, ImageView check) {
        card.setCardElevation(6);
        card.setScaleX(1f);
        card.setScaleY(1f);
        check.setVisibility(View.INVISIBLE);
    }

    private void navigateToHome(String selection) {
        Intent intent = new Intent(LookingForActivity.this, IdentityUploadActivity.class);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras()); // chain all previous user info
        }
        intent.putExtra("lookingFor", selection); // add this step's info
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}