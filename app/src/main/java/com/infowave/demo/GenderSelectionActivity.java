package com.infowave.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.infowave.demo.supabase.UsersRepository;

public class GenderSelectionActivity extends AppCompatActivity {

    private CardView cardMale, cardFemale;
    private Button btnContinue;
    private String selectedGender = "";
    private String userId = "";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_selection);

        cardMale = findViewById(R.id.cardMale);
        cardFemale = findViewById(R.id.cardFemale);
        btnContinue = findViewById(R.id.btnContinue);

        // Get userId from intent, check for null
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "User ID missing. Please restart registration.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.7f);

        cardMale.setOnClickListener(v -> selectGender("male", cardMale));
        cardFemale.setOnClickListener(v -> selectGender("female", cardFemale));

        btnContinue.setOnClickListener(v -> {
            if (!selectedGender.isEmpty()) {
                btnContinue.setEnabled(false);
                String originalText = btnContinue.getText().toString();
                btnContinue.setText("Saving...");

                // This call is now secure via JWT (see UsersRepository.updateGender)
                UsersRepository.updateGender(
                        GenderSelectionActivity.this,
                        userId,
                        selectedGender,
                        new UsersRepository.UserCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                Intent intent = new Intent(GenderSelectionActivity.this, IdentityUploadActivity.class);
                                intent.putExtra("userId", userId);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                finish();
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(GenderSelectionActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                                btnContinue.setEnabled(true);
                                btnContinue.setText(originalText);
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectGender(String gender, CardView selectedCard) {
        selectedGender = gender;

        resetCard(cardMale);
        resetCard(cardFemale);

        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_selected));
        selectedCard.setCardElevation(12f);

        selectedCard.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> selectedCard.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                ).start();

        btnContinue.setEnabled(true);
        btnContinue.animate().alpha(1f).setDuration(300).start();
    }

    private void resetCard(CardView card) {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_normal));
        card.setCardElevation(8f);
    }
}
