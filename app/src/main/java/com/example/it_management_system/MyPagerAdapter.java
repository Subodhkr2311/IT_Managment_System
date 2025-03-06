package com.example.it_management_system;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.HashMap;

public class MyPagerAdapter extends FragmentStateAdapter {
    private Fragment currentFragment;
    private final HashMap<Integer, Fragment> fragmentMap = new HashMap<>();

    public MyPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new tabComplaintsExecutive();
                break;
            case 1:
                fragment = new tabRequirementExecutive();
                break;
            default:
                fragment = new tabComplaintsExecutive();
        }
        fragmentMap.put(position, fragment);
        currentFragment = fragment;
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;  // We have two tabs: Complaints and Requirements
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}