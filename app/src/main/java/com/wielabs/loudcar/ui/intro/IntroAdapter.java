package com.wielabs.loudcar.ui.intro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class IntroAdapter extends FragmentStateAdapter {
    private final int NUM_PAGES = 3;

    public IntroAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);

        IntroFragment introFragment = new IntroFragment();
        introFragment.setArguments(bundle);

        return introFragment;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
