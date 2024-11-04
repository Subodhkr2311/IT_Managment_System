package com.example.it_management_system;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyPagerAdapter extends FragmentStateAdapter {

    public MyPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new tabComplaintsExecutive();  // Fragment showing complaint details
            case 1:
                return new tabRequirementExecutive();  // Fragment showing requirements details
            default:
                return new tabComplaintsExecutive();
        }
    }

    @Override
    public int getItemCount() {
        return 2;  // We have two tabs: Complaints and Requirements
    }
}