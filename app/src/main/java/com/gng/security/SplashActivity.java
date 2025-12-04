package com.gng.security;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("GnGSecurityPrefs", MODE_PRIVATE);
            boolean hasAgreedToTerms = sharedPreferences.getBoolean("hasAgreedToTerms", false);

            if (!hasAgreedToTerms) {
                // If user has never agreed to terms, they must go to TermsActivity first.
                startActivity(new Intent(SplashActivity.this, TermsActivity.class));
                finish();
                return;
            }

            // If terms have been agreed to, check login status.
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                // User is already logged in, go to the main activity.
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // User is not logged in, go to the login activity.
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1000); // 1 second delay
    }
}
