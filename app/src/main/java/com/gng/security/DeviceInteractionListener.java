package com.gng.security;

public interface DeviceInteractionListener {
    void onDeviceRemoved(String deviceName);
    void onDisarmClicked(String deviceName);
}
