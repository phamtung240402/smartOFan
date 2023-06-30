package com.example.ofan.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ofan.Fragment.DeviceFragment;
import com.example.ofan.Fragment.HomeFragment;
import com.example.ofan.Fragment.TimerFragment;

public class   ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: 
                return new HomeFragment();
            case 1:
                return new TimerFragment();
            case 2:
                return new DeviceFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
