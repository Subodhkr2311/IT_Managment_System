package com.example.it_management_system;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyticketsExecutiveFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MyPagerAdapter myPagerAdapter;
    private ChipGroup filterChipGroup;
    public static final int FILTER_ALL = 0;
    public static final int FILTER_ASSIGNED = 1;
    public static final int FILTER_UNASSIGNED = 2;
    public static final int FILTER_MY = 3;
    public static final int FILTER_RECENT = 4;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mytickets_executive, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);


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
        setupFilterChips();
        return view;
    }
    private void setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int filterType;
            if (checkedId == R.id.chipAssigned) {
                filterType = FILTER_ASSIGNED;
            } else if (checkedId == R.id.chipUnassigned) {
                filterType = FILTER_UNASSIGNED;
            } else if (checkedId == R.id.chipMy) {
                filterType = FILTER_MY;
            } else if (checkedId == R.id.chipRecent) {
                filterType = FILTER_RECENT;
            } else {
                filterType = FILTER_ALL;
            }

            // Notify both fragments about the filter change
            if (viewPager.getCurrentItem() == 0) {
                tabComplaintsExecutive complaintsTab = (tabComplaintsExecutive) myPagerAdapter.getCurrentFragment();
                if (complaintsTab != null) {

                }
            } else {
                tabRequirementExecutive requirementsTab = (tabRequirementExecutive) myPagerAdapter.getCurrentFragment();
                if (requirementsTab != null) {
                    requirementsTab.applyFilter(filterType);
                }
            }
        });
    }

    public static boolean matchesFilter(String assignedTo, String date, int filterType) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        switch (filterType) {
            case FILTER_ASSIGNED:
                return assignedTo != null && !assignedTo.isEmpty();
            case FILTER_UNASSIGNED:
                return assignedTo == null || assignedTo.isEmpty();
            case FILTER_MY:
                return assignedTo != null && assignedTo.equals(currentUserId);
            case FILTER_RECENT:
                if (date == null) return false;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date ticketDate = sdf.parse(date);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, -7); // Last 7 days
                    return ticketDate.after(cal.getTime());
                } catch (Exception e) {
                    return false;
                }
            default: // FILTER_ALL
                return true;
        }
    }

    }





