package com.wielabs.loudcar.ui.settings.textanimation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wielabs.loudcar.databinding.FragmentTextAnimationBinding;
import com.wielabs.loudcar.util.SharedPreferencesUtil;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TextAnimationFragment extends Fragment {
    private FragmentTextAnimationBinding binding;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTextAnimationBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switch (sharedPreferencesUtil.getAnimation()) {
            case 1:
                binding.noneRb.setChecked(true);
                break;
            case 2:
                binding.rTLRb.setChecked(true);
                break;
            case 3:
                binding.lTRRb.setChecked(true);
                break;
            case 4:
                binding.bTTRb.setChecked(true);
                break;
            case 5:
                binding.tTBRb.setChecked(true);
                break;
        }

        binding.noneRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setAnimation(1);
        });

        binding.rTLRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setAnimation(2);
        });

        binding.lTRRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setAnimation(3);
        });

        binding.bTTRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setAnimation(4);
        });

        binding.tTBRb.setOnClickListener(v -> {
            sharedPreferencesUtil.setAnimation(5);
        });
    }
}
