package com.infowave.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class HelpCenterActivity extends AppCompatActivity {

    LinearLayout faqLayout, faqContainer, privacyLayout, guidelinesLayout, contactSupportLayout, reportLayout;
    TextView question1, answer1, question2, answer2, question3, answer3, question4, answer4, question5, answer5;

    boolean isFaqVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);
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
        // Initialize all views
        faqLayout = findViewById(R.id.layout_faq);
        faqContainer = findViewById(R.id.faq_container);
        privacyLayout = findViewById(R.id.layout_privacy);
        guidelinesLayout = findViewById(R.id.layout_guidelines);
        contactSupportLayout = findViewById(R.id.layout_contact);
        reportLayout = findViewById(R.id.layout_report);

        // Questions and Answers
        question1 = findViewById(R.id.question1);
        answer1 = findViewById(R.id.answer1);
        question2 = findViewById(R.id.question2);
        answer2 = findViewById(R.id.answer2);
        question3 = findViewById(R.id.question3);
        answer3 = findViewById(R.id.answer3);
        question4 = findViewById(R.id.question4);
        answer4 = findViewById(R.id.answer4);
        question5 = findViewById(R.id.question5);
        answer5 = findViewById(R.id.answer5);

        // Toggle FAQ visibility
        faqLayout.setOnClickListener(v -> {
            isFaqVisible = !isFaqVisible;
            faqContainer.setVisibility(isFaqVisible ? View.VISIBLE : View.GONE);
        });

        // Toggle each answer visibility on question click
        question1.setOnClickListener(v -> toggleVisibility(answer1));
        question2.setOnClickListener(v -> toggleVisibility(answer2));
        question3.setOnClickListener(v -> toggleVisibility(answer3));
        question4.setOnClickListener(v -> toggleVisibility(answer4));
        question5.setOnClickListener(v -> toggleVisibility(answer5));

        // Other layout actions
        privacyLayout.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://yourapp.com/privacy")));
        });

        guidelinesLayout.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://yourapp.com/community-guidelines")));
        });

        contactSupportLayout.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@yourapp.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help Needed");
            startActivity(Intent.createChooser(emailIntent, "Contact Support"));
        });

        reportLayout.setOnClickListener(v -> {
            // TODO: Implement your Report Problem Activity here
        });
    }

    private void toggleVisibility(TextView answer) {
        if (answer.getVisibility() == View.VISIBLE) {
            answer.setVisibility(View.GONE);
        } else {
            // Hide all other answers
            answer1.setVisibility(View.GONE);
            answer2.setVisibility(View.GONE);
            answer3.setVisibility(View.GONE);
            answer4.setVisibility(View.GONE);
            answer5.setVisibility(View.GONE);
            // Show the selected one
            answer.setVisibility(View.VISIBLE);
        }
    }
}
