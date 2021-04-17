package com.wielabs.loudcar.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.wielabs.loudcar.R;
import com.wielabs.loudcar.databinding.ActivityMainBinding;
import com.wielabs.loudcar.ui.home.HomeFragmentDirections;
import com.wielabs.loudcar.util.Constants;
import com.wielabs.loudcar.util.SharedPreferencesUtil;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener, View.OnClickListener {
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_LOCATION = 2;
    public static final int REQUEST_TURN_ON_LOCATION = 3;
    public static final int REQUEST_CODE_SPEECH_RECOGNITION = 4;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    public static int brig = 5;
    public static int sendVal = 0;
    public static int connectFlag = 0;
    public static int pix = 16;
    public static ByteBuffer[][] bytedata = ((ByteBuffer[][]) Array.newInstance(ByteBuffer.class, new int[]{8, 16}));
    public static List<Map<String, String>> data = new ArrayList();
    public static String logFlag = "0";
    public static String logFont = "/system/fonts/DroidSans.ttf";
    public static int logFontSize = 16;
    public static int logFontStyle = 0;
    public static String logContext = "";

    static {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 16; j++) {
                bytedata[i][j] = ByteBuffer.allocate(512);
            }
        }
    }

    private ActivityMainBinding binding;
    public NavController navController;
    private final BroadcastReceiver bluetoothDeviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ArrayList<BluetoothDevice> mClassicDevices = mainActivityViewModel.getmClassicDevices();

                // Verify and add the new classic device to list.
                if (!mainActivityViewModel.classicDeviceAddresses.contains(device.getAddress())) {
                    mainActivityViewModel.classicDeviceAddresses.add(device.getAddress());
                    mClassicDevices.add(device);
                    mainActivityViewModel.postClassicDevices(mClassicDevices);
                }
            }
        }
    };

    @Inject
    MainActivityViewModel mainActivityViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_LoudcarClassic);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        IntentFilter devicesIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // Register receiver for discovering devices.
        registerReceiver(bluetoothDeviceReceiver, devicesIntentFilter);

        // Determine whether BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Grab brightness value from preferences.
        brig = sharedPreferencesUtil.getBrightnessSelection();
        // Add default data.
        data.add(getModel(sharedPreferencesUtil));
        data.add(getModel(sharedPreferencesUtil));
        data.add(getModel(sharedPreferencesUtil));
        data.add(getModel(sharedPreferencesUtil));
        data.add(getModel(sharedPreferencesUtil));
        data.add(getModel(sharedPreferencesUtil));
        data.add(getModel(sharedPreferencesUtil));
        data.add(getModel(sharedPreferencesUtil));

        binding.speechRecognition.setOnClickListener(this);
        binding.settings.setOnClickListener(this);
        binding.home.setOnClickListener(this);

        navController = ((NavHostFragment) (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))).getNavController();
        navController.addOnDestinationChangedListener(this);

        Intent appLinkIntent = getIntent();
        Uri intentData = appLinkIntent.getData();
        if (intentData != null)
            handleIntent(intentData);

        binding.toolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp();
        });
    }

    private void handleIntent(Uri intentData) {
        String queryParam = intentData.getQueryParameter(Constants.PARAM_THING_TYPE);
        if (queryParam != null && !queryParam.isEmpty())
            navController.navigate(HomeFragmentDirections.Companion.actionGlobalHomeFragment(queryParam));
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        switch (destination.getId()) {
            case R.id.baseIntroFragment: {
                binding.setHideNavigationIcon(true);
                binding.setHideLogo(true);
                binding.setHideTitle(true);
                binding.setHideBottomBar(true);
                break;
            }
            case R.id.homeFragment: {
                binding.setHideNavigationIcon(true);
                binding.setHideLogo(false);
                binding.setHideTitle(false);
                binding.setTitle(getString(R.string.app_name));
                binding.setHideBottomBar(false);
                focusOnSelectedNavItem(R.id.homeFragment);
                break;
            }
            case R.id.settingsFragment: {
                binding.setHideNavigationIcon(false);
                binding.setHideLogo(true);
                binding.setHideTitle(false);
                binding.setTitle(destination.getLabel().toString());
                binding.setHideBottomBar(false);
                focusOnSelectedNavItem(R.id.settingsFragment);
                break;
            }
            case R.id.findDevicesFragment: {
                binding.setHideNavigationIcon(false);
                binding.setHideLogo(true);
                binding.setHideTitle(false);
                binding.setTitle(destination.getLabel().toString());
                binding.setHideBottomBar(false);
                break;
            }

            default: {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_RECOGNITION) {
            if (data == null)
                return;

            ArrayList<String> recognizedText = data.getStringArrayListExtra("android.speech.extra.RESULTS");
            mainActivityViewModel.updatePreviewText(mainActivityViewModel.getLastModifiedLedTextIndex(), recognizedText.get(0), true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speech_recognition: {
                // navController.navigate(HomeFragmentDirections.Companion.actionGlobalSpeechRecognitionBottomSheetDialog());
                Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                startActivityForResult(speechRecognizerIntent, REQUEST_CODE_SPEECH_RECOGNITION);
                break;
            }
            case R.id.settings: {
                navController.navigate(HomeFragmentDirections.Companion.actionGlobalSettingsFragment());
                break;
            }
            case R.id.home: {
                navController.navigate(HomeFragmentDirections.Companion.actionGlobalHomeFragment(null));
                break;
            }
            default: {
            }
        }
    }

    private void focusOnSelectedNavItem(int selectedItemId) {
        if (R.id.homeFragment == selectedItemId) {
            binding.home.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_400)));
            binding.settings.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey)));
        } else if (R.id.settingsFragment == selectedItemId) {
            binding.home.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey)));
            binding.settings.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.purple_400)));
        }
    }

    public void requestBluetoothPermission() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
    }

    public void requestForTogglingLocationIfRequired() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGpsEnabled) {
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_TURN_ON_LOCATION);
        }
    }

    private static Map<String, String> getModel(SharedPreferencesUtil sharedPreferencesUtil) {
        Map<String, String> model = new HashMap<>();
        model.put("check", "false");
        model.put("flag", "0");
        model.put("context", "");
        model.put("reverse", "false");
        model.put("fontSize", "16");
        model.put("fontFile", "/system/fonts/DroidSans.ttf");
        model.put("fontStyle", "0");
        model.put("time", 8 - sharedPreferencesUtil.getSpeedSelection() + "");
        model.put("action", sharedPreferencesUtil.getDirectionSelection() + "");
        model.put("twinkle", sharedPreferencesUtil.getFlash() + "");
        model.put("border", sharedPreferencesUtil.getEdging() + "");
        model.put("qianru", "0");
        return model;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(bluetoothDeviceReceiver);
    }
}
