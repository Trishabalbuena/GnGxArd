package com.gng.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) return;

        SwitchMaterial notificationsSwitch = view.findViewById(R.id.enable_notifications_switch);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);

        // Load the saved state for the master switch
        boolean isMasterEnabled = sharedPreferences.getBoolean("master_notification_enabled", true); // Default to true
        notificationsSwitch.setChecked(isMasterEnabled);

        // Save state on change
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("master_notification_enabled", isChecked);
            editor.apply();
        });
    }
}
