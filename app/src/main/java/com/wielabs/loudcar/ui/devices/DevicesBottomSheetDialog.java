package com.wielabs.loudcar.ui.devices;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wielabs.loudcar.databinding.FragmentDeviceBottomSheetBinding;
import com.wielabs.loudcar.ui.MainActivityViewModel;
import com.wielabs.loudcar.ui.finddevice.DeviceAdapter;
import com.wielabs.loudcar.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DevicesBottomSheetDialog extends BottomSheetDialogFragment implements DeviceAdapter.DeviceAdapterListener {
    private FragmentDeviceBottomSheetBinding binding;
    private DeviceAdapter deviceAdapter;

    @Inject
    MainActivityViewModel mainActivityViewModel;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeviceBottomSheetBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mainActivityViewModel.isBluetoothEnabled())
            mainActivityViewModel.scanClassicDevices();

        deviceAdapter = new DeviceAdapter(new DeviceAdapter.DeviceDiffUtil(), this);
        binding.classicDevicesRv.setAdapter(deviceAdapter);

        mainActivityViewModel.getClassicDevices().observe(getViewLifecycleOwner(), classicDevices -> {
            Set<String> devAddresses = new HashSet<>();
            ArrayList<BluetoothDevice> devices = new ArrayList<>();
            for (BluetoothDevice device : classicDevices)
                if (!devAddresses.contains(device.getAddress()) && device.getName() != null && device.getName().startsWith("led")) {
                    devAddresses.add(device.getAddress());
                    devices.add(device);
                }

            deviceAdapter.submitList(devices);
        });
    }

    @Override
    public void onDeviceClicked(BluetoothDevice device) {
        if (mainActivityViewModel.isBluetoothEnabled()) {
            sharedPreferencesUtil.setLastKnownAddress(device.getAddress());
            new ConnectThread(device).start();
            dismiss();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Stop scanning for classic devices and clear scanned devices.
        mainActivityViewModel.getmClassicDevices().clear();
        mainActivityViewModel.stopScanningForClassicDevices();
    }
}
