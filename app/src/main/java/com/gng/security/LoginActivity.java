package com.gng.security; // Replace with your actual package name

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// NOTE: All Firebase imports are kept for easy re-enabling
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn, registerBtn;
    private ImageButton googleLogin;
    // private FirebaseAuth mAuth; // Temporarily disabled
    // private GoogleSignInClient mGoogleSignInClient; // Temporarily disabled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Start of Temporary Bypass ---
        // Check if user is already "logged in" using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("GnGSecurityPrefs", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        // --- End of Temporary Bypass ---

        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();

        // mAuth = FirebaseAuth.getInstance(); // Temporarily disabled
        // setupGoogleSignIn(); // Temporarily disabled
    }

    private void initializeViews() {
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        googleLogin = findViewById(R.id.googleLogin);
    }

    private void setupClickListeners() {
        loginBtn.setOnClickListener(v -> showLoginDialog());
        registerBtn.setOnClickListener(v -> showRegisterDialog());
        googleLogin.setOnClickListener(v -> signInWithGoogle());
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Login to GnG Security");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null);
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        builder.setView(dialogView);
        builder.setPositiveButton("Login", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            performLogin(email, password);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Account");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        EditText fullNameInput = dialogView.findViewById(R.id.fullNameInput);
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        builder.setView(dialogView);
        builder.setPositiveButton("Register", (dialog, which) -> {
            String fullName = fullNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            performRegistration(fullName, email, password, confirmPassword);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void performLogin(String email, String password) {
        // --- Start of Temporary Bypass ---
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the dummy login state
        SharedPreferences sharedPreferences = getSharedPreferences("GnGSecurityPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        // Proceed to main activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
        // --- End of Temporary Bypass ---

        /* --- Original Firebase Logic ---
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(...);
        */
    }

    private void performRegistration(String fullName, String email, String password, String confirmPassword) {
        // --- Start of Temporary Bypass ---
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the dummy login state
        SharedPreferences sharedPreferences = getSharedPreferences("GnGSecurityPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        // Proceed to main activity
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
        // --- End of Temporary Bypass ---

        /* --- Original Firebase Logic ---
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(...);
        */
    }

    private void signInWithGoogle() {
        // --- Start of Temporary Bypass ---
        // Save the dummy login state
        SharedPreferences sharedPreferences = getSharedPreferences("GnGSecurityPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        // Proceed to main activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
        // --- End of Temporary Bypass ---

        /* --- Original Firebase Logic ---
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        */
    }
    
    /* --- Original Firebase methods are left below for easy re-enabling ---
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { ... }

    private void firebaseAuthWithGoogle(String idToken) { ... }
    
    private void setupGoogleSignIn() { ... }
    */
}
