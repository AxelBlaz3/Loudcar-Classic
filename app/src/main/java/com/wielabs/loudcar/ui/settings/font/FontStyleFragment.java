package com.wielabs.loudcar.ui.settings.font;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wielabs.loudcar.databinding.FragmentFontStyleBinding;
import com.wielabs.loudcar.util.SharedPreferencesUtil;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FontStyleFragment extends Fragment {
    private FragmentFontStyleBinding binding;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFontStyleBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switch (sharedPreferencesUtil.getFontFamilySelection()) {
            case 1:
                binding.defaultRb.setChecked(true);
                break;
            case 2:
                binding.sanSerifRb.setChecked(true);
                break;
            case 3:
                binding.poppinsRb.setChecked(true);
                break;
            case 4:
                binding.avenirRb.setChecked(true);
                break;
            case 5:
                binding.arialRb.setChecked(true);
                break;
        }

        binding.defaultRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setFontFamily(1);
        });

        binding.sanSerifRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setFontFamily(2);
        });

        binding.poppinsRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setFontFamily(3);
        });

        binding.avenirRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setFontFamily(4);
        });

        binding.arialRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setFontFamily(5);
        });
    }
}
