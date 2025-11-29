package com.gng.security;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private final List<String> deviceList;
    private final DeviceInteractionListener listener;

    public DeviceAdapter(List<String> deviceList, DeviceInteractionListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        String deviceName = deviceList.get(position);
        holder.deviceName.setText(deviceName);

        holder.deviceMenu.setOnClickListener(v -> {
            if (v.getContext() instanceof AppCompatActivity) {
                DeviceSettingsBottomSheetFragment bottomSheet = DeviceSettingsBottomSheetFragment.newInstance(deviceName);
                bottomSheet.setListener(listener);
                bottomSheet.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), bottomSheet.getTag());
            }
        });

        holder.disarmButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDisarmClicked(deviceName);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (deviceList == null) {
            return 0;
        }
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        ImageButton deviceMenu;
        ImageButton disarmButton;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceMenu = itemView.findViewById(R.id.device_menu);
            disarmButton = itemView.findViewById(R.id.disarm_button);
        }
    }
}
