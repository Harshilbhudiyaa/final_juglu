package com.infowave.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

public class GenderSelectionActivity extends AppCompatActivity {

    private CardView cardMale, cardFemale;
    private Button btnContinue;
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_selection);

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


        cardMale = findViewById(R.id.cardMale);
        cardFemale = findViewById(R.id.cardFemale);
        btnContinue = findViewById(R.id.btnContinue);

        // Start disabled, faded out
        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.7f);

        cardMale.setOnClickListener(v -> selectGender("Male", cardMale));
        cardFemale.setOnClickListener(v -> selectGender("Female", cardFemale));

        btnContinue.setOnClickListener(v -> {
            if (!selectedGender.isEmpty()) {
                Intent intent = new Intent(GenderSelectionActivity.this, LookingForActivity.class);
                if (getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }
                intent.putExtra("gender", selectedGender);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            } else {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectGender(String gender, CardView selectedCard) {
        selectedGender = gender;

        // Reset all cards
        resetCard(cardMale);
        resetCard(cardFemale);

        // Highlight selected card
        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected));
        selectedCard.setCardElevation(12f);

        // Pulse animation
        selectedCard.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        selectedCard.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start()
                ).start();

        // Enable and animate button
        btnContinue.setEnabled(true);
        btnContinue.animate().alpha(1f).setDuration(300).start();
    }

    private void resetCard(CardView card) {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_normal));
        card.setCardElevation(8f);
    }
}
