package com.example.it_management_system;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboard extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarAdmin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigationAdmin);
        fragmentManager = getSupportFragmentManager();

        // Set default fragment on dashboard load
        if (savedInstanceState == null) {
            // Set the initial fragment (like DashboardFragment)
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_containerAdmin, new HomeAdminFragment());
            fragmentTransaction.commit();
        }

        // Set Bottom Navigation item select listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_homeAdmin) {  // Example menu item
                selectedFragment = new HomeAdminFragment();
            } else if (itemId == R.id.nav_ticketsAdmin) {  // Example menu item
                selectedFragment = new TicketAdminFragment();
            } else if (itemId == R.id.nav_executives) {  // Example menu item
                selectedFragment = new ExecutivesListAdminFragment();
            } else {  // Example menu item
                selectedFragment = new SettingAdminFragment();
            }

            if (selectedFragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_containerAdmin, selectedFragment);
                transaction.commit();
            }
            return true;
        });

    }
}