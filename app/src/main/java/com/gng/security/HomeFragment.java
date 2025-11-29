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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements DeviceInteractionListener {

    private RecyclerView devicesRecyclerView;
    private DeviceAdapter adapter;
    private List<String> deviceList;
    private CardView addDeviceCard;
    private FloatingActionButton addDeviceFab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addDeviceCard = view.findViewById(R.id.addDeviceCard);
        devicesRecyclerView = view.findViewById(R.id.devices_recycler_view);
        addDeviceFab = view.findViewById(R.id.add_device_fab);

        deviceList = getDeviceList();
        adapter = new DeviceAdapter(deviceList, this);

        if (getContext() != null) {
            devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            devicesRecyclerView.setAdapter(adapter);
        }

        if (addDeviceCard != null) {
            addDeviceCard.setOnClickListener(v -> showAddDeviceDialog());
        }
        if (addDeviceFab != null) {
            addDeviceFab.setOnClickListener(v -> showAddDeviceDialog());
        }

        updateUIVisibility();
    }

    @Override
    public void onDisarmClicked(String deviceName) {
        if (getContext() == null || !isAdded()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pincode, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText pincodeInput = dialogView.findViewById(R.id.pin_code_input);
        Button disarmButton = dialogView.findViewById(R.id.disarm_button);

        disarmButton.setOnClickListener(v -> {
            String enteredPin = pincodeInput.getText().toString();
            if (isPincodeCorrect(deviceName, enteredPin)) {
                Toast.makeText(getContext(), deviceName + " disarmed!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private boolean isPincodeCorrect(String deviceName, String enteredPin) {
        if (getContext() == null) return false;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);
        String savedPin = sharedPreferences.getString("pincode_" + deviceName, null);
        return enteredPin.equals(savedPin);
    }

    private void updateUIVisibility() {
        if (deviceList == null) return;
        boolean isEmpty = deviceList.isEmpty();
        if (addDeviceCard != null) {
            addDeviceCard.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (devicesRecyclerView != null) {
            devicesRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (addDeviceFab != null) {
            addDeviceFab.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showAddDeviceDialog() {
        if (getContext() == null || !isAdded()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_device, null);
        builder.setView(dialogView);

        EditText deviceCodeInput = dialogView.findViewById(R.id.deviceCodeInput);
        Button confirmButton = dialogView.findViewById(R.id.confirmButton);

        AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(v -> {
            String deviceCode = deviceCodeInput.getText().toString();
            if (!deviceCode.isEmpty()) {
                dialog.dismiss();
                showRenameDeviceDialog(deviceCode);
            } else {
                Toast.makeText(getContext(), "Please enter a device code", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void showRenameDeviceDialog(String deviceCode) {
        if (getContext() == null || !isAdded()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rename Device");

        EditText renameInput = new EditText(getContext());
        renameInput.setHint("Enter new device name");
        builder.setView(renameInput);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = renameInput.getText().toString().trim();
            if (!newName.isEmpty()) {
                if (deviceList == null) {
                    deviceList = new ArrayList<>();
                }
                deviceList.add(newName);
                saveDeviceList(deviceList);
                if (adapter != null) {
                    adapter.notifyItemInserted(deviceList.size() - 1);
                }
                updateUIVisibility();
                dialog.dismiss();
                showSetPincodeDialog(newName);
            } else {
                Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(false);
        builder.show();
    }

    private void showSetPincodeDialog(String deviceName) {
        if (getContext() == null || !isAdded()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_set_pincode, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText createPincode = dialogView.findViewById(R.id.create_pincode_input);
        EditText confirmPincode = dialogView.findViewById(R.id.confirm_pincode_input);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(v -> {
            String pincode = createPincode.getText().toString();
            String confirm = confirmPincode.getText().toString();

            if (pincode.length() != 4) {
                Toast.makeText(getContext(), "Pincode must be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pincode.equals(confirm)) {
                Toast.makeText(getContext(), "Pincodes do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            savePincode(deviceName, pincode);
            Toast.makeText(getContext(), "Device \"" + deviceName + "\" added successfully!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }

    private void savePincode(String deviceName, String pincode) {
        if (getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("pincode_" + deviceName, pincode);
        editor.apply();
    }

    private List<String> getDeviceList() {
        if (getContext() == null) return new ArrayList<>();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("deviceList", null);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            List<String> list = new Gson().fromJson(json, type);
            if (list == null) {
                throw new JsonSyntaxException("Parsed list is null");
            }
            list.removeAll(Collections.singleton(null));
            return list;
        } catch (Exception e) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("deviceList");
            editor.apply();
            return new ArrayList<>();
        }
    }

    private void saveDeviceList(List<String> list) {
        if (getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(list);
        editor.putString("deviceList", json);
        editor.apply();
    }

    @Override
    public void onDeviceRemoved(String deviceName) {
        if (deviceList == null || !isAdded()) return;
        int index = deviceList.indexOf(deviceName);
        if (index != -1) {
            deviceList.remove(index);
            saveDeviceList(deviceList);
            // Also remove the associated pincode
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("GnGSecurityPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("pincode_" + deviceName);
            editor.apply();
            
            if (adapter != null) {
                adapter.notifyItemRemoved(index);
            }
            updateUIVisibility();
        }
    }
}
