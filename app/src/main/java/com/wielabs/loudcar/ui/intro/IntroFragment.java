package com.wielabs.loudcar.ui.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wielabs.loudcar.databinding.FragmentIntroBinding;
import com.wielabs.loudcar.ui.MainActivity;
import com.wielabs.loudcar.ui.MainActivityViewModel;
import com.wielabs.loudcar.util.SharedPreferencesUtil;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class IntroFragment extends Fragment {
    private FragmentIntroBinding binding;
    private int position;

    @Inject
    MainActivityViewModel mainActivityViewModel;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt("position", 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIntroBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setPosition(position);

        binding.nextButton.setOnClickListener(v -> {
            if (position < 2)
                mainActivityViewModel.setIntroPosition(position + 1);
            else {
                mainActivityViewModel.setIntroPosition(position + 1);
                sharedPreferencesUtil.saveIsFirstLaunch(false);
                ((MainActivity) requireActivity()).navController.navigate(BaseIntroFragmentDirections.Companion.actionBaseIntroFragmentToHomeFragment(null));
            }
        });

        binding.skipButton.setOnClickListener(v -> {
            mainActivityViewModel.setIntroPosition(3);
            sharedPreferencesUtil.saveIsFirstLaunch(false);
            ((MainActivity) requireActivity()).navController.navigate(BaseIntroFragmentDirections.Companion.actionBaseIntroFragmentToHomeFragment(null));
        });
    }
}
