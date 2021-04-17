package com.wielabs.loudcar.ui.finddevice;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.wielabs.loudcar.R;
import com.wielabs.loudcar.databinding.FragmentFindDevicesBinding;
import com.wielabs.loudcar.ui.MainActivityViewModel;
import com.wielabs.loudcar.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FindDevicesFragment extends Fragment implements DeviceAdapter.DeviceAdapterListener {
    private FragmentFindDevicesBinding binding;
    private DeviceAdapter deviceAdapter;
    @Inject
    MainActivityViewModel mainActivityViewModel;
    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    private final BroadcastReceiver bondStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                int prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bondState != prevBondState) {
                    Snackbar.make(binding.getRoot(), device.getName() + " selected for hand less operation", Snackbar.LENGTH_SHORT)
                            .setAnchorView((requireActivity()).findViewById(R.id.bottom_nav)).show();
                    sharedPreferencesUtil.setLastKnownAddress(device.getAddress());
                    mainActivityViewModel.scanClassicDevices();
                }
            }
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d("FindDevicesFragment", "onConnectionStateChange() - " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED)
                gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("FindDevicesFragment", "onServicesDiscovered()");
            for (BluetoothGattService gattService : gatt.getServices()) {
                Log.d("Service", gattService + "");
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    Log.d("Characteristic", gattCharacteristic + "");
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("FindDevicesFragment", characteristic + "");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("FindDevicesFragment", "onCharacteristicChanged() - " + characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFindDevicesBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().registerReceiver(bondStateChangeReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        // Scan for LE devices.
        mainActivityViewModel.scanClassicDevices();

        deviceAdapter = new DeviceAdapter(new DeviceAdapter.DeviceDiffUtil(), this);
        binding.bluetoothDevicesRecyclerView.setAdapter(deviceAdapter);
        mainActivityViewModel.getClassicDevices().observe(getViewLifecycleOwner(), bluetoothDevices -> {
            Set<String> devAddresses = new HashSet<>();
            ArrayList<BluetoothDevice> devices = new ArrayList<>();
            for (BluetoothDevice device : bluetoothDevices)
                if (!devAddresses.contains(device.getAddress()) && device.getName() != null && device.getName().startsWith("led")) {
                    devAddresses.add(device.getAddress());
                    devices.add(device);
                }

            deviceAdapter.submitList(devices);
        });
    }

    @Override
    public void onDeviceClicked(BluetoothDevice device) {
        //device.connectGatt(requireContext(), true, gattCallback);
        if (device.getBondState() == BluetoothDevice.BOND_NONE)
            device.createBond();
        else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            Snackbar.make(binding.getRoot(), device.getName() + " selected for hand less operation", Snackbar.LENGTH_SHORT)
                    .setAnchorView((requireActivity()).findViewById(R.id.bottom_nav)).show();
            sharedPreferencesUtil.setLastKnownAddress(device.getAddress());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        requireActivity().unregisterReceiver(bondStateChangeReceiver);
    }
}
