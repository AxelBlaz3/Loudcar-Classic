package com.wielabs.loudcar.ui.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wielabs.loudcar.databinding.FragmentBaseIntroBinding;
import com.wielabs.loudcar.ui.MainActivityViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BaseIntroFragment extends Fragment {
    private FragmentBaseIntroBinding binding;

    @Inject
    MainActivityViewModel mainActivityViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBaseIntroBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.introPager.setAdapter(new IntroAdapter(this));

        mainActivityViewModel.getIntroPosition().observe(getViewLifecycleOwner(), position -> {
            binding.introPager.setCurrentItem(position != -1 ? position : 0);
        });
    }
}
