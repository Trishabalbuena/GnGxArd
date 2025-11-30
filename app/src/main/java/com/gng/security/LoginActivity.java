package com.gng.security; // Replace with your actual package name

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn, registerBtn;
    private ImageButton googleLogin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            boolean isPasswordProvider = false;
            for (UserInfo userInfo : currentUser.getProviderData()) {
                if (EmailAuthProvider.PROVIDER_ID.equals(userInfo.getProviderId())) {
                    isPasswordProvider = true;
                    break;
                }
            }

            if (!isPasswordProvider || currentUser.isEmailVerified()) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
        setupGoogleSignIn();
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

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Log.d(TAG, "signInWithEmail:success");
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure - email not verified");
                            Toast.makeText(LoginActivity.this, "Please check your inbox to verify your email address.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void performRegistration(String fullName, String email, String password, String confirmPassword) {
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                if(profileTask.isSuccessful()){
                                    Log.d(TAG, "User profile updated.");
                                }
                            });

                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Log.d(TAG, "Verification email sent.");
                                            Toast.makeText(LoginActivity.this, "Registration successful. Verification email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                                        } else {
                                            Log.w(TAG, "sendEmailVerification:failure", verificationTask.getException());
                                            Toast.makeText(LoginActivity.this, "Registration successful, but failed to send verification email.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google Sign In failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Firebase authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
