package com.gng.security;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        TextView termsTextView = findViewById(R.id.terms_text_view);
        CheckBox agreeCheckbox = findViewById(R.id.agree_checkbox);
        Button agreeButton = findViewById(R.id.agree_btn);
        Button disagreeButton = findViewById(R.id.disagree_btn);

        String termsText = "Welcome to GnG Security.\n\n" +
                "This declaration and these Terms of Service (\"Terms\") govern your use of the GnG Security mobile application (the \"App\"). By checking the box and continuing, you acknowledge that you have read, understood, and agree to be bound by these Terms.\n\n" +
                "1. User Accounts\n" +
                "You are responsible for all activity that occurs under your account. You must safeguard your password and keep your account information current. Do not share your account credentials with others.\n\n" +
                "2. Required Hardware\n" +
                "The GnG Security App is designed to function exclusively with its corresponding hardware device. You are responsible for obtaining and maintaining this hardware. The App provides no functionality without it.\n\n" +
                "3. Acceptable Use\n" +
                "You agree not to use the App for any illegal purpose or to misuse it in any way. This includes, but is not limited to, attempting to interfere with the service, accessing data you are not authorized to access, or attempting to circumvent security features.\n\n" +
                "4. Limitation of Liability\n" +
                "The App is provided on an \"AS IS\" basis. While it is a tool designed to assist with security, we do not guarantee that it will prevent all potential damage, loss, or unauthorized access. We are not liable for any damages or losses that may arise from your use of the App or its connected hardware.\n\n" +
                "5. Termination\n" +
                "We reserve the right to suspend or terminate your account at any time, without notice, if you violate these Terms.\n\n" +
                "6. Changes to Terms\n" +
                "We may modify these Terms at any time. We will provide notice of changes, and your continued use of the App after the changes become effective will constitute your acceptance of the new Terms.";

        termsTextView.setText(termsText);

        agreeButton.setOnClickListener(v -> {
            if (agreeCheckbox.isChecked()) {
                // User has agreed, save this preference
                SharedPreferences sharedPreferences = getSharedPreferences("GnGSecurityPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("hasAgreedToTerms", true);
                editor.apply();

                // Proceed to the login screen
                startActivity(new Intent(TermsActivity.this, LoginActivity.class));
                finish();
            } else {
                // User has not agreed, show a message
                Toast.makeText(TermsActivity.this, "You must agree to the terms and conditions to continue.", Toast.LENGTH_SHORT).show();
            }
        });

        disagreeButton.setOnClickListener(v -> {
            // Exit the app if user disagrees
            finishAffinity();
        });
    }
}
