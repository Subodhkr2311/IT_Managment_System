package com.example.it_management_system;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyticketsExecutiveFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MyPagerAdapter myPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mytickets_executive, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Set up ViewPager2 with a FragmentStateAdapter
        myPagerAdapter = new MyPagerAdapter(this);
        viewPager.setAdapter(myPagerAdapter);

        // Set up TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Complaints");
                    break;
                case 1:
                    tab.setText("Requirements");
                    break;
            }
        }).attach();

        return view;
    }

    }





