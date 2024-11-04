package com.example.it_management_system;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class HomeAdminFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public HomeAdminFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);

        tabLayout = view.findViewById(R.id.tab_layoutAdmin);
        viewPager = view.findViewById(R.id.view_pagerAdmin);

        // Set up the ViewPager with a custom adapter
        viewPager.setAdapter(new TabPagerAdapter(this));

        // Connect TabLayout and ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Raise Complaint");
                    } else if (position == 1) {
                        tab.setText("Request Requirements");
                    }
                }
        ).attach();

        return view;
    }

    private static class TabPagerAdapter extends FragmentStateAdapter {

        public TabPagerAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new RaiseComplaintFragmentAdmin();
            } else {
                return new RequestRequirementsFragmentAdmin();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Total number of tabs
        }
    }
}
