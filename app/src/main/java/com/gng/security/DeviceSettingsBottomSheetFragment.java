package com.gng.security;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DeviceSettingsBottomSheetFragment extends BottomSheetDialogFragment {

    private String deviceName;
    private DeviceInteractionListener listener;
    private FirebaseAuth mAuth;

    public static DeviceSettingsBottomSheetFragment newInstance(String deviceName) {
        DeviceSettingsBottomSheetFragment fragment = new DeviceSettingsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("deviceName", deviceName);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(DeviceInteractionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            deviceName = getArguments().getString("deviceName");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_settings_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchMaterial notificationsSwitch = view.findViewById(R.id.notifications_switch);
        
        // Load saved state for this device
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);
        boolean isEnabled = sharedPreferences.getBoolean("notification_enabled_" + deviceName, true); // Default to true
        notificationsSwitch.setChecked(isEnabled);

        // Save state on change
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notification_enabled_" + deviceName, isChecked);
            editor.apply();
        });

        Button disconnectButton = view.findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(v -> {
            removeDevice(deviceName);
            Toast.makeText(getContext(), deviceName + " disconnected", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onDeviceRemoved(deviceName);
            }
            dismiss();
        });

        Button resetPincodeButton = view.findViewById(R.id.reset_pincode_button);
        resetPincodeButton.setOnClickListener(v -> showResetPincodeDialog());
    }

    private void showResetPincodeDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reset_pincode_with_password, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText passwordInput = dialogView.findViewById(R.id.account_password_input);
        EditText newPincodeInput = dialogView.findViewById(R.id.new_pincode_input);
        EditText confirmNewPincodeInput = dialogView.findViewById(R.id.confirm_new_pincode_input);
        Button saveButton = dialogView.findViewById(R.id.save_new_pincode_button);

        saveButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString();
            String newPincode = newPincodeInput.getText().toString();
            String confirmPincode = confirmNewPincodeInput.getText().toString();

            if (password.isEmpty() || newPincode.isEmpty() || confirmPincode.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPincode.length() != 4) {
                Toast.makeText(getContext(), "New pincode must be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPincode.equals(confirmPincode)) {
                Toast.makeText(getContext(), "New pincodes do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            reauthenticateAndResetPincode(password, newPincode, dialog);
        });

        dialog.show();
    }

    private void reauthenticateAndResetPincode(String password, String newPincode, AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getContext(), "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                savePincode(deviceName, newPincode);
                Toast.makeText(getContext(), "Pincode for " + deviceName + " has been reset.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                dismiss(); // Dismiss the bottom sheet as well
            } else {
                Toast.makeText(getContext(), "Authentication failed. Please check your password.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeDevice(String deviceName) {
        FirebaseUser user = mAuth.getCurrentUser();
        Context context = getContext();
        if (context == null || user == null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("deviceList_" + user.getUid(), null);
        if (json == null) {
            return;
        }
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> deviceList = new Gson().fromJson(json, type);
        if (deviceList != null) {
            deviceList.remove(deviceName);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String newJson = new Gson().toJson(deviceList);
            editor.putString("deviceList_" + user.getUid(), newJson);
            editor.remove("pincode_" + user.getUid() + "_" + deviceName); // Also remove the pincode
            editor.apply();
        }
    }

    private void savePincode(String deviceName, String pincode) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (getContext() == null || user == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("pincode_" + user.getUid() + "_" + deviceName, pincode);
        editor.apply();
    }
}
