package com.gng.security;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView nameTextView, emailTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In to get the client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        if (getActivity() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        }

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameTextView = view.findViewById(R.id.name);
        emailTextView = view.findViewById(R.id.email);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logoutUser());

        Button deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountConfirmationDialog());

        loadUserProfile();
    }

    private void loadUserProfile(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            // Set the email address
            emailTextView.setText(user.getEmail());

            // Set the name based on the rules
            String displayName = user.getDisplayName();
            if(!TextUtils.isEmpty(displayName)){
                // If user has a display name (from registration or Google), use it
                nameTextView.setText(displayName);
            } else if (user.getEmail() != null && user.getEmail().contains("@")){
                // If no display name, derive it from the email (for Google sign-in fallback)
                String emailName = user.getEmail().split("@")[0];
                nameTextView.setText(emailName);
            } else {
                // Fallback if no information is available
                nameTextView.setText("User");
            }
        }
    }

    private void logoutUser() {
        if (getActivity() == null) return;

        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> {
                Log.d(TAG, "Google Sign-out successful.");
            });
        }

        // Redirect to LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void showDeleteAccountConfirmationDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "No user found to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Here you would also delete user data from your database (e.g. device list and pincodes)

        user.delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User account deleted.");
                        Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                        logoutUser(); // Logout after successful deletion
                    } else {
                        Log.w(TAG, "Error deleting account.", task.getException());
                        Toast.makeText(getContext(), "Failed to delete account. Please try logging in again before deleting.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
