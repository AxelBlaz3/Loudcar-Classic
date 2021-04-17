package com.wielabs.loudcar.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wielabs.loudcar.model.LedText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainActivityViewModel extends ViewModel {
    boolean mScanning = false;
    public boolean isReverseLedText = false;
    private static final long SCAN_PERIOD = 10000;
    private Handler handler;
    public BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;

    Set<String> leDeviceAddresses = new HashSet();
    public Set<String> classicDeviceAddresses = new HashSet();
    private final ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    if (!leDeviceAddresses.contains(device.getAddress())) {
                        leDeviceAddresses.add(device.getAddress());
                        mLeDevices.add(device);
                        leDevices.setValue(mLeDevices);
                    }
                }
            };
    private final ArrayList<BluetoothDevice> mClassicDevices = new ArrayList();
    private final ArrayList<BluetoothDevice> mLeDevices = new ArrayList();
    private final MutableLiveData<ArrayList<LedText>> previewTexts = new MutableLiveData<>(new ArrayList<>());
    private ArrayList<LedText> _previewTexts = new ArrayList<>();
    private final MutableLiveData<ArrayList<BluetoothDevice>> leDevices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ArrayList<BluetoothDevice>> classicDevices = new MutableLiveData<>(new ArrayList<>());
    private int lastModifiedLedTextIndex = 0;

    @Inject
    MainActivityViewModel() {
        super();
    }

    private final MutableLiveData<Integer> introPosition = new MutableLiveData(-1);

    public ArrayList<BluetoothDevice> getmClassicDevices() {
        return mClassicDevices;
    }

    public LiveData<Integer> getIntroPosition() {
        return introPosition;
    }

    public void setIntroPosition(int newPosition) {
        introPosition.setValue(newPosition);
    }

    public ScanCallback getLeScanCallback() {
        return leScanCallback;
    }

    public LiveData<ArrayList<BluetoothDevice>> getLeDevices() {
        return leDevices;
    }

    public LiveData<ArrayList<BluetoothDevice>> getClassicDevices() {
        return classicDevices;
    }

    public LiveData<ArrayList<LedText>> getPreviewTextsLiveData() {
        return previewTexts;
    }

    public void postClassicDevices(ArrayList<BluetoothDevice> mClassicDevices) {
        classicDevices.postValue(new ArrayList<>(mClassicDevices));
    }

    public int getLastModifiedLedTextIndex() {
        return lastModifiedLedTextIndex;
    }

    public void setLastModifiedLedTextIndex(int newIndex) {
        lastModifiedLedTextIndex = newIndex;
    }

    public boolean isBluetoothEnabled() {
        if (bluetoothAdapter == null)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void toggleBluetooth() {
        if (bluetoothAdapter == null)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            return;
        if (!bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();
        else
            bluetoothAdapter.disable();
    }

    // Start scanning for classic devices.
    public void scanClassicDevices() {
        if (bluetoothAdapter == null)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Clear previously scanned device address if any.
        classicDeviceAddresses.clear();
        Log.d("MainActivityViewModel", bluetoothAdapter.startDiscovery() + "");
    }

    // Stop scanning for classic devices
    public void stopScanningForClassicDevices() {
        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();
    }

    // Start scanning for LE devices.
    public void scanLeDevices() {
        // Perform some null checks.
        if (bluetoothAdapter == null)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothLeScanner == null)
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (handler == null)
            handler = new Handler();

        try {
            if (!mScanning) {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed(() -> {
                    mScanning = false;
                    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())
                        bluetoothLeScanner.stopScan(getLeScanCallback());
                }, SCAN_PERIOD);

                // Clear previously scanned devices if any.
                mLeDevices.clear();
                leDeviceAddresses.clear();
                mScanning = true;
                bluetoothLeScanner.startScan(getLeScanCallback());
            } else {
                mScanning = false;
                bluetoothLeScanner.stopScan(getLeScanCallback());

                // Restart scan.
                scanLeDevices();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAndSubmitNewLedText() {
        _previewTexts.add(LedText.getDefaultLedTextForId(_previewTexts.size() + 1));
        previewTexts.setValue(_previewTexts);
    }

    public void removeAndSubmitLedTexts(int position) {
        if (position < 0 || _previewTexts.isEmpty())
            return;

        _previewTexts.remove(position);

        if (_previewTexts.isEmpty()) {
            previewTexts.setValue(_previewTexts);
            return;
        }

        setLastModifiedLedTextIndex(0);

        // Rearrange ids of LedText from 1 - 8 again.
        for (int i = 0; i < _previewTexts.size(); i++) {
            if (_previewTexts.get(i).getId() != i + 1) {
                LedText ledText = _previewTexts.get(i);
                ledText.setId(i + 1);
                _previewTexts.set(i, ledText);
            }
        }

        Log.d("MainActivityViewModel", "About to set value...");
        previewTexts.setValue(_previewTexts);
    }

    public void updatePreviewText(int position, String text, boolean updateNow) {
        LedText currentLedText = LedText.getDefaultLedTextForId(position + 1);
        currentLedText.setText(text);
        ArrayList<LedText> tempPreviewTexts = new ArrayList<>();
        for (LedText ledText : _previewTexts) {
            tempPreviewTexts.add(ledText);
        }
        tempPreviewTexts.set(position, currentLedText);
        if (updateNow)
            previewTexts.setValue(tempPreviewTexts);
        else
            _previewTexts = tempPreviewTexts;
        //_previewTexts.set(position, currentLedText);
        //previewTexts.setValue(null);
    }

    public String getReverseString(String text) {
        StringBuilder previewTextBuilder = new StringBuilder();
        previewTextBuilder.append(text);
        return previewTextBuilder.reverse().toString();
    }
}
