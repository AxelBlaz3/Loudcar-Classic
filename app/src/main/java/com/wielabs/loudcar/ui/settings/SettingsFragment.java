package com.wielabs.loudcar.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wielabs.loudcar.databinding.FragmentSettingsBinding;
import com.wielabs.loudcar.ui.MainActivity;
import com.wielabs.loudcar.ui.MainActivityViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    @Inject
    MainActivityViewModel mainActivityViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.settingsFindDevices.setOnClickListener(v -> {
            if (mainActivityViewModel.isBluetoothEnabled())
                if (((MainActivity) requireActivity()).isLocationPermissionGranted())
                    ((MainActivity) requireActivity()).navController.navigate(SettingsFragmentDirections.Companion.actionSettingsFragmentToFindDevicesFragment());
                else
                    ((MainActivity) requireActivity()).requestLocationPermission();
            else
                ((MainActivity) requireActivity()).requestBluetoothPermission();
        });

        binding.settingsTextAnimation.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navController.navigate(SettingsFragmentDirections.Companion.actionSettingsFragmentToTextAnimationFragment());
        });

        binding.settingsEffects.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navController.navigate(SettingsFragmentDirections.Companion.actionSettingsFragmentToEffectsFragment());
        });

        binding.settingsFontStyle.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navController.navigate(SettingsFragmentDirections.Companion.actionSettingsFragmentToFontStyleFragment());
        });
    }
}
