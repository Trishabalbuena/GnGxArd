package com.gng.security;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// This activity is the first screen the user sees.
public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        // Find the views
        CheckBox agreeCheckbox = findViewById(R.id.agreeCheckbox);
        Button disagreeBtn = findViewById(R.id.disagreeBtn);
        Button agreeBtn = findViewById(R.id.agreeBtn);

        // Set up the click listener for the AGREE button
        agreeBtn.setOnClickListener(v -> {
            if (agreeCheckbox.isChecked()) {
                // If the user agrees, go to the LoginActivity.
                Intent intent = new Intent(TermsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close this screen so the user can't come back to it
            } else {
                // If they haven't checked the box, show a message.
                Toast.makeText(this, "Please read and agree to the terms", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the click listener for the DISAGREE button
        disagreeBtn.setOnClickListener(v -> {
            finish(); // Close the app if they disagree.
        });
    }
}
