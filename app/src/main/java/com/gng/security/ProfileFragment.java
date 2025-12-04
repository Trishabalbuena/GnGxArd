package com.gng.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView nameTextView, emailTextView;
    private RelativeLayout changePasswordButton;
    private CircleImageView profileImageView;
    private ImageView addProfileImageButton, editNameButton;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfileImage(imageUri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

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
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        profileImageView = view.findViewById(R.id.profileImage);
        addProfileImageButton = view.findViewById(R.id.addProfileImage);
        editNameButton = view.findViewById(R.id.editName);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logoutUser());

        Button deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountConfirmationDialog());

        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        addProfileImageButton.setOnClickListener(v -> openGallery());

        editNameButton.setOnClickListener(v -> showEditNameDialog());

        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            emailTextView.setText(user.getEmail());

            // Load Profile Image
            if (user.getPhotoUrl() != null && getContext() != null) {
                Glide.with(getContext()).load(user.getPhotoUrl()).into(profileImageView);
            }

            // Load Display Name
            String displayName = user.getDisplayName();
            if (!TextUtils.isEmpty(displayName)) {
                nameTextView.setText(displayName);
            } else if (user.getEmail() != null && user.getEmail().contains("@")) {
                nameTextView.setText(user.getEmail().split("@")[0]);
            } else {
                nameTextView.setText("User");
            }

            // Check provider and set button visibility
            boolean isPasswordProvider = false;
            for (UserInfo userInfo : user.getProviderData()) {
                if (EmailAuthProvider.PROVIDER_ID.equals(userInfo.getProviderId())) {
                    isPasswordProvider = true;
                    break;
                }
            }
            changePasswordButton.setVisibility(isPasswordProvider ? View.VISIBLE : View.GONE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Toast.makeText(getContext(), "Uploading photo...", Toast.LENGTH_SHORT).show();

        StorageReference profileImageRef = mStorage.getReference().child("profile_images/" + user.getUid() + ".jpg");

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserProfilePhoto(uri);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserProfilePhoto(Uri photoUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUrl)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
                loadUserProfile(); // Reload to show the new image
            } else {
                Toast.makeText(getContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditNameDialog(){
        if(getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Display Name");

        final EditText input = new EditText(getContext());
        input.setText(nameTextView.getText());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if(!newName.isEmpty()){
                updateUserName(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserName(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Name updated!", Toast.LENGTH_SHORT).show();
                loadUserProfile(); // Reload to show the new name
            } else {
                Toast.makeText(getContext(), "Failed to update name.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showChangePasswordDialog() { 
        // ... (code for this is already correct)
    }

    private void reauthenticateAndUpdatePassword(String oldPassword, String newPassword, AlertDialog dialog) {
        // ... (code for this is already correct)
    }

    private void logoutUser() {
        // ... (code for this is already correct)
    }

    private void showDeleteAccountConfirmationDialog() {
        // ... (code for this is already correct)
    }

    private void deleteAccount() {
        // ... (code for this is already correct)
    }
}
