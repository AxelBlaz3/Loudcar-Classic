package com.wielabs.loudcar.ui.home;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.wielabs.loudcar.R;
import com.wielabs.loudcar.databinding.FragmentHomeBinding;
import com.wielabs.loudcar.ui.MainActivity;
import com.wielabs.loudcar.ui.MainActivityViewModel;
import com.wielabs.loudcar.ui.devices.ConnectThread;
import com.wielabs.loudcar.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, LedTextAdapter.LedTextAdapterListener {
    private FragmentHomeBinding binding;
    private LedTextAdapter ledTextAdapter;
    private String queryText;

    @Inject
    MainActivityViewModel mainActivityViewModel;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    private final BroadcastReceiver bluetoothStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                binding.bluetoothToggle.setChecked(bluetoothState == BluetoothAdapter.STATE_ON);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queryText = HomeFragmentArgs.fromBundle(getArguments()).getText();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (sharedPreferencesUtil.isFirstLaunch())
            ((MainActivity) requireActivity()).navController.navigate(HomeFragmentDirections.Companion.actionHomeFragmentToBaseIntroFragment());

        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (!mainActivityViewModel.isBluetoothEnabled())
            ((MainActivity) requireActivity()).requestBluetoothPermission();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().registerReceiver(bluetoothStateChangeReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        if (queryText != null && !queryText.isEmpty()) {
            binding.ledTv.setText(queryText);
            Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
            if (bondedDevices.isEmpty())
                Toast.makeText(getContext(), "Bluetooth is off or no connected devices", Toast.LENGTH_LONG).show();
            else {
                //Log.d("Device to connect", "*" + sharedPreferencesUtil.getLastKnownAddress());
                for (BluetoothDevice device : bondedDevices) {
                    //Log.d("Current device", device.getName());
                    if (sharedPreferencesUtil.getLastKnownAddress().equalsIgnoreCase(device.getAddress())) {
                        // Start connection.
                        // Set value of first text to 'queryText'.
                        MainActivity.data.get(0).put("context", queryText);
                        MainActivity.data.get(0).put("check", "true");
                        new ConnectThread(device).start();
                        break;
                    }
                }
            }
        }

        binding.directTopIv.setOnClickListener(this);
        binding.directLeftIv.setOnClickListener(this);
        binding.directBottomIv.setOnClickListener(this);
        binding.directRightIv.setOnClickListener(this);
        binding.newLedTextFab.setOnClickListener(this);
        binding.findDevicesButton.setOnClickListener(this);
        binding.sendLedText.setOnClickListener(this);
        binding.decreaseSpeedIv.setOnClickListener(this);
        binding.increaseSpeedIv.setOnClickListener(this);
        binding.bluetoothToggle.setOnClickListener(this);
        binding.reverseTextSwitch.setOnCheckedChangeListener(this);
        binding.borderSwitch.setOnCheckedChangeListener(this);
        binding.flashSwitch.setOnCheckedChangeListener(this);
        binding.speedSlider.addOnChangeListener((slider, value, fromUser) -> {
            sharedPreferencesUtil.setSpeed((int) value);

            for (int i = 0; i < MainActivity.data.size(); ++i)
                MainActivity.data.get(i).put("time", (int) (8 - value) + "");
        });
        binding.brightnessSlider.addOnChangeListener((slider, value, fromUser) -> {
            sharedPreferencesUtil.setBrightness((int) value);

            //MainActivity.connectFlag = 2;
            //MainActivity.sendVal = (int) value;
            MainActivity.brig = (int) value;
        });

        // Display UI according to user preferences.
        int direction = sharedPreferencesUtil.getDirectionSelection();
        focusOnSelectedDirection(direction == 1 ? R.id.direct_left_iv : (direction == 2 ? R.id.direct_right_iv : (direction == 3 ? R.id.direct_top_iv : R.id.direct_bottom_iv)));

        // Preset user preferences.
        binding.reverseTextSwitch.setChecked(sharedPreferencesUtil.getReverseTextToggle());
        binding.speedSlider.setValue(sharedPreferencesUtil.getSpeedSelection());
        binding.brightnessSlider.setValue(sharedPreferencesUtil.getBrightnessSelection());
        binding.borderSwitch.setChecked(sharedPreferencesUtil.getEdging());
        binding.flashSwitch.setChecked(sharedPreferencesUtil.getFlash());
        binding.bluetoothToggle.setChecked(mainActivityViewModel.isBluetoothEnabled());

        ledTextAdapter = new LedTextAdapter(new LedTextAdapter.LedTextDiffUtil(), this);
        binding.previewEditTextRv.setAdapter(ledTextAdapter);

        // Populate an empty LedText if empty.
        if (mainActivityViewModel.getPreviewTextsLiveData().getValue().isEmpty())
            mainActivityViewModel.addAndSubmitNewLedText();

        mainActivityViewModel.getPreviewTextsLiveData().observe(getViewLifecycleOwner(), ledTexts -> {
            Log.d("MainActivityViewModel", "Submitting list...");
            //ledTextAdapter.submitList(null);
            ledTextAdapter.submitList(new ArrayList<>(ledTexts));
        });

        // Setup fontsSpinner.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.fonts_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.fontsSpinner.setAdapter(adapter);
    }

    private void focusOnSelectedDirection(int selectedDirectionId) {
        if (selectedDirectionId == R.id.direct_bottom_iv) {
            binding.directBottomIv.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_400));
            binding.directLeftIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directRightIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directTopIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
        } else if (selectedDirectionId == R.id.direct_right_iv) {
            binding.directBottomIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directLeftIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directRightIv.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_400));
            binding.directTopIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
        } else if (selectedDirectionId == R.id.direct_top_iv) {
            binding.directBottomIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directLeftIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directRightIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directTopIv.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_400));
        } else {
            binding.directBottomIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directLeftIv.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_400));
            binding.directRightIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
            binding.directTopIv.setBackgroundColor(getAttrForResourceId(android.R.attr.colorControlHighlight));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.direct_bottom_iv:
            case R.id.direct_right_iv:
            case R.id.direct_top_iv:
            case R.id.direct_left_iv: {
                int viewId = v.getId();
                int direction = (viewId == R.id.direct_bottom_iv ? 4 : (viewId == R.id.direct_top_iv ? 3 : (viewId == R.id.direct_right_iv ? 2 : 1)));
                for (int i = 0; i < 8; ++i)
                    MainActivity.data.get(i).put("action", direction + "");
                sharedPreferencesUtil.setDirection(direction);
                focusOnSelectedDirection(viewId);
                break;
            }
            case R.id.new_led_text_fab: {
                if (mainActivityViewModel.getPreviewTextsLiveData().getValue().size() < 8) {
                    mainActivityViewModel.addAndSubmitNewLedText();
                } else
                    Toast.makeText(requireContext(), getString(R.string.max_led_texts_toast_msg), Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.find_devices_button: {
                if (mainActivityViewModel.isBluetoothEnabled())
                    if (((MainActivity) requireActivity()).isLocationPermissionGranted())
                        ((MainActivity) requireActivity()).navController.navigate(HomeFragmentDirections.Companion.actionHomeFragmentToFindDevicesFragment());
                    else
                        ((MainActivity) requireActivity()).requestLocationPermission();
                else
                    ((MainActivity) requireActivity()).requestBluetoothPermission();
                break;

            }
            case R.id.send_led_text: {
                if (mainActivityViewModel.isBluetoothEnabled())
                    if (((MainActivity) requireActivity()).isLocationPermissionGranted()) {
                        ((MainActivity) requireActivity()).requestForTogglingLocationIfRequired();

                        ((MainActivity) requireActivity()).navController.navigate(HomeFragmentDirections.Companion.actionHomeFragmentToDevicesBottomSheetDialog());
                    } else
                        ((MainActivity) requireActivity()).requestLocationPermission();
                else
                    ((MainActivity) requireActivity()).requestBluetoothPermission();
                break;
            }
            case R.id.decrease_speed_iv: {
                int currentSpeedValue = (int) binding.speedSlider.getValue();
                binding.speedSlider.setValue(currentSpeedValue > 1 ? currentSpeedValue - 1 : currentSpeedValue);
                break;
            }
            case R.id.increase_speed_iv: {
                int currentSpeedValue = (int) binding.speedSlider.getValue();
                binding.speedSlider.setValue(currentSpeedValue < 7 ? currentSpeedValue + 1 : currentSpeedValue);
                break;
            }
            case R.id.bluetooth_toggle: {
                mainActivityViewModel.toggleBluetooth();
            }
            default: {
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == MainActivity.REQUEST_LOCATION)
            ((MainActivity) requireActivity()).navController.navigate(HomeFragmentDirections.Companion.actionHomeFragmentToFindDevicesFragment());

    }

    private int getAttrForResourceId(int resourceId) {
        TypedValue outValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(resourceId, outValue, true);
        return outValue.data;
    }

    @Override
    public void onRemoveLedText(int position) {
        mainActivityViewModel.removeAndSubmitLedTexts(position);
        // Clear the context at position.
        MainActivity.data.get(position).put("context", "");

        binding.ledTv.setText(getString(R.string.preview));
    }

    @Override
    public void onLedTextChanged(String previewText, int position) {
        mainActivityViewModel.setLastModifiedLedTextIndex(position);
        if (previewText == null || previewText.isEmpty()) {
            MainActivity.data.get(position).put("context", "");
            mainActivityViewModel.updatePreviewText(position, previewText, false);
        }
        else {
            MainActivity.data.get(position).put("context", previewText);
            MainActivity.data.get(position).put("check", "true");
            mainActivityViewModel.updatePreviewText(position, previewText, false);
        }
        if (!mainActivityViewModel.isReverseLedText)
            binding.ledTv.setText(previewText);
        else
            binding.ledTv.setText(mainActivityViewModel.getReverseString(previewText));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.flash_switch: {
                sharedPreferencesUtil.setFlash(isChecked);
                // Enable/Disable flash.
                for (int i = 0; i < MainActivity.data.size(); ++i)
                    MainActivity.data.get(i).put("twinkle", isChecked ? "true" : "false");
                break;
            }

            case R.id.border_switch: {
                sharedPreferencesUtil.setEdging(isChecked);
                // Enable/Disable border.
                for (int i = 0; i < MainActivity.data.size(); ++i)
                    MainActivity.data.get(i).put("border", isChecked ? "true" : "false");
                break;
            }

            case R.id.reverse_text_switch: {
                sharedPreferencesUtil.setReverseText(isChecked);
                // Reverse led texts.
                for (int i = 0; i < MainActivity.data.size(); ++i) {
                    String tempContext = MainActivity.data.get(i).get("context");
                    if (tempContext == null || tempContext.isEmpty())
                        continue;
                    MainActivity.data.get(i).put("reverse", binding.reverseTextSwitch.isChecked() ? "true" : "false");
                }

                if (isChecked || mainActivityViewModel.isReverseLedText)
                    binding.ledTv.setText(mainActivityViewModel.getReverseString(binding.ledTv.getText().toString()));
                mainActivityViewModel.isReverseLedText = isChecked;
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        requireActivity().unregisterReceiver(bluetoothStateChangeReceiver);
    }
}
